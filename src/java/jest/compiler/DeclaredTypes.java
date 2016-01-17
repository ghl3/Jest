package jest.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Stream;


public class DeclaredTypes {


    public interface Type {
        String getName();
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
    }


    public interface FunctionSignature {
        List<String> getParameterNames();
        List<Type> getParameterTypes();
        Type getReturnType();
        String getName();
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
    }

}
