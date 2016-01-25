package jest.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import jest.Exception.FunctionParameterTypeMismatch;
import jest.Exception.GenericInferenceError;
import jest.Exception.ValidationException;
import jest.Exception.WrongNumberOfFunctionParameters;
import jest.Utils.Pair;
import jest.Utils.Triplet;
import jest.compiler.Types.FunctionType;
import jest.compiler.Types.GenericFunctionDeclaration;
import jest.compiler.Types.GenericParameter;
import jest.compiler.Types.Type;
import jest.compiler.Validator.ParameterTypeMismatch;
import org.antlr.v4.runtime.ParserRuleContext;

import static jest.Utils.zip;


public class Generics {

    public static class GenericMismatch extends Exception {
    };

    interface GenericFunctionCallTypeError {
        ValidationException createException(String functionName, ParserRuleContext context);
    }

    public static class ArgumentNumberError implements GenericFunctionCallTypeError {

        public final Integer numExpected;

        public final Integer numEncountered;

        public ArgumentNumberError(Integer numExpected, Integer numEncountered) {
            this.numExpected = numExpected;
            this.numEncountered = numEncountered;
        }

        @Override
        public ValidationException createException(String functionName, ParserRuleContext context) {
            return new WrongNumberOfFunctionParameters(context, functionName, numExpected, numEncountered);
        }
    }

    public static class GenericTypeError implements GenericFunctionCallTypeError {

        public final String paramName;

        public final Type expected;

        public final Type encountered;

        public GenericTypeError(String paramName, Type expected, Type encountered) {
            this.paramName = paramName;
            this.expected = expected;
            this.encountered = encountered;
        }

        @Override
        public ValidationException createException(String functionName, ParserRuleContext context) {
            return new FunctionParameterTypeMismatch(context, functionName, paramName, expected, encountered);
        }
    }

    public static class GenericError implements GenericFunctionCallTypeError {

        public final GenericFunctionDeclaration declaration;

        public final List<Type> callingTypes;

        public GenericError(GenericFunctionDeclaration typeDeclaration, List<Type> callingTypes) {
            this.declaration = typeDeclaration;
            this.callingTypes = ImmutableList.copyOf(callingTypes);
        }

        @Override
        public ValidationException createException(String functionName, ParserRuleContext context) {
            return new GenericInferenceError(context, functionName, declaration, callingTypes);
        }
    }


    public static Optional<GenericFunctionCallTypeError> checkGenericFunctionCall(GenericFunctionDeclaration typeDeclaration,
                                                                                  List<Type> argumentTypes) {

        if (argumentTypes.size() != typeDeclaration.getSignature().parameterTypes.size()) {
            return Optional.of(new ArgumentNumberError(typeDeclaration.getSignature().parameterTypes.size(), argumentTypes.size()));
        }

        // Check the non=genric parameters
        for (Triplet<String, Type, Type> types : zip(typeDeclaration.getParameterNames(), typeDeclaration.getSignature().parameterTypes, argumentTypes)) {
            if (types._1.isGeneric()) {
                continue;
            }
            if (!types._2.implementsType(types._1)) {
                return Optional.of(new GenericTypeError(types._0, types._1, types._2));
            }
        }

        Set<Pair<GenericParameter, Type>> genericConstraints = Sets.newHashSet();

        for (Pair<Type, Type> types: zip(typeDeclaration.signature.parameterTypes, argumentTypes)) {
            try {
                ensureMatchingShapesOfTypes(types.left, types.right);
            } catch (GenericMismatch genericMismatch) {
                return Optional.of(new GenericTypeError("", types.left, types.right));
            }
            genericConstraints.addAll(getGenericTypeConstraints(types.left, types.right));
        }

        GenericInferenceSummary result = TypeInference.inferGenericTypes(genericConstraints);

        if (!result.inferenceSuccessful) {
            return Optional.of(new GenericError(typeDeclaration, argumentTypes));
        }

        return Optional.empty();
    }


    /**
     * Check the "shape" of the declaration and usage parameters
     * This ensures that:
     * - They have the same number of dependent types
     * - They are both variables or functions
     * - Their generic parameters line up (we will try to infer
     *   the types of generic parameters later)
     * @param declaration
     * @param usage
     * @return
     * @throws GenericMismatch
     */
    public static Optional<ParameterTypeMismatch> ensureMatchingShapesOfTypes(Type declaration, Type usage)
        throws GenericMismatch {

        if (declaration.getDependentTypes().size() != usage.getDependentTypes().size()) {
            throw new GenericMismatch();
        }

        if (FunctionType.class.isAssignableFrom(declaration.getClass()) &&
            !FunctionType.class.isAssignableFrom(usage.getClass())) {
            throw new GenericMismatch();
        }

        if (declaration.getDependentTypes().size() == 0) {
            // If neither are generic, then they must match
            if (!declaration.isGeneric() && !usage.isGeneric()) {
                if (!usage.implementsType(declaration)) {
                    // TODO: Make this a better error
                    return Optional.of(new ParameterTypeMismatch("foobar", declaration, usage));
                }
            }
        } else {
            for (Pair<Type, Type> types: zip(declaration.getDependentTypes(), usage.getDependentTypes())) {
                Optional<ParameterTypeMismatch> error = ensureMatchingShapesOfTypes(types.left, types.right);
                if (error.isPresent()) {
                    return error;
                }
            }
        }

        return Optional.empty();
    }


    /**
     * Takes a single type, which may have deeply
     * nested dependent types, and return a single
     * iterable that walks over all of the dependent
     * types (using a depth-first strategy)
     * @param type
     * @return
     */
    public static Iterable<Type> walkTypeDependencies(Type type) {

        Stack<Type> types = new Stack<>();

        if (type.getDependentTypes().size()==0) {
            types.push(type);
        } else {
            for (Type dependentType: type.getDependentTypes()) {
                for (Type node: walkTypeDependencies(dependentType)) {
                    types.push(node);
                }
            }
        }
        return types;
    }


    /**
     * Given two types, which may be nested, walk through all
     * types and return a list of type constraints for generic
     * parameters.  A constraint is simply defined as any time
     * that a generic parameter lines up with another parameter
     * (which may also be generic).
     *
     * So, if our type structure looks like:
     *
     * [A [B] D]
     * [E [F] Number]
     *
     * assuming all letters are generics,
     * we emit the following pairs:
     *
     * [
     *   [A E]
     *   [E A]
     *   [B F]
     *   [F B]
     *   [D Number]
     * ]
     *
     * @param declaration
     * @param usage
     * @return
     */
    public static List<Pair<GenericParameter, Type>> getGenericTypeConstraints(Type declaration, Type usage) {

        List<Pair<GenericParameter, Type>> pairs = Lists.newArrayList();

        for (Pair<Type, Type> types: zip(walkTypeDependencies(declaration), walkTypeDependencies(usage))) {

            if (GenericParameter.class.isAssignableFrom(types.left.getClass())) {
                pairs.add(new Pair<>((GenericParameter)types.left, types.right));
            }
            if (GenericParameter.class.isAssignableFrom(types.right.getClass())) {
                pairs.add(new Pair<>((GenericParameter)types.right, types.left));
            }
        }
        return pairs;
    }

    public static class GenericInferenceSummary {

        public final Boolean inferenceSuccessful;

        public final Map<GenericParameter, Type> inferredTypes;

        private GenericInferenceSummary(Boolean inferenceSuccessful, Map<GenericParameter, Type> inferredTypes) {
            this.inferenceSuccessful = inferenceSuccessful;
            this.inferredTypes = ImmutableMap.copyOf(inferredTypes);
        }

        public static GenericInferenceSummary success(Map<GenericParameter, Type> inferredTypes) {
            return new GenericInferenceSummary(true, inferredTypes);
        }

        public static GenericInferenceSummary failure() {
            return new GenericInferenceSummary(false, ImmutableMap.of());
        }
    }
}
