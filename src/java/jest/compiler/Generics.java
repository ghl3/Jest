package jest.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import jest.Utils.Pair;
import jest.Utils.Triplet;
import jest.compiler.Types.GenericFunctionDeclaration;
import jest.compiler.Types.GenericParameter;
import jest.compiler.Types.Type;

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



    interface GenericFunctionCallResult {
        Boolean passesTypeChecks();
    };

    public static class GenericFunctionCallMatch implements GenericFunctionCallResult {
        @Override
        public Boolean passesTypeChecks() {
            return true;
        }
    }


    public static class ArgumentNumberMismatch implements GenericFunctionCallResult {
        @Override
        public Boolean passesTypeChecks() {
            return false;
        }
    }

    public static class GenericTypeParameterMismatch implements GenericFunctionCallResult {
        @Override
        public Boolean passesTypeChecks() {
            return false;
        }
    }



    public static GenericFunctionCallResult checkGenericFunctionCall(GenericFunctionDeclaration typeDeclaration,
                                                                     List<Type> argumentTypes) {

        if (argumentTypes.size() != typeDeclaration.getSignature().parameterTypes.size()) {
            return new ArgumentNumberMismatch();
            //throw new WrongNumberOfFunctionParameters(ctx,
//                typeDeclaration.getSignature().parameterTypes.size(),
//                argumentTypes.size());
        }

        // Check the non=genric parameters
        for (Triplet<String, Type, Type> types : zip(typeDeclaration.getParameterNames(), typeDeclaration.getSignature().parameterTypes, argumentTypes)) {
            if (types._1.isGeneric()) {
                continue;
            }
            if (!types._2.implementsType(types._1)) { //typesEqual(types._1, types._2)) {
                return new GenericTypeParameterMismatch();
                //throw new FunctionParameterTypeMismatch(ctx, functionName, types._0, types._1, types._2);
            }
        }

        for (GenericArguments arguments: Generics.getGenericArguments(typeDeclaration, argumentTypes)) {

            GenericParameter param = arguments.parameter;

            Set<Type> allUsageTypes = Sets.newHashSet();

            for (Pair<Type, Type> types: zip(arguments.typeDeclarations, arguments.argumentTypes)) {

                Type declaration = types.left;
                Type usage = types.right;

                try {
                    allUsageTypes.addAll(getTypesOfGenericParameter(param, declaration, usage));
                } catch (GenericMismatch genericMismatch) {
                    return new GenericTypeParameterMismatch();
                    // TODO: Make this a different exception
                    // Properly propagate the GenericMismatch exception's information upward
                    //throw new InconsistentGenericTypes(ctx, param, ImmutableList.of(declaration, usage)); //entry.getKey(), types);
                }
            }

            if (!typesConsistent(allUsageTypes)) {
                return new GenericTypeParameterMismatch();
                //throw new InconsistentGenericTypes(ctx, param, allUsageTypes); //
            }
        }
        return new GenericFunctionCallMatch();
    }


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
        } else if (declaration.getDependentTypes().size() == 0) {
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

            if (!usage.getBaseType().implementsType(declaration.getBaseType())) {
                throw new GenericMismatch();
            }

            List<Type> parameterTypesInUsage = Lists.newArrayList();

            for (Pair<Type, Type> types: zip(declaration.getDependentTypes(), usage.getDependentTypes())) {
                parameterTypesInUsage.addAll(getTypesOfGenericParameter(parameter, types.left, types.right));
            }

            return ImmutableList.copyOf(parameterTypesInUsage);
        }
    }
}
