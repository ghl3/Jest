package jest.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import jest.Exception.BadSource;
import jest.Exception.NotYetImplemented;
import jest.compiler.DeclaredTypes.DeclaredFunctionSignature;
import jest.compiler.DeclaredTypes.FunctionSignature;
import jest.compiler.DeclaredTypes.Type;
import jest.compiler.DeclaredTypes.UserType;
import jest.grammar.JestParser.ExpressionContext;
import jest.grammar.JestParser.ExpressionListContext;
import jest.grammar.JestParser.FunctionCallContext;
import jest.grammar.JestParser.FunctionDefContext;
import jest.grammar.JestParser.FunctionDefParamsContext;
import jest.grammar.JestParser.MethodDefContext;
import jest.grammar.JestParser.MethodParamsContext;
import jest.grammar.JestParser.TypeAnnotationContext;
import org.antlr.v4.runtime.Token;

import static jest.Exception.jestException;
import static jest.Utils.combine;
import static jest.Utils.removeNulls;


public class Contexts {


    public static String getName(FunctionDefContext ctx) {
        return ctx.name.getText();
    }


    public static Type getType(Scope scope, ExpressionContext expression) {
        return new ExpressionEvaluator(scope).visit(expression);
    }


    public static Type getType(TypeAnnotationContext ctx) {
        if (ctx.singleType != null) {
            return new UserType(ctx.singleType.getText());
        } else {
            throw new NotYetImplemented(ctx, "Type Not singleType");
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


    public static FunctionSignature getFunctionSignature(Scope scope, FunctionDefContext function) {
        Type returnType = getReturnType(scope, function);
        List<String> parameterNames = getParameterNames(function.functionDefParams());
        List<Type> parameterTypes = getParameterTypes(function.functionDefParams());
        return new DeclaredFunctionSignature(getName(function), parameterNames, parameterTypes, returnType);
    }


    public static FunctionSignature getMethodSignature(MethodDefContext function) {
        return null;
    }


    //getParameterNames


    private static List<String> getParameterNames(FunctionDefParamsContext functionDefParamsContext) {
        List<String> names = Lists.newArrayList();
        for (Token name: combine(functionDefParamsContext.first, functionDefParamsContext.rest)) {
            names.add(name.getText());
        }
        return ImmutableList.copyOf(names);
    }

    private static List<Type> getParameterTypes(FunctionDefParamsContext functionDefParamsContext) {
        List<Type> types = Lists.newArrayList();
        for (TypeAnnotationContext typeAnn: combine(functionDefParamsContext.firstType, functionDefParamsContext.restTypes)) {
            types.add(getType(typeAnn));
        }
        return ImmutableList.copyOf(types);
    }

}
