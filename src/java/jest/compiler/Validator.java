package jest.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import jest.Exception.FunctionAlreadyDeclared;
import jest.Exception.FunctionParameterTypeMismatch;
import jest.Exception.GenericError;
import jest.Exception.InconsistentGenericTypes;
import jest.Exception.UnknownFunction;
import jest.Exception.UnknownVariable;
import jest.Exception.VariableAlreadyDeclared;
import jest.Exception.VariableTypeMismatch;
import jest.Exception.WrongNumberOfFunctionParameters;
import jest.Utils.Pair;
import jest.Utils.Triplet;
import jest.compiler.Contexts.FunctionParameterSummary;
import jest.compiler.Core.PrimitiveType;
import jest.compiler.Core.CollectionType;
import jest.compiler.Generics.GenericArguments;
import jest.compiler.Generics.GenericFunctionCallResult;
import jest.compiler.Generics.GenericMismatch;
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

import static jest.Exception.jestException;
import static jest.Utils.range;
import static jest.Utils.zip;
import static jest.compiler.Contexts.getArgumentTypes;
import static jest.compiler.Contexts.getFunctionName;
import static jest.compiler.Contexts.getFunctionDeclaration;
import static jest.compiler.Contexts.getFunctionParameterSummary;
import static jest.compiler.Contexts.getGenericParameters;
import static jest.compiler.Contexts.getMethodSignature;
import static jest.compiler.Contexts.getType;
import static jest.compiler.Generics.checkGenericFunctionCall;
import static jest.compiler.Generics.getTypesOfGenericParameter;
import static jest.compiler.Types.GenericFunctionDeclaration.typesConsistent;


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

        // TODO: Remove the "else" when we require all functions to be annotated
        if (ctx.typeAnnotation() != null) {
            FunctionDeclaration sig = getFunctionDeclaration(currentScope(), ctx);
            currentScope().addFunction(functionName, sig);
        } else {
            // TODO: Infer the function return type...
            currentScope().addFunction(functionName, null);
        }

        // TODO: Include the function parameters in the current scope
        JestParser.FunctionDefParamsContext params = ctx.functionDefParams();

        // Create a new scope for the function body
        Scope functionBodyScope = createNewScope(scopes);

        // Add all generic types to the function body scope
        for (String genericType: getGenericParameters(ctx)) {
            functionBodyScope.addType(genericType, new GenericParameter(genericType));
        }

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
        for (Pair<String, GenericParameter> variableType: parameterSummary.genericTypes) {
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
        for (TerminalNode node: params.ID()) {
            String name = node.getText();
            if (!functionBodyScope.isVariableOrFunctionInScope(name)) {
                currentScope().addVariable(name, null);
            }
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

            if (!expressionType.implementsType(annotatedType)) { //typesEqual(annotatedType, expressionType)) {
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

            GenericFunctionCallResult result = checkGenericFunctionCall(typeDeclaration, argumentTypes);

            if (!result.passesTypeChecks()) {
                throw new GenericError(ctx, typeDeclaration, argumentTypes);
            }

            /*
            if (argumentTypes.size() != typeDeclaration.getSignature().parameterTypes.size()) {
                throw new WrongNumberOfFunctionParameters(ctx,
                    typeDeclaration.getSignature().parameterTypes.size(),
                    argumentTypes.size());
            }

            // Check the non=genric parameters
            for (Triplet<String, Type, Type> types : zip(typeDeclaration.getParameterNames(), typeDeclaration.getSignature().parameterTypes, argumentTypes)) {
                if (types._1.getClass().isAssignableFrom(GenericParameter.class)) {
                    continue;
                }
                if (!types._2.implementsType(types._1)) { //typesEqual(types._1, types._2)) {
                    throw new FunctionParameterTypeMismatch(ctx, functionName, types._0, types._1, types._2);
                }
            }

            for (GenericArguments arguments: Generics.getGenericArguments(typeDeclaration, argumentTypes)) {

                GenericParameter param = arguments.parameter;

                Set<Type> allUsageTypes = Sets.newHashSet();

                for (Pair<Type, Type> types: zip(arguments.argumentTypes, arguments.argumentTypes)) {

                    Type declaration = types.left;
                    Type usage = types.right;

                    try {
                        allUsageTypes.addAll(getTypesOfGenericParameter(param, declaration, usage));
                    } catch (GenericMismatch genericMismatch) {
                        // TODO: Make this a different exception
                        // Properly propagate the GenericMismatch exception's information upward
                        throw new InconsistentGenericTypes(ctx, param, ImmutableList.of(declaration, usage)); //entry.getKey(), types);
                    }
                }

                if (!typesConsistent(allUsageTypes)) {
                    throw new InconsistentGenericTypes(ctx, param, allUsageTypes); //
                }
            }
*/

            /*
            // Now, check that the generic parameters are consistent
            for (Entry<GenericPrameter, List<Pair<Type typeDeclaration, Type

            for (Entry<GenericParameter, List<Integer>> entry: typeDeclaration.getGenericTypeIndices().entrySet()) {

                // For a given GenericParameter (eg "T", "U", etc), get all of
                // the arguments in the function call that refer to this
                // generic parameter.  So, for example, if our function signature is:
                //   defn func(mapper: (T) -> T, val: T, other: U)
                // and we call it with:
                //   func(myMapper, myVal, myOtherVal)
                // we would pull out:
                // {T: [myMapper, myVal], U: [myOtherVal]}
                Iterable<Type> types = getAll(argumentTypes, entry.getValue());

                // Now, we need to "extract" the generic parameter from the
                // called argument.


                if (!typesConsistent(types)) {
                    throw new InconsistentGenericTypes(ctx, entry.getKey(), types);
                }
            }
            */

        } else {

            List<Type> argumentTypes = getArgumentTypes(currentScope(), ctx);

            FunctionDeclaration typeDeclaration = currentScope()
                .getFunctionDeclaration(functionName)
                .orElseThrow(jestException(ctx));

            if (argumentTypes.size() != typeDeclaration.getSignature().parameterTypes.size()) {
                throw new WrongNumberOfFunctionParameters(ctx,
                    typeDeclaration.getSignature().parameterTypes.size(),
                    argumentTypes.size());
            }

            for (Triplet<String, Type, Type> types : zip(typeDeclaration.getParameterNames(), typeDeclaration.getSignature().parameterTypes, argumentTypes)) {
                if (!types._2.implementsType(types._1)) { //typesEqual(types._1, types._2)) {
                    throw new FunctionParameterTypeMismatch(ctx, functionName, types._0, types._1, types._2);
                }
            }
        }
    }
}
