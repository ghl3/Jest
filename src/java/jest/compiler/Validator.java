package jest.compiler;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;
import jest.Utils.Triplet;
import jest.compiler.Errors.ParameterNumberMismatch;
import jest.compiler.Errors.ParameterTypeMismatch;
import jest.compiler.Exceptions.FunctionAlreadyDeclared;
import jest.compiler.Exceptions.UnknownFunction;
import jest.compiler.Exceptions.UnknownVariable;
import jest.compiler.Exceptions.VariableAlreadyDeclared;
import jest.compiler.Exceptions.VariableTypeMismatch;
import jest.Utils.Pair;
import jest.compiler.Contexts.FunctionParameterSummary;
import jest.compiler.Core.PrimitiveType;
import jest.compiler.Core.CollectionType;
import jest.compiler.Types.DeclaredFunctionDeclaration;
import jest.compiler.Types.FunctionDeclaration;
import jest.compiler.Types.FunctionSignature;
import jest.compiler.Types.FunctionType;
import jest.compiler.Types.GenericFunctionDeclaration;
import jest.compiler.Types.GenericParameter;
import jest.compiler.Types.Type;
import jest.grammar.JestBaseListener;
import jest.grammar.JestParser;
import org.antlr.v4.runtime.tree.TerminalNode;

import jest.compiler.Errors.FunctionCallError;

import static jest.Utils.zip;
import static jest.compiler.Exceptions.jestException;
import static jest.Utils.range;
import static jest.compiler.Contexts.getArgumentTypes;
import static jest.compiler.Contexts.getFunctionName;
import static jest.compiler.Contexts.getFunctionDeclaration;
import static jest.compiler.Contexts.getFunctionParameterSummary;
import static jest.compiler.Contexts.getGenericParameters;
import static jest.compiler.Contexts.getMethodSignature;
import static jest.compiler.Contexts.getType;
import static jest.compiler.Generics.checkGenericFunctionCall;



public class Validator extends JestBaseListener {

    private final Stack<Scope> scopes;

    public Scope currentScope() {
        return scopes.peek();
    }

    public Validator() {
        scopes = new Stack<Scope>();

        // Create the global scope
        scopes.push(new Scope(null));

        for (Entry<String, FunctionDeclaration> entry: Core.coreFunctions.entrySet()) {
            scopes.peek().addFunction(entry.getKey(), entry.getValue()); //, null);

        }

        // Add core functions to the scope
        for (String func: Core.clojureCore) {
            // TODO: Add signature for core functions
            if (!Core.coreFunctions.containsKey(func)) {
                scopes.peek().addFunction(func, null);
            }
        }

        // Add built-in types to the scope
        for (PrimitiveType type: PrimitiveType.values()) {
            scopes.peek().addType(type.getName(), type);
        }

        for (CollectionType type: CollectionType.values()) {
            scopes.peek().addType(type.getName(), type);
        }

        // The above scope is the "core" scope
        // We create a sub-scope of this where the
        // user places their variables and functions
        Scope userScope = createNewScope(scopes);
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
        }

        Scope outsideOfFunctionScope = currentScope();

        // Create a new scope for the function signature and add generic
        // types to it.  This allows us to think about generic types
        // in the function declaration as any other type, thus
        // simplifying the code downstream
        Scope functionSignatureScope = createNewScope(scopes);
        for (GenericParameter genericParameter: getGenericParameters(functionName, ctx)) {
            functionSignatureScope.addType(genericParameter.getName(), genericParameter);
        }

        // TODO: Remove the "else" when we require all functions to be annotated
        // Note that we add the function declaration to the outer scope
        // This ensures that the function name is available to other expressions
        // in that scope.
        // Note that it is important that the functionSignatureScope be created
        // above so that the generic parameters can be available in scope
        // in the "getFunctionDeclaration" below but also so they don't leak
        // out of the function declaration
        if (ctx.typeAnnotation() != null) {
            FunctionDeclaration sig = getFunctionDeclaration(currentScope(), ctx);
            outsideOfFunctionScope.addFunction(functionName, sig);
        } else {
            // TODO: Infer the function return type...
            outsideOfFunctionScope.addFunction(functionName, null);
        }

        Scope functionBodyScope = createNewScope(scopes);

        // We now have to add all variables appearing in the argument list of the
        // function to the function body's scope.
        // There are 3 types of variables appearing in the argument list:
        // - Standard variable parameters, added to variables in scope
        // - Function parameters, added to functions in scope (ie if a function takes a function)
        // - Generic parameter types in scope, added to the list of types in scope
        FunctionParameterSummary parameterSummary = getFunctionParameterSummary(currentScope(), ctx);

        for (Pair<String, Type> variableType: parameterSummary.variableTypes) {
            functionBodyScope.addVariable(variableType.left, variableType.right);
        }
        for (Pair<String, FunctionType> variableType: parameterSummary.functionTypes) {
            FunctionSignature signature = variableType.right.signature;
            List<String> functionNames = range(signature.parameterTypes.size())
                .map(String::valueOf)
                .collect(Collectors.toList());

            FunctionDeclaration declaration = new DeclaredFunctionDeclaration(variableType.left, functionNames,
                signature.parameterTypes, signature.returnType);

            functionBodyScope.addFunction(variableType.left, declaration);
        }

        // TODO: Remove this when we require all functuions to be annotated
        JestParser.FunctionDefParamsContext params = ctx.functionDefParams();
        for (TerminalNode node: params.ID()) {
            String name = node.getText();
            if (!functionBodyScope.isVariableOrFunctionInScope(name)) {
                currentScope().addVariable(name, null);
            }
        }
    }


    @Override
    public void exitFunctionDef(JestParser.FunctionDefContext ctx) {
        // Drop both the function body scope
        // and the parameter scope
        dropCurrentScope(scopes);
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
            FunctionDeclaration sig = getMethodSignature(ctx);
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

        Type expressionType;
        try {
            expressionType = getType(currentScope(), ctx.expression());
        } catch (Exception e) {
            System.out.println("Cannot determine expression type");
            expressionType = null;
        }

        if (ctx.typeAnnotation() != null) {
            Type annotatedType = getType(currentScope(), ctx.typeAnnotation());

            if (!expressionType.implementsType(annotatedType)) {
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

        if (!currentScope().getFunctionDeclaration(functionName).isPresent()) {
            System.out.println(String.format("No present signature for function: %s", functionName));
        } else if (currentScope().getFunctionDeclaration(functionName).get().isGeneric()) {

            List<Type> argumentTypes = getArgumentTypes(currentScope(), ctx);

            GenericFunctionDeclaration typeDeclaration = (GenericFunctionDeclaration) currentScope()
                .getFunctionDeclaration(functionName)
                .orElseThrow(jestException(ctx));

            Optional<FunctionCallError> error = checkGenericFunctionCall(typeDeclaration, argumentTypes);

            if (error.isPresent()) {
                throw error.get().createException(functionName, ctx);
            }

        } else {

            List<Type> argumentTypes = getArgumentTypes(currentScope(), ctx);

            FunctionDeclaration typeDeclaration = currentScope()
                .getFunctionDeclaration(functionName)
                .orElseThrow(jestException(ctx));

            Optional<FunctionCallError> error = checkFunctionCall(typeDeclaration, argumentTypes);

            if (error.isPresent()) {
                throw error.get().createException(functionName, ctx);
            }
        }
    }


    public static Optional<FunctionCallError> checkFunctionCall(FunctionDeclaration typeDeclaration, List<Type> argumentTypes) {

        if (argumentTypes.size() != typeDeclaration.getSignature().parameterTypes.size()) {
            return Optional.of(new ParameterNumberMismatch(typeDeclaration.getSignature().parameterTypes.size(), argumentTypes.size()));
        }

        for (Triplet<String, Type, Type> types : zip(typeDeclaration.getParameterNames(), typeDeclaration.getSignature().parameterTypes, argumentTypes)) {
            if (!types._2.implementsType(types._1)) {
                return Optional.of(new ParameterTypeMismatch(types._0, types._1, types._2));
            }
        }
        return Optional.empty();
    }

}
