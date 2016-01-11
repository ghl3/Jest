package jest.compiler;

import java.util.Stack;

import jest.Exception.NotYetImplemented;
import jest.compiler.DeclaredTypes.FunctionSignature;
import jest.compiler.DeclaredTypes.Type;
import jest.grammar.JestBaseListener;
import jest.grammar.JestParser;

import jest.grammar.JestParser.ExpressionContext;
import org.antlr.v4.runtime.Token;

import org.antlr.v4.runtime.tree.TerminalNode;


public class Validator extends JestBaseListener {

    private final Stack<Scope> scopes;

    public Scope currentScope() {
        return scopes.peek();
    }


    public static Type getExpressionType(Scope scope, ExpressionContext expression) {
        return new ExpressionEvaluator(scope).visit(expression);
    }

    //protected ExpressionTypeVisitor expressionEvaluator = new ExpressionTypeVisitor(scopes);


    public Validator() {
        scopes = new Stack<Scope>();

        // Create the global scope
        scopes.push(new Scope(null));

        // Add core functions to the scope
        for (String func: Core.clojureCore) {
            // TODO: Add signature for core functions
            scopes.peek().addFunction(func, null);
        }
    }

    public class ValidationError extends RuntimeException {
        public ValidationError(String message) {
            super(message);
        }
    }

    public class AlreadyDeclared extends ValidationError {
        public AlreadyDeclared(Token token) {
            super(String.format("Error - Line %s: Already declared variable with name: %s",
                                token.getLine(), token.getText()));
        }
    }

    public class NotDeclared extends ValidationError {
        public NotDeclared(TerminalNode token) {
            super(String.format("Error - Line %s: Attempting to use variable %s that has not been declared",
                                token.getSymbol().getLine(), token.getText()));
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
        if (scopes.peek().isGlobalScope()) {
            return scopes.peek();
        } else {
            scopes.pop();
            return scopes.peek();
        }
    }


    public static FunctionSignature getFunctionSignature(JestParser.FunctionDefContext function) {
        return null;
    }


    @Override
    public void enterFunctionDef(JestParser.FunctionDefContext ctx) {
        if (currentScope().isFunctionInCurrentScope(ctx.name.getText())) {
            throw new AlreadyDeclared(ctx.name);
        } else {
            FunctionSignature sig = getFunctionSignature(ctx);
            currentScope().addFunction(ctx.name.getText(), sig);
        }

        Scope functionBodyScope = createNewScope(scopes);

        // TODO: Include the function parameters in the current scope
        JestParser.FunctionDefParamsContext params = ctx.functionDefParams();

        for (TerminalNode node: params.ID()) {
            // TODO: Add the signature types the scope
            currentScope().addVariable(node.getText(), null);
        }

    }


    @Override
    public void exitFunctionDef(JestParser.FunctionDefContext ctx) {
        dropCurrentScope(scopes);
    }


    @Override
    public void enterForLoop(JestParser.ForLoopContext ctx) {
        Scope loopScope = createNewScope(scopes);
        // TODO: Include the loop parameters in the current scope

        for (TerminalNode node: ctx.ID()) {
            // TODO: Get type signature for loop parameters
            currentScope().addVariable(node.getText(), null);
        }
    }

    @Override
    public void exitForLoop(JestParser.ForLoopContext ctx) {
        dropCurrentScope(scopes);
    }


    public static FunctionSignature getMethodSignature(JestParser.MethodDefContext function) {
        return null;
    }


    @Override
    public void enterMethodDef(JestParser.MethodDefContext ctx) {
        if (currentScope().isFunctionInCurrentScope(ctx.name.getText())) {
            throw new AlreadyDeclared(ctx.name);
        } else {
            FunctionSignature sig = getMethodSignature(ctx);
            currentScope().addFunction(ctx.name.getText(), sig);
        }

        Scope functionBodyScope = createNewScope(scopes);

        // TODO: Include the function parameters in the current scope
        JestParser.FunctionDefParamsContext params = ctx.functionDefParams();

        for (TerminalNode node: params.ID()) {
            // TODO: Add the parameter type
            currentScope().addVariable(node.getText(), null);
        }
    }


    @Override
    public void exitMethodDef(JestParser.MethodDefContext ctx) {
        dropCurrentScope(scopes);
    }


    @Override
    public void enterBlock(JestParser.BlockContext ctx) {
        Scope loopScope = createNewScope(scopes);
    }


    @Override
    public void exitBlock(JestParser.BlockContext ctx) {
        dropCurrentScope(scopes);
    }


    public void enterConditional(JestParser.ConditionalContext ctx) {
        Scope loopScope = createNewScope(scopes);
    }


    public void exitConditional(JestParser.ConditionalContext ctx) {
        dropCurrentScope(scopes);
    }


    public void enterLambda(JestParser.LambdaContext ctx) {
        Scope scope = createNewScope(scopes);
        JestParser.FunctionDefParamsContext params = ctx.functionDefParams();

        for (TerminalNode node: params.ID()) {
            currentScope().addVariable(node.getText(), null);
        }
    }


    public void exitLambda(JestParser.LambdaContext ctx) {
        dropCurrentScope(scopes);
    }


    @Override
    public void enterVarScope(JestParser.VarScopeContext ctx) {
        for (TerminalNode node: ctx.ID()) {
            String name = node.getText();
            if (currentScope().isVariableInCurrentScope(name)) {
                throw new AlreadyDeclared(node.getSymbol());
            } else {
                currentScope().addVariable(node.getText(), null);
            }
        }
    }

    @Override
    public void exitVarScope(JestParser.VarScopeContext ctx) {
        dropCurrentScope(scopes);
    }


    @Override
    public void enterDefAssignment(JestParser.DefAssignmentContext ctx) {
        if (currentScope().isVariableInCurrentScope(ctx.name.getText())) {
            throw new AlreadyDeclared(ctx.name);
        }

        Type type = getExpressionType(currentScope(), ctx.expression());
        currentScope().addVariable(ctx.name.getText(), type);
    }

    @Override
    public void enterExpressionAtom(JestParser.ExpressionAtomContext ctx) {

        // If the expression is a variable, ensure the variable
        // has been declared
        if (ctx.ID() != null) {
            if (!currentScope().isVariableOrFunctionInScope(ctx.ID().getText())) {
                throw new NotDeclared(ctx.ID());
            }
        }
    }

    @Override
    public void enterFunctionCall(JestParser.FunctionCallContext ctx) {
        if (!currentScope().isVariableOrFunctionInScope(ctx.ID().getText())) {
            throw new NotDeclared(ctx.ID());
        }
    }
}
