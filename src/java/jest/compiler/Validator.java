package jest.compiler;

import java.util.List;
import java.util.Stack;
import jest.Exception.FunctionAlreadyDeclared;
import jest.Exception.FunctionParameterTypeMismatch;
import jest.Exception.UnknownFunction;
import jest.Exception.UnknownVariable;
import jest.Exception.VariableAlreadyDeclared;
import jest.Exception.VariableTypeMismatch;
import jest.Exception.WrongNumberOfFunctionParameters;
import jest.Utils.Triplet;
import jest.compiler.DeclaredTypes.FunctionSignature;
import jest.compiler.DeclaredTypes.Type;
import jest.grammar.JestBaseListener;
import jest.grammar.JestParser;
import org.antlr.v4.runtime.tree.TerminalNode;

import static jest.Exception.jestException;
import static jest.Utils.zip;
import static jest.compiler.Contexts.getArgumentTypes;
import static jest.compiler.Contexts.getFunctionName;
import static jest.compiler.Contexts.getFunctionSignature;
import static jest.compiler.Contexts.getMethodSignature;
import static jest.compiler.Contexts.getType;
import static jest.compiler.DeclaredTypes.typesEqual;


public class Validator extends JestBaseListener {

    private final Stack<Scope> scopes;

    public Scope currentScope() {
        return scopes.peek();
    }

    public Validator() {
        scopes = new Stack<Scope>();

        // Create the global scope
        scopes.push(new Scope(null));

        // Add core functions to the scope
        for (String func: Core.clojureCore) {
            // TODO: Add signature for core functions
            scopes.peek().addFunction(func, null);
        }

        // Add built-in types to the scope
        for (Type type: Core.BuiltInTypes.values()) {
            scopes.peek().addType(type.getName(), type);
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

    @Override
    public void enterFunctionDef(JestParser.FunctionDefContext ctx) {

        String functionName = getFunctionName(ctx);

        if (currentScope().isFunctionInCurrentScope(ctx.name.getText())) {
            throw new FunctionAlreadyDeclared(ctx, functionName);
        } else {
            // TODO: Remove the "else" when we require all functions to be annotated
            if (ctx.typeAnnotation() != null) {
                FunctionSignature sig = getFunctionSignature(currentScope(), ctx);
                currentScope().addFunction(functionName, sig);
            } else {
                currentScope().addFunction(functionName, null);
            }
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


    @Override
    public void enterMethodDef(JestParser.MethodDefContext ctx) {

        String methodName = ctx.name.getText();

        if (currentScope().isFunctionInCurrentScope(ctx.name.getText())) {
            throw new FunctionAlreadyDeclared(ctx, methodName);
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
                throw new VariableAlreadyDeclared(ctx, name);
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

        String name = ctx.name.getText();

        if (currentScope().isVariableInCurrentScope(name)) {
            throw new VariableAlreadyDeclared(ctx, name);
        }

        Type expressionType = getType(currentScope(), ctx.expression());

        if (ctx.typeAnnotation() != null) {
            Type annotatedType = getType(ctx.typeAnnotation());

            if (!typesEqual(annotatedType, expressionType)) {
                throw new VariableTypeMismatch(ctx, name, annotatedType, expressionType);
            }
        }

        // TODO: This should be the annotated type
        currentScope().addVariable(ctx.name.getText(), expressionType);
    }

    @Override
    public void enterExpressionAtom(JestParser.ExpressionAtomContext ctx) {

        // If the expression is a variable, ensure the variable
        // has been declared

        if (ctx.ID() != null) {
            String name = ctx.ID().getText();
            if (!currentScope().isVariableOrFunctionInScope(name)) {
                throw new UnknownVariable(ctx, name);
            }
        }
    }

    @Override
    public void enterFunctionCall(JestParser.FunctionCallContext ctx) {

        String functionName = getFunctionName(ctx);

        if (!currentScope().isFunctionInScope(functionName)) {
            throw new UnknownFunction(ctx, functionName);
        }

        // Get the types of the parameters being called
        List<Type> argumentTypes = getArgumentTypes(currentScope(), ctx);

        if (!currentScope().getFunctionSignature(functionName).isPresent()) {
            System.out.println(String.format("No present signature for function: %s", functionName));
        } else {

            FunctionSignature typeSignature = currentScope()
                .getFunctionSignature(functionName)
                .orElseThrow(jestException(ctx));

            if (argumentTypes.size() != typeSignature.getParameterTypes().size()) {
                throw new WrongNumberOfFunctionParameters(ctx,
                    typeSignature.getParameterTypes().size(),
                    argumentTypes.size());
            }

            for (Triplet<String, Type, Type> types : zip(typeSignature.getParameterNames(), typeSignature.getParameterTypes(), argumentTypes)) {
                if (!typesEqual(types._1, types._2)) {
                    throw new FunctionParameterTypeMismatch(ctx, functionName, types._0, types._1, types._2);
                }
            }
        }
    }
}
