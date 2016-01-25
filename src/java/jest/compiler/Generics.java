package jest.compiler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import jest.compiler.Errors.ArgumentNumberError;
import jest.compiler.Errors.GenericInferenceError;
import jest.compiler.Errors.GenericTypeError;
import jest.Utils.Pair;
import jest.Utils.Triplet;
import jest.compiler.Errors.ParameterCategoryMismatch;
import jest.compiler.Errors.ParameterNumberMismatch;
import jest.compiler.Types.FunctionType;
import jest.compiler.Types.GenericFunctionDeclaration;
import jest.compiler.Types.GenericParameter;
import jest.compiler.Types.Type;

import jest.compiler.Errors.FunctionCallError;

import static jest.Utils.zip;


public class Generics {


    public static Optional<FunctionCallError> checkGenericFunctionCall(GenericFunctionDeclaration typeDeclaration,
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

            Optional<FunctionCallError> result = ensureMatchingShapesOfTypes(types.left, types.right);
            if (result.isPresent()) {
                return result;
            }

            genericConstraints.addAll(getGenericTypeConstraints(types.left, types.right));
        }

        GenericInferenceSummary result = TypeInference.inferGenericTypes(genericConstraints);

        if (!result.inferenceSuccessful) {
            return Optional.of(new GenericInferenceError(typeDeclaration, argumentTypes));
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
     */
    public static Optional<FunctionCallError> ensureMatchingShapesOfTypes(Type declaration, Type usage) {

        if (declaration.getDependentTypes().size() != usage.getDependentTypes().size()) {
            return Optional.of(new ParameterNumberMismatch(declaration.getDependentTypes().size(), usage.getDependentTypes().size()));
            //throw new GenericMismatch();
        }

        if (FunctionType.class.isAssignableFrom(declaration.getClass()) &&
            !FunctionType.class.isAssignableFrom(usage.getClass())) {
            return Optional.of(new ParameterCategoryMismatch(declaration, usage));
        }

        if (declaration.getDependentTypes().size() == 0) {
            // If neither are generic, then they must match
            if (!declaration.isGeneric() && !usage.isGeneric()) {
                if (!usage.implementsType(declaration)) {
                    // TODO: Make this a better error
                    return Optional.of(new GenericTypeError("foobar", declaration, usage));
                }
            }
        } else {
            for (Pair<Type, Type> types: zip(declaration.getDependentTypes(), usage.getDependentTypes())) {
                Optional<FunctionCallError> error = ensureMatchingShapesOfTypes(types.left, types.right);
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
