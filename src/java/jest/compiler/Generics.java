package jest.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import jest.Exception.FunctionParameterTypeMismatch;
import jest.Exception.InconsistentGenericTypes;
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

import static jest.Utils.getAll;
import static jest.Utils.zip;
import static jest.compiler.Types.GenericFunctionDeclaration.typesConsistent;


public class Generics {

    public static class GenericArguments {
        public final GenericParameter parameter;
        public final List<Type> typeDeclarations;
        public final List<Type> argumentTypes;

        public GenericArguments(GenericParameter parameter,
                                List<Type> typeDeclarations,
                                List<Type> argumentTypes) {
            this.parameter = parameter;
            this.typeDeclarations = ImmutableList.copyOf(typeDeclarations);
            this.argumentTypes = ImmutableList.copyOf(argumentTypes);
        }
    }


    /**
     * Takes the declaration of a generic function and a
     * list of types for the arguments that are used in a
     * specific call of that function and return a list of
     * those arguments (and the corresponding declared types
     * for those arguments) grouped by generic parameters
     * used in the function declaration.
     * These lists of types and arguments MAY overlap in the
     * case that a single argument in the function type
     * signature uses two generic parameters.
     *
     * Example:
     *
     * defn <T, U> mapIt(mapper: (T)->U, t: T, u: U) {...}
     *
     * and a call of
     *
     * mapIt(myMapper, t, u)
     *
     * would be grouped (using information notation) as:
     *
     * {T: [myMapper, t], U: [myMapper, u]}
     *
     * @param functionDeclaration
     * @param argumentTypes
     * @return
     */
    public static Iterable<GenericArguments> getGenericArguments(GenericFunctionDeclaration functionDeclaration,
                                                                 List<Type> argumentTypes) {

        List<GenericArguments> genericArguments = Lists.newArrayList();

        for (Entry<GenericParameter, List<Integer>> entry: functionDeclaration.getGenericTypeIndices().entrySet()) {

            GenericParameter parameter = entry.getKey();
            List<Integer> indices = entry.getValue();
            List<Type> declarations = getAll(functionDeclaration.getSignature().parameterTypes, indices);
            List<Type> arguments =  getAll(argumentTypes, indices);

            genericArguments.add(new GenericArguments(parameter, declarations, arguments));
        }

        return ImmutableList.copyOf(genericArguments);
    }


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

    public static class InconsistentGenericError implements GenericFunctionCallTypeError {

        public final GenericParameter param;

        public final Set<Type> encountered;

        public InconsistentGenericError(GenericParameter param, Set<Type> encounteredTypes) {
            this.param = param;
            this.encountered = ImmutableSet.copyOf(encounteredTypes);
        }

        @Override
        public ValidationException createException(String functionName, ParserRuleContext context) {
            return new InconsistentGenericTypes(context, functionName, param, encountered);
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

        Set<Pair<Type, Type>> genericConstraints = Sets.newHashSet();

        for (Pair<Type, Type> types: zip(typeDeclaration.signature.parameterTypes, argumentTypes)) {
            try {
                ensureMatchingShapesOfTypes(types.left, types.right);
            } catch (GenericMismatch genericMismatch) {
                return Optional.of(new GenericTypeError("", types.left, types.right)); //param.name, declaration, usage)); //types.left, types.right));

                //genericMismatch.printStackTrace();
            }
            genericConstraints.addAll(getGenericTypeConstraints(types.left, types.right));
        }

        //if (!genericConstraintsConsistent(genericConstraints)) {
          //
        //}



        for (GenericArguments arguments: Generics.getGenericArguments(typeDeclaration, argumentTypes)) {

            GenericParameter param = arguments.parameter;

            Set<Type> allUsageTypes = Sets.newHashSet();



            for (Pair<Type, Type> types: zip(arguments.typeDeclarations, arguments.argumentTypes)) {

                Type declaration = types.left;
                Type usage = types.right;

                try {
                    ensureMatchingShapesOfTypes(declaration, usage);
                    allUsageTypes.addAll(getTypesOfGenericParameter(param, declaration, usage));
                } catch (GenericMismatch genericMismatch) {
                    return Optional.of(new GenericTypeError(param.name, declaration, usage)); //types.left, types.right));
                }
            }

              if (!typesConsistent(allUsageTypes)) {
                return Optional.of(new InconsistentGenericError(param, allUsageTypes));
            }
        }

        return Optional.empty();
    }


    // OLD VERSION

    // TODO: Need to support ID types

    /**
     * Take a generic parameter declaration and
     * the type of an expression being used as an
     * argument for that declaration and return a list
     * of the concrete types used in the location
     * of the given generic parameter.  Throws an
     * exception if the generic type structure of the
     * usage type doesn't match the generic type structure
     * of the declaration type.
     *
     * Examples:
     *
     * parameter:   T
     * declaration: Map<T, U>
     * usage:       Map<Integer, String>
     * -----------
     * result:      Integer
     *
     *
     * parameter:   T
     * declaration: Map<T, U>, T
     * usage:       Map<Integer, Integer>, Double
     * -----------
     * result:      Integer, Double
     *
     *
     * parameter: T
     * declaration: Map<T, U>
     * usage:       Double
     * -----------
     * result:      GenericMismatch
     *
     * To do this, is has to recursively walk the Type-Tree defined
     * a generic parameter.
     *
     * Really, this should be a
     *
     * @param parameter
     * @param declaration
     * @param usage
     * @return
     */
    public static List<Type> getTypesOfGenericParameter(GenericParameter parameter,
                                                        Type declaration,
                                                        Type usage)
        throws GenericMismatch {

        if (declaration.getDependentTypes().size() != usage.getDependentTypes().size()) {
            throw new GenericMismatch();
        }

        if (declaration.getDependentTypes().size() == 0) {
            // This is the base case
            // If we get to a leaf in the tree and the
            // left side is equal to the generic parameter in question,
            // then we consider that a match to the right side and
            // we return that.
            if (declaration.equals(parameter)) {
                return ImmutableList.of(usage);
            } else {
                return ImmutableList.of();
            }
        } else {

            // Recursive case: If we get to a dependent type, we
            // assert that the two types match up

            List<Type> parameterTypesInUsage = Lists.newArrayList();

            for (Pair<Type, Type> types: zip(declaration.getDependentTypes(), usage.getDependentTypes())) {
                parameterTypesInUsage.addAll(getTypesOfGenericParameter(parameter, types.left, types.right));
            }

            return ImmutableList.copyOf(parameterTypesInUsage);
        }
    }



    // NEW VERSION


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
    public static List<Pair<Type, Type>> getGenericTypeConstraints(Type declaration, Type usage) {

        List<Pair<Type, Type>> pairs = Lists.newArrayList();

        for (Pair<Type, Type> types: zip(walkTypeDependencies(declaration), walkTypeDependencies(usage))) {

            if (GenericParameter.class.isAssignableFrom(types.left.getClass())) {
                pairs.add(new Pair<>(types.left, types.right));
            }
            if (GenericParameter.class.isAssignableFrom(types.right.getClass())) {
                pairs.add(new Pair<>(types.right, types.left));
            }
        }
        return pairs;
    }






}
