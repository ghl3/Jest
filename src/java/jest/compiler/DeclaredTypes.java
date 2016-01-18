package jest.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jest.Utils.Pair;

import static jest.Utils.enumerate;
import static jest.Utils.getAllPairs;


public class DeclaredTypes {


    public interface Type {
        String getName();
        Boolean implementsType(Type type);
    }

    public static boolean typesEqual(Type left, Type right) {
        return left.getName().equals(right.getName());
    }

    public static class UserType implements Type {

        final String name;

        public UserType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Boolean implementsType(Type type) {
            return this.getName().equals(type.getName());
        }

        @Override
        public boolean equals(Object other) {
            if (!Type.class.isAssignableFrom(other.getClass())) {
                return false;
            } else {
                Type otherType = (Type) other;
                return Objects.equals(otherType.getName(), getName());
            }
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getName());
        }
    }

    public static class GenericType implements Type {

        final String name;

        public GenericType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Boolean implementsType(Type type) {
            if (!type.getClass().isAssignableFrom(GenericType.class)) {
                return false;
            } else {
                return this.getName().equals(type.getName());
            }
        }

        @Override
        public boolean equals(Object other) {
            if (!Type.class.isAssignableFrom(other.getClass())) {
                return false;
            } else {
                Type otherType = (Type) other;
                return Objects.equals(otherType.getName(), getName());
            }
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getName());
        }
    }

    public interface FunctionSignature {
        String getName();
        List<String> getParameterNames();
        List<Type> getParameterTypes();
        Type getReturnType();
        Boolean isGeneric();

        //List<Type> matches
    }


    public static class DeclaredFunctionSignature implements FunctionSignature {

        final String name;

        final List<String> parameterNames;

        final List<Type> parameterTypes;

        final Type returnType;

        public DeclaredFunctionSignature(String name, List<String> parameterNames, List<Type> parameterTypes, Type returnType) {
            this.name = name;
            this.parameterNames = ImmutableList.copyOf(parameterNames);
            this.parameterTypes = ImmutableList.copyOf(parameterTypes);
            this.returnType = returnType;
        }

        @Override
        public List<String> getParameterNames() {
            return ImmutableList.copyOf(parameterNames);
        }

        @Override
        public List<Type> getParameterTypes() {
            return ImmutableList.copyOf(parameterTypes);
        }

        @Override
        public Type getReturnType() {
            return returnType;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Boolean isGeneric() {
            return false;
        }
    }


    public static class GenericFunctionSignature implements FunctionSignature {

        final String name;

        final List<String> parameterNames;

        final List<Type> parameterTypes;

        final Type returnType;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<String> getParameterNames() {
            return parameterNames;
        }

        @Override
        public List<Type> getParameterTypes() {
            return parameterTypes;
        }

        @Override
        public Type getReturnType() {
            return returnType;
        }

        @Override
        public Boolean isGeneric() {
            return true;
        }

        /**
         * Return a Map of a generic type to
         * the list of parameter indices that have
         * that generic type declared.
         *
         * So, for example, if we do:
         *
         * defn <T, U> foo(t1: T, t2: T, u: U, x: String)
         *
         * we return:
         *
         * {GenericType(T): [0, 1],
         *  GenericType(U): [2]}
         *
         * @return
         */
        public Map<GenericType, List<Integer>> getGenericTypeIndices() {

            Map<GenericType, List<Integer>> map = Maps.newHashMap();
            for (Pair<Integer, Type> pair: enumerate(getParameterTypes())) {

                if (pair.right.getClass().isAssignableFrom(GenericType.class)) {

                    GenericType type = (GenericType) pair.right;

                    if (!map.containsKey(type)) {
                        map.put(type, Lists.<Integer>newArrayList());
                    }

                    map.get(type).add(pair.left);
                }
            }

            return map;
        }

        public GenericFunctionSignature(String name, List<String> parameterNames, List<Type> parameterTypes, Type returnType) {
            this.name = name;
            this.parameterNames = ImmutableList.copyOf(parameterNames);
            this.parameterTypes = ImmutableList.copyOf(parameterTypes);
            this.returnType = returnType;
        }



        public static Boolean typesConsistent(Iterable<Type> types) {
            for (Pair<Type, Type> typePair: getAllPairs(types)) {
                if (! typePair.left.implementsType(typePair.right) || typePair.right.implementsType(typePair.left)) {
                    return false;
                }
            }
            return true;
        }
    }

}
