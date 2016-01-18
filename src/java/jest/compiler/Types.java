package jest.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import jest.Utils.Pair;

import static jest.Utils.asType;
import static jest.Utils.enumerate;
import static jest.Utils.getAllPairs;
import static jest.Utils.zip;


public class Types {

    /**
     * A type must declare its fully-qualified name and
     * must be able to determine if it equals another type
     * (this is EXACT equality) and must also expose if it
     * implements another type (this is interface implementation
     * and possibly invariance/covariance).
     */
    public interface Type {
        String getName();
        Boolean implementsType(Type type);
        boolean equals(Object other);
        int hashCode();
    }


    public static class SimpleType implements Type {

        final String name;

        public SimpleType(String name) {
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
            for (SimpleType otherSimpleType: asType(other, SimpleType.class)) {
                return otherSimpleType.name.equals(this.name);
            }
            return false;

/*
            if (!SimpleType.class.isAssignableFrom(other.getClass())) {
                return false;
            } else {
                Type otherType = (Type) other;
                return Objects.equals(otherType.getName(), getName());
            }
            */
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getName());
        }
    }


    /**
     * A specific implementation of a generic (parametrized?) type
     */
    public static class GenericType implements Type {

        final String name;

        final List<Type> typeParameters;

        public GenericType(String name, List<Type> typeParameters) {
            this.name = name;
            this.typeParameters = ImmutableList.copyOf(typeParameters);
        }

        public GenericType(String name, Type...typeParameters) {
            this.name = name;
            this.typeParameters = ImmutableList.copyOf(typeParameters);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Boolean implementsType(Type other) {

            if (!GenericType.class.isAssignableFrom(other.getClass())) {
                return false;
            }

            GenericType genericOther = (GenericType) other;

            if (!genericOther.name.equals(this.name)) {
                return false;
            }

            if (genericOther.typeParameters.size() != this.typeParameters.size()) {
                return false;
            }

            for (Pair<Type, Type> types: zip(this.typeParameters, genericOther.typeParameters)) {
                // Invariance?  Covariance?
                if (!types.right.implementsType(types.left)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean equals(Object other) {
            for (GenericType otherGenericType: asType(other, GenericType.class)) {
                return otherGenericType.name.equals(this.name) &&
                    otherGenericType.typeParameters.equals(this.typeParameters);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getName());
        }

    }


    /**
     * A data structure to hold a "Type" in a function signature
     * that is marked as generic (ie it holds "T" or "U", etc)
     */
    public static class GenericParameter implements Type {

        final String name;

        public GenericParameter(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Boolean implementsType(Type other) {
            for (GenericParameter otherGenericParameter: asType(other, GenericParameter.class)) {
                return otherGenericParameter.name.equals(this.name);
            }
            return false;
        }

        @Override
        public boolean equals(Object other) {
            for (GenericParameter otherGenericParameter: asType(other, GenericParameter.class)) {
                return otherGenericParameter.name.equals(this.name);
            }
            return false;
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
        public Map<GenericParameter, List<Integer>> getGenericTypeIndices() {

            Map<GenericParameter, List<Integer>> map = Maps.newHashMap();
            for (Pair<Integer, Type> pair: enumerate(getParameterTypes())) {

                if (pair.right.getClass().isAssignableFrom(GenericParameter.class)) {

                    GenericParameter type = (GenericParameter) pair.right;

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
