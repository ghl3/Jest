package jest.compiler;

import java.util.Stack;

import jest.compiler.Scope;

import jest.grammar.JestBaseListener;

import jest.grammar.JestParser;
    
import org.antlr.v4.runtime.Token;

public class Validator extends JestBaseListener {

    private final Stack<Scope> scopes;

    public Validator() {
        scopes = new Stack<Scope>();
        // Create the global scope
        scopes.push(new Scope(null));
    }

    public class AlreadyDeclared extends RuntimeException {
        public AlreadyDeclared(Token token) {
            super(String.format("Error - Line %s: Already declared variable with name: %s",
                                token.getLine(), token.getText()));
        }
    }

    /**
       Create a new scope and return that scope
     */
    public static Scope createNewScope(Stack<Scope> scopes) {
         Scope outerScope = scopes.peek();
         Scope newScope = new Scope(outerScope);
         scopes.push(newScope);
         return newScope;
    }

    /**
       Drop the highest scope and return the
       new current scope
     */
    public static Scope dropCurrentScope(Stack<Scope> scopes) {
        scopes.pop();
        return scopes.peek();
    }

    @Override
    public void enterFunction_def(JestParser.Function_defContext ctx) {
        if (scopes.peek().isInScope(ctx.name.getText())) {
            throw new AlreadyDeclared(ctx.name);
        } else {
            scopes.peek().addToScope(ctx.name.getText(), ctx);
        }

        Scope functionScope = createNewScope(scopes);
    }

    @Override
    public void exitFunction_def(JestParser.Function_defContext ctx) {
        dropCurrentScope(scopes);
    }

    @Override
    public void enterVal_assignment(JestParser.Val_assignmentContext ctx) {
        if (scopes.peek().isInScope(ctx.name.getText())) {
            throw new AlreadyDeclared(ctx.name);
        } else {
            scopes.peek().addToScope(ctx.name.getText(), ctx);
        }
    }
}
