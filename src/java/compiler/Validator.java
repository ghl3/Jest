package jest.compiler;

import java.util.Stack;
import jest.compiler.Scope;

import jest.grammar.JestBaseListener;
import jest.grammar.JestParser;

import org.antlr.v4.runtime.Token;


public class Validator extends JestBaseListener {

    private final Stack<Scope> scopes;

    public Scope currentScope() {
        return scopes.peek();
    }

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

    public class NotDeclared extends RuntimeException {
        public NotDeclared(Token token) {
            super(String.format("Error - Line %s: Attempting to use variable %s that has not been declared",
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
        if (currentScope().isInCurrentScope(ctx.name.getText())) {
            throw new AlreadyDeclared(ctx.name);
        } else {
            currentScope().addToScope(ctx.name.getText(), ctx);
        }

        Scope functionScope = createNewScope(scopes);

        // TODO: Include the function parameters in the current scope
        JestParser.Method_paramsContext method_params = ctx.method_params;
    }


    @Override
    public void exitFunction_def(JestParser.Function_defContext ctx) {
        dropCurrentScope(scopes);
    }


    @Override
    public void enterFor_loop(JestParser.For_loopContext ctx) {
        Scope loopScope = createNewScope(scopes);
        // TODO: Include the loop parameters in the current scope
    }

    @Override
    public void exitFor_loop(JestParser.For_loopContext ctx) {
        dropCurrentScope(scopes);
    }


    @Override
    public void enterLet_statement(JestParser.Let_statementContext ctx) {
        Scope loopScope = createNewScope(scopes);
    }

    @Override
    public void exitLet_statement(JestParser.Let_statementContext ctx) {
        dropCurrentScope(scopes);
    }


    @Override
    public void enterMethod_def(JestParser.Method_defContext ctx) {
        Scope loopScope = createNewScope(scopes);
    }

    @Override
    public void exitMethod_def(JestParser.Method_defContext ctx) {
        dropCurrentScope(scopes);
    }


    // Require variables to be defined

    @Override
    public void enterVal_assignment(JestParser.Val_assignmentContext ctx) {
        if (currentScope().isInCurrentScope(ctx.name.getText())) {
            throw new AlreadyDeclared(ctx.name);
        } else {
            currentScope().addToScope(ctx.name.getText(), ctx);
        }
    }

    @Override
    public void enterExpression_atom(JestParser.Expression_atomContext ctx) {

        // If the expression is a variable, ensure the variable
        // has been declared
        if (ctx.ID != null) {
            if (!currentScope().isInScope(ctx.ID.getText())) {
                throw new NotDeclared(ctx.ID);
            }
        }
        if (ctx.SYMBOL != null) {
            if (!currentScope().isInScope(ctx.SYMBOL.getText())) {
                throw new NotDeclared(ctx.SYMBOL);
            }
        }
    }

    @Override
    public void enterFunction_call(JestParser.Function_callContext ctx) {
        if (!currentScope().isInScope(ctx.ID.getText())) {
            throw new NotDeclared(ctx.ID);
        }
    }
}
