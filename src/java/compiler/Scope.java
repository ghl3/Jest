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

    boolean isInScope(String varName) {
        if (variables.containsKey(varName)) {
            return true;
        }

        if (parent == null) {
            return false;
        } else {
            return parent.isInScope(varName);
        }
    }
}
