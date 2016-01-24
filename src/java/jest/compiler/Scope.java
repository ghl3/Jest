package jest.compiler;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import jest.compiler.Types.FunctionDeclaration;
import jest.compiler.Types.FunctionSignature;
import jest.compiler.Types.Type;


public class Scope {

    // TODO: Create a class called ImmutableScope
    // Leverage that to ensure that scopes are only
    // edited in the "enter" and "exit" functions
    // in the Validator, and dependent functions that
    // need the scope are read-only

    final Scope parent;

    final Map<String, Type> variables = new HashMap<String, Type>();

    final Map<String, FunctionDeclaration> functions = new HashMap<String, FunctionDeclaration>();

    final Map<String, Type> types = new HashMap<String, Type>();

    public Scope() {
        this.parent = null;
    }

    public Scope(Scope parent) {
        this.parent = parent;
    }

    boolean isGlobalScope() {
        return parent == null;
    }


    // Variables

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
        if (this.variables.containsKey(name)) {
            return Optional.ofNullable(this.variables.get(name));
        } else if (parent != null) {
            return this.parent.getVariableType(name);
        } else {
            return Optional.empty();
        }
    }

    // Types

    public boolean isTypeInCurrentScope(String varName) {
        if (types.containsKey(varName)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isTypeInScope(String varName) {
        if (isTypeInCurrentScope(varName)) {
            return true;
        }

        if (parent == null) {
            return false;
        } else {
            return parent.isTypeInScope(varName);
        }
    }

    void addType(String varName, Type type) {
        types.put(varName, type);
    }


    public Optional<Type> getType(String name) {
        if (this.types.containsKey(name)) {
            return Optional.ofNullable(this.types.get(name));
        } else if (parent != null) {
            return this.parent.getType(name);
        } else {
            return Optional.empty();
        }
    }

    // Functions

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

    void addFunction(String varName, FunctionDeclaration declaration) {
        functions.put(varName, declaration);
    }

    /*
    void addFunction(String varName, FunctionDeclaration declaration) {
        functions.put(varName, declaration.signature);
    }
*/

    public Optional<FunctionDeclaration> getFunctionDeclaration(String name) {
        if (this.functions.containsKey(name)) {
            return Optional.ofNullable(this.functions.get(name));
        } else if (parent != null) {
            return this.parent.getFunctionDeclaration(name);
        } else {
            return Optional.empty();
        }
    }

    public boolean isVariableOrFunctionInScope(String name) {
        return isVariableInScope(name) || isFunctionInScope(name);
    }
}
