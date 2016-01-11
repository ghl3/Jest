package jest.compiler;

import com.google.common.base.Optional;
import java.util.Map;
import java.util.HashMap;
import jdk.nashorn.internal.runtime.options.Option;
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


    public Optional<Type> getVariableType(String name) {
        return Optional.fromNullable(this.variables.get(name));
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


    public Optional<FunctionSignature> getFunctionSignature(String name) {
        return Optional.fromNullable(this.functions.get(name));
    }

    public boolean isVariableOrFunctionInScope(String name) {
        return isVariableInScope(name) || isFunctionInScope(name);
    }
}
