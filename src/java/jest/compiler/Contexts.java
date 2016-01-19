package jest.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jest.Exception.BadSource;
import jest.Exception.NotYetImplemented;
import jest.Exception.UnknownFunction;
import jest.Exception.UnknownType;
import jest.Exception.UnknownVariable;
import jest.Utils.Pair;
import jest.compiler.Types.DeclaredFunctionDeclaration;
import jest.compiler.Types.FunctionDeclaration;
import jest.compiler.Types.FunctionSignature;
import jest.compiler.Types.FunctionType;
import jest.compiler.Types.GenericFunctionDeclaration;
import jest.compiler.Types.GenericParameter;
import jest.compiler.Types.Type;
import jest.compiler.Types.SimpleType;
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
            return getType(scope, ctx.b);
        } else {
            throw new NotYetImplemented(ctx, "Type Not singleType");
        }
    }

    public static Type getType(Scope scope, VariableTypeContext ctx) {
        String typeName = ctx.path().getText();
        return scope.getType(typeName).orElseThrow(jestException(new UnknownType(ctx, typeName)));
       //return new SimpleType(ctx.path().getText());
    }

    public static Type getType(Scope scope, FunctionTypeContext ctx) {
        return getFunctionType(scope, ctx);
    }

    public static FunctionType getFunctionType(Scope scope, FunctionTypeContext ctx) {

        List<Type> types = Lists.newArrayList();
        for (TypeAnnotationContext annotation: combine(ctx.first, ctx.rest)) {
            Type type = getType(scope, annotation); //getType(annotation);
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


    public static Type getReturnType(Scope scope, FunctionDefContext function) {
        String returnTypeName = function.returnType.getText();
        return scope.getType(returnTypeName).orElseThrow(jestException(function));
    }


    public static FunctionDeclaration getFunctionDeclaration(Scope scope, FunctionDefContext function) {
        String name = getFunctionName(function);
        List<String> parameterNames = getParameterNames(function.functionDefParams());

        if (function.firstGenericParam==null) {
            Type returnType = getReturnType(scope, function);
            List<Type> parameterTypes = getParameterTypes(scope, function.functionDefParams());
            return new DeclaredFunctionDeclaration(name, parameterNames, parameterTypes, returnType);
        } else {
            Set<String> genericTypes = getGenericParameters(function);

            String returnTypeIdentifier = function.returnType.getText();
            Type returnType;
            if (genericTypes.contains(returnTypeIdentifier)) {
                returnType = new GenericParameter(returnTypeIdentifier);
            } else {
                returnType = getReturnType(scope, function);
            }

            List<Type> parameterTypes = getGenericParameterTypes(scope, function.functionDefParams(), genericTypes);
            return new GenericFunctionDeclaration(name, parameterNames, parameterTypes, returnType);
        }
    }


    public static FunctionDeclaration getMethodSignature(MethodDefContext function) {
        return null;
    }


    public static Boolean isVariableType(TypeAnnotationContext ctx) {
        return ctx.a != null;
    }

    public static Boolean isGenericVariableType(TypeAnnotationContext ctx, Set<String> genericParameters) {
        return isVariableType(ctx) && genericParameters.contains(ctx.a.getText());
    }

    public static Boolean isFunctionType(TypeAnnotationContext ctx) {
        return ctx.b != null;
    }


    public static class FunctionParameterSummary {

        public final List<Pair<String, Type>> variableTypes;
        public final List<Pair<String, GenericParameter>> genericTypes;
        public final List<Pair<String, FunctionType>> functionTypes;

        public FunctionParameterSummary(List<Pair<String, Type>> variableTypes,
                                        List<Pair<String, GenericParameter>> genericTypes,
                                        List<Pair<String, FunctionType>> functionTypes) {
            this.variableTypes = ImmutableList.copyOf(variableTypes);
            this.genericTypes = ImmutableList.copyOf(genericTypes);
            this.functionTypes = ImmutableList.copyOf(functionTypes);
        }
    }

    // TODO: Clean up function def stuff...
    public static FunctionParameterSummary getFunctionParameterSummary(Scope scope, FunctionDefContext functionDef) {

        FunctionDefParamsContext params = functionDef.functionDefParams();
        Set<String> genericParameterNames = getGenericParameters(functionDef);

        List<Pair<String, Type>> variableTypes = Lists.newArrayList();
        List<Pair<String, GenericParameter>> genericTypes = Lists.newArrayList();
        List<Pair<String, FunctionType>> functionTypes = Lists.newArrayList();

        for (Pair<Token, TypeAnnotationContext> pair: zip(
            combine(params.first, params.rest),
            combine(params.firstType, params.restTypes))) {

            String parameterName = pair.left.getText();

            if (isGenericVariableType(pair.right, genericParameterNames)) { //  genericParameterNames.contains(parameterName)) {
                String genericTokenName = pair.right.a.getText();
                genericTypes.add(new Pair<String, GenericParameter>(parameterName, new GenericParameter(genericTokenName)));
            }

            else if (isVariableType(pair.right)) {
                String typeName = pair.right.a.getText();
                Type variableType = scope.getType(typeName).orElseThrow(jestException(new UnknownType(functionDef, typeName)));
                variableTypes.add(new Pair<String, Type>(parameterName, variableType));
            }

            else if (isFunctionType(pair.right)) {
                FunctionType type = getFunctionType(scope, pair.right.b);
                functionTypes.add(new Pair<String, FunctionType>(parameterName, type));
            }

            else {
                throw new BadSource(functionDef);
            }
        }

        return new FunctionParameterSummary(variableTypes, genericTypes, functionTypes);
    }


    public static List<Pair<String, GenericParameter>> getGenricParameters(FunctionDefParamsContext params) {
        return null;
    }

    public static List<Pair<String, FunctionType>> getFunctionParameters(FunctionDefParamsContext params) {
        return null;
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
            // TODO: Handle function type parameters here
            //String typeName = typeAnn.getText();
            // Ensure that types are declared
            //types.add(scope.getType(typeName).orElseThrow(jestException(new UnknownType(typeAnn, typeName))));
        }
        return ImmutableList.copyOf(types);
    }

    private static List<Type> getGenericParameterTypes(Scope scope, FunctionDefParamsContext functionDefParamsContext, Set<String> genericParameters) {
        List<Type> types = Lists.newArrayList();
        for (TypeAnnotationContext typeAnn: combine(functionDefParamsContext.firstType, functionDefParamsContext.restTypes)) {
            if (genericParameters.contains(typeAnn.getText())) {
                types.add(new GenericParameter(typeAnn.getText()));
            } else {
                String typeName = typeAnn.getText();
                // Ensure that types are declared
                types.add(scope.getType(typeName).orElseThrow(jestException(new UnknownType(typeAnn, typeName))));
            }
        }
        return ImmutableList.copyOf(types);
    }

    public static Set<String> getGenericParameters(FunctionDefContext function) {
        Set<String> genericParameters = Sets.newHashSet();
        for (Token foo: combine(function.firstGenericParam, function.genericParameter)) {
            genericParameters.add(foo.getText());
        }
        return genericParameters;
    }

}
