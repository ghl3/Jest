package jest.compiler;

import java.util.List;


public class DeclaredTypes {


    public interface Type {
        String getName();
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
        Boolean {
            @Override
            public String getName() {
                return "jest.Boolean";
            }
        }
    }
}
