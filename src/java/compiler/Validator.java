package jest.compiler;

import java.util.Stack;

import jest.compiler.Scope;

import jest.grammar.JestBaseListener;

public class Validator extends JestBaseListener {

    private final Stack<Scope> scopes;

    public Validator() {
        scopes = new Stack<Scope>();
        // Create the global scope
        scopes.push(new Scope(null));
    }
}
