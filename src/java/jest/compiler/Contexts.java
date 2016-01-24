package jest.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import jest.Exception.BadSource;
import jest.Exception.NotYetImplemented;
import jest.Exception.UnknownFunction;
import jest.Exception.UnknownType;
import jest.Exception.UnknownVariable;
import jest.Exception.ValidationException;
import jest.Utils.Pair;
import jest.compiler.Types.DeclaredFunctionDeclaration;
import jest.compiler.Types.FunctionDeclaration;
import jest.compiler.Types.FunctionSignature;
import jest.compiler.Types.FunctionType;
import jest.compiler.Types.GenericFunctionDeclaration;
import jest.compiler.Types.GenericParameter;
import jest.compiler.Types.Type;
import jest.grammar.JestParser.ExpressionContext;
import jest.grammar.JestParser.ExpressionListContext;
import jest.grammar.JestParser.FunctionCallContext;
import jest.grammar.JestParser.FunctionDefContext;
import jest.grammar.JestParser.FunctionDefParamsContext;
import jest.grammar.JestParser.FunctionTypeContext;
import jest.grammar.JestParser.MethodDefContext;
import jest.grammar.JestParser.MethodParamsContext;
import jest.grammar.JestParser.TypeAnnotationContext;
import jest.grammar.JestParser.VariableTypeContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import static jest.Exception.jestException;
import static jest.Utils.combine;
import static jest.Utils.removeNulls;
import static jest.Utils.zip;


public class Contexts {


    public static Type getType(Scope scope, ExpressionContext expression) {
        return new ExpressionEvaluator(scope).visit(expression);
    }


    public static Type getType(Scope scope, TypeAnnotationContext ctx) {
        if (ctx.a != null) {
            return getType(scope, ctx.a);
        } else if (ctx.b != null) {
            return getFunctionType(scope, ctx.b);
        } else {
            throw new NotYetImplemented(ctx, "Type Not singleType");
        }
    }

    public static Type getType(Scope scope, VariableTypeContext ctx) {
        String typeName = ctx.path().getText();
        return scope.getType(typeName).orElseThrow(jestException(new UnknownType(ctx, typeName)));
    }


    public static FunctionType getFunctionType(Scope scope, FunctionTypeContext ctx) {

        List<Type> types = Lists.newArrayList();
        for (TypeAnnotationContext annotation: combine(ctx.first, ctx.rest)) {
            Type type = getType(scope, annotation);
            types.add(type);
        }

        Type returnType = getType(scope, ctx.returnType);

        FunctionSignature signature = new FunctionSignature(types, returnType);
        return new FunctionType(signature);
    }


    public static Type getVariableOrFunctionType(Scope scope, String name, ParserRuleContext ctx) {
        if (scope.isVariableInCurrentScope(name)) {
            return scope.getVariableType(name).orElseThrow(jestException(new UnknownVariable(ctx, name)));
        } else if (scope.isFunctionInCurrentScope(name)) {
            FunctionDeclaration decl = scope.getFunctionDeclaration(name).orElseThrow(jestException(new UnknownFunction(ctx, name)));
            return new FunctionType(decl.getSignature());
        } else {
            throw new BadSource(ctx);
        }
    }


    public static String getFunctionName(FunctionCallContext ctx) {
        return ctx.ID().getText();
    }

    public static String getFunctionName(FunctionDefContext ctx) {
        return ctx.name.getText();
    }

    public static List<Type> getArgumentTypes(Scope scope, FunctionCallContext ctx) {
        return getArgumentTypes(scope, ctx.methodParams());
    }


    public static List<ExpressionContext> getExpressions(ExpressionListContext ctx) {
        return ImmutableList.copyOf(removeNulls(combine(ctx.a, ctx.b)));
    }

    public static List<Type> getArgumentTypes(Scope scope, MethodParamsContext ctx) {
        if (ctx.expression() != null) {
            return ImmutableList.of(getType(scope, ctx.expression()));
        } else if (ctx.expressionList() != null) {
            List<Type> types = Lists.newArrayList();
            for (ExpressionContext expression: getExpressions(ctx.expressionList())) {
                types.add(getType(scope, expression));
            }
            return ImmutableList.copyOf(types);
        } else {
            throw new BadSource(ctx);
        }
    }


    public static Type getReturnType(Scope scope, FunctionDefContext function) throws ValidationException {
        String returnTypeName = function.returnType.getText();
        return scope.getType(returnTypeName).orElseThrow(jestException(function));
    }


    /**
     * Takes a Parsed FunctionDefContext and returns a function
     * declaration object representing that declared function.
     * The return FunctionDeclaration may possibly be generic.
     * The Scope argument is used to ensure that types used
     * in the function declaration are legitimate types in the
     * current scope (and to reference those type objects in the
     * declaration object itself)
     * @param scope
     * @param function
     * @return
     */
    public static FunctionDeclaration getFunctionDeclaration(Scope scope, FunctionDefContext function) {
        if (isGenericDeclaration(function)) {
            return getGenericFunctionDeclaration(scope, function);
        } else {
            return getNonGenericFunctionDeclaration(scope, function);
        }
    }

    public static boolean isGenericDeclaration(FunctionDefContext ctx) {
        return ctx.firstGenericParam != null;
    }

    private static DeclaredFunctionDeclaration getNonGenericFunctionDeclaration(Scope scope, FunctionDefContext function) {
        String name = getFunctionName(function);
        List<String> parameterNames = getParameterNames(function.functionDefParams());
        Type returnType = getReturnType(scope, function);
        List<Type> parameterTypes = getParameterTypes(scope, function.functionDefParams());
        return new DeclaredFunctionDeclaration(name, parameterNames, parameterTypes, returnType);
    }

    private static GenericFunctionDeclaration getGenericFunctionDeclaration(Scope scope, FunctionDefContext function)
        throws ValidationException {

        String name = getFunctionName(function);
        List<String> parameterNames = getParameterNames(function.functionDefParams());

        List<GenericParameter> genericParameters = getGenericParameters(function);

        Type returnType = getReturnType(scope, function);

        List<Type> parameterTypes = getParameterTypes(scope, function.functionDefParams());

        return new GenericFunctionDeclaration(name, genericParameters,
            parameterNames, parameterTypes,
            returnType);
    }

    public static FunctionDeclaration getMethodSignature(MethodDefContext function) {
        return null;
    }


    public static Boolean isVariableType(TypeAnnotationContext ctx) {
        return ctx.a != null;
    }

    public static Boolean isFunctionType(TypeAnnotationContext ctx) {
        return ctx.b != null;
    }


    public static class FunctionParameterSummary {

        public final List<Pair<String, Type>> variableTypes;
        public final List<Pair<String, FunctionType>> functionTypes;

        public FunctionParameterSummary(List<Pair<String, Type>> variableTypes,
                                        List<Pair<String, FunctionType>> functionTypes) {
            this.variableTypes = ImmutableList.copyOf(variableTypes);
            this.functionTypes = ImmutableList.copyOf(functionTypes);
        }
    }

    // TODO: Clean up function def stuff...
    public static FunctionParameterSummary getFunctionParameterSummary(Scope scope, FunctionDefContext functionDef) {

        FunctionDefParamsContext params = functionDef.functionDefParams();

        List<Pair<String, Type>> variableTypes = Lists.newArrayList();
        List<Pair<String, FunctionType>> functionTypes = Lists.newArrayList();

        for (Pair<Token, TypeAnnotationContext> pair: zip(
            combine(params.first, params.rest),
            combine(params.firstType, params.restTypes))) {

            String parameterName = pair.left.getText();

            if (isFunctionType(pair.right)) {
                FunctionType type = getFunctionType(scope, pair.right.b);
                functionTypes.add(new Pair<>(parameterName, type));
            } else if (isVariableType(pair.right)) {
                String typeName = pair.right.a.getText();
                Type variableType = scope.getType(typeName).orElseThrow(jestException(new UnknownType(functionDef, typeName)));
                variableTypes.add(new Pair<>(parameterName, variableType));
            } else {
                throw new BadSource(functionDef);
            }
        }
        return new FunctionParameterSummary(variableTypes, functionTypes);
    }


    private static List<String> getParameterNames(FunctionDefParamsContext functionDefParamsContext) {
        List<String> names = Lists.newArrayList();
        for (Token name: combine(functionDefParamsContext.first, functionDefParamsContext.rest)) {
            names.add(name.getText());
        }
        return ImmutableList.copyOf(names);
    }

    private static List<Type> getParameterTypes(Scope scope, FunctionDefParamsContext functionDefParamsContext) {
        List<Type> types = Lists.newArrayList();
        for (TypeAnnotationContext typeAnn: combine(functionDefParamsContext.firstType, functionDefParamsContext.restTypes)) {
            Type type = getType(scope, typeAnn);
            types.add(type);
        }
        return ImmutableList.copyOf(types);
    }


    public static List<GenericParameter> getGenericParameters(FunctionDefContext function) {
        List<GenericParameter> genericParameters = Lists.newArrayList();
        for (Token foo: combine(function.firstGenericParam, function.genericParameter)) {
            genericParameters.add(new GenericParameter(foo.getText()));
        }
        return genericParameters;
    }

}
