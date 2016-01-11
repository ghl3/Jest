package jest.compiler;

import java.util.List;


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
        List<Type> getParameterTypes();
        Type getReturnType();
        String getName();
    }


    enum BuiltInTypes implements Type {
        String {
            @Override
            public String getName() {
                return "jest.String";
            }
        },
        Symbol {
            @Override
            public String getName() {
                return "jest.Symbol";
            }
        },
        Number {
            @Override
            public String getName() {
                return "jest.Number";
            }
        },
        Map {
            @Override
            public String getName() {
                return "jest.Map";
            }
        },
        List {
            @Override
            public String getName() {
                return "jest.List";
            }
        },
        Vector {
            @Override
            public String getName() {
                return "jest.Vector";
            }
        },
        Boolean {
            @Override
            public String getName() {
                return "jest.Boolean";
            }
        },
        Nil {
            @Override
            public String getName() {
                return "jest.Nil";
            }
        }
    }
}