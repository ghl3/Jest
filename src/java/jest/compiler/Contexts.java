package jest.compiler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import jest.Exception.NotYetImplemented;
import jest.compiler.DeclaredTypes.DeclaredFunctionSignature;
import jest.compiler.DeclaredTypes.FunctionSignature;
import jest.compiler.DeclaredTypes.Type;
import jest.compiler.DeclaredTypes.UserType;
import jest.grammar.JestParser.ExpressionContext;
import jest.grammar.JestParser.FunctionDefContext;
import jest.grammar.JestParser.FunctionDefParamsContext;
import jest.grammar.JestParser.TypeAnnotationContext;

import static jest.Exception.jestException;
import static jest.Utils.combine;


public class Contexts {


    public static String getName(FunctionDefContext ctx) {
        return ctx.ID().getText();
    }


    public static Type getType(Scope scope, ExpressionContext expression) {
        return new ExpressionEvaluator(scope).visit(expression);
    }


    public static Type getType(TypeAnnotationContext ctx) {
        if (ctx.singleType != null) {
            return new UserType(ctx.singleType.getText());
        } else {
            throw new NotYetImplemented(ctx);
        }
    }


    public static Type getReturnType(Scope scope, FunctionDefContext function) {
        String returnTypeName = function.returnType.getText();
        return scope.getType(returnTypeName).orElseThrow(jestException(function));
    }


    public static FunctionSignature getFunctionSignature(Scope scope, FunctionDefContext function) {
        Type returnType = getReturnType(scope, function);
        List<Type> parameterTypes = getParameterTypes(function.functionDefParams());
        return new DeclaredFunctionSignature(getName(function), parameterTypes, returnType);
    }


    private static List<Type> getParameterTypes(FunctionDefParamsContext functionDefParamsContext) {
        List<Type> types = Lists.newArrayList();
        for (TypeAnnotationContext typeAnn: combine(functionDefParamsContext.firstType, functionDefParamsContext.restTypes)) {
            types.add(getType(typeAnn));
        }
        return ImmutableList.copyOf(types);
    }

}
