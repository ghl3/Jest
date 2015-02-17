package jest.compiler;


import java.util.Map;
import java.util.HashMap;

class Scope {

    final Scope parent;

    final Map<String, Object> variables = new HashMap<String, Object>();

    public Scope() {
        this.parent = null;
    }

    public Scope(Scope parent) {
        this.parent = parent;
    }

    boolean isInCurrentScope(String varName) {
        if (variables.containsKey(varName)) {
            return true;
        } else {
            return false;
        }
    }

    boolean isInScope(String varName) {
        if (isInCurrentScope(varName)) {
            return true;
        }

        if (parent == null) {
            return false;
        } else {
            return parent.isInScope(varName);
        }
    }

    void addToScope(String varName, Object obj) {
        variables.put(varName, obj);
    }

    boolean isGlobalScope() {
        return parent == null;
    }
    
}
