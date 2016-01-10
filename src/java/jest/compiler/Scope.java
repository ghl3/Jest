package jest.compiler;

import java.util.Map;
import java.util.HashMap;
import jest.compiler.DeclaredTypes.FunctionSignature;
import jest.compiler.DeclaredTypes.Type;


public class Scope {

    final Scope parent;

    final Map<String, Type> variables = new HashMap<String, Type>();

    final Map<String, FunctionSignature> functions = new HashMap<String, FunctionSignature>();

    public Scope() {
        this.parent = null;
    }

    public Scope(Scope parent) {
        this.parent = parent;
    }

    boolean isGlobalScope() {
        return parent == null;
    }

    public boolean isVariableInCurrentScope(String varName) {
        if (variables.containsKey(varName)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isVariableInScope(String varName) {
        if (isVariableInCurrentScope(varName)) {
            return true;
        }

        if (parent == null) {
            return false;
        } else {
            return parent.isVariableInScope(varName);
        }
    }

    void addVariable(String varName, Type type) {
        variables.put(varName, type);
    }

    ///////

    public boolean isFunctionInCurrentScope(String varName) {
        if (functions.containsKey(varName)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isFunctionInScope(String varName) {
        if (isFunctionInCurrentScope(varName)) {
            return true;
        }

        if (parent == null) {
            return false;
        } else {
            return parent.isFunctionInScope(varName);
        }
    }

    void addFunction(String varName, FunctionSignature signature) {
        functions.put(varName, signature);
    }


    public boolean isVariableOrFunctionInScope(String name) {
        return isVariableInScope(name) || isFunctionInScope(name);
    }
}
