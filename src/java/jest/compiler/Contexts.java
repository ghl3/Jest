package jest.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import jest.Exception.BadSource;
import jest.Exception.NotYetImplemented;
import jest.compiler.Types.DeclaredFunctionSignature;
import jest.compiler.Types.FunctionSignature;
import jest.compiler.Types.GenericFunctionSignature;
import jest.compiler.Types.GenericParameter;
import jest.compiler.Types.Type;
import jest.compiler.Types.SimpleType;
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
import static jest.Utils.zip;


public class Contexts {


    public static String getName(FunctionDefContext ctx) {
        return ctx.name.getText();
    }


    public static Type getType(Scope scope, ExpressionContext expression) {
        return new ExpressionEvaluator(scope).visit(expression);
    }


    public static Type getType(TypeAnnotationContext ctx) {
        if (ctx.singleType != null) {
            return new SimpleType(ctx.singleType.getText());
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

    //public static Boolean isFunctionSignatureGeneric(FunctionSignature ctx) {
//        return
//    }


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
        String name = getName(function);
        Type returnType = getReturnType(scope, function);
        List<String> parameterNames = getParameterNames(function.functionDefParams());

        if (function.firstGenericParam==null) {
            List<Type> parameterTypes = getParameterTypes(function.functionDefParams());
            return new DeclaredFunctionSignature(name, parameterNames, parameterTypes, returnType);
        } else {
            Set<String> genericTypes = getGenericParameers(function);
            List<Type> parameterTypes = getGenericParameterTypes(function.functionDefParams(), genericTypes);
            return new GenericFunctionSignature(name, parameterNames, parameterTypes, returnType);
        }
    }


    public static FunctionSignature getMethodSignature(MethodDefContext function) {
        return null;
    }


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

    private static List<Type> getGenericParameterTypes(FunctionDefParamsContext functionDefParamsContext, Set<String> genericParameters) {
        List<Type> types = Lists.newArrayList();
        for (TypeAnnotationContext type: combine(functionDefParamsContext.firstType, functionDefParamsContext.restTypes)) {
            if (genericParameters.contains(type.getText())) {
                types.add(new GenericParameter(type.getText()));
            } else {
                types.add(getType(type));
            }
        }
        return ImmutableList.copyOf(types);
    }

    private static Set<String> getGenericParameers(FunctionDefContext function) {
        Set<String> genericParameters = Sets.newHashSet();
        for (Token foo: combine(function.firstGenericParam, function.genericParameter)) {
            genericParameters.add(foo.getText());
        }
        return genericParameters;
    }

}
