package jest.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Stream;


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
            return null;
        }

        @Override
        public List<String> getParameterNames() {
            return null;
        }

        @Override
        public List<Type> getParameterTypes() {
            return null;
        }

        @Override
        public Type getReturnType() {
            return null;
        }

        @Override
        public Boolean isGeneric() {
            return true;
        }

        public GenericFunctionSignature(String name, List<String> parameterNames, List<Type> parameterTypes, Type returnType) {
            this.name = name;
            this.parameterNames = ImmutableList.copyOf(parameterNames);
            this.parameterTypes = ImmutableList.copyOf(parameterTypes);
            this.returnType = returnType;
        }
    }
}
