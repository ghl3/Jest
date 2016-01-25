package jest.compiler;

import com.google.common.collect.ImmutableList;
import java.util.List;
import jest.compiler.Exceptions.FunctionParameterCategoryMismatch;
import jest.compiler.Exceptions.FunctionParameterTypeMismatch;
import jest.compiler.Exceptions.GenericInferenceException;
import jest.compiler.Exceptions.ValidationException;
import jest.compiler.Exceptions.WrongNumberOfFunctionParameters;
import jest.compiler.Types.GenericFunctionDeclaration;
import jest.compiler.Types.Type;
import org.antlr.v4.runtime.ParserRuleContext;


public class Errors {


    interface FunctionCallError {
        ValidationException createException(String functionName, ParserRuleContext context);
    }

    // Basic Errors

    public static class ParameterTypeMismatch implements FunctionCallError {

        public final String paramName;

        public final Type expected;

        public final Type actual;

        public ParameterTypeMismatch(String paramName, Type expected, Type actual) {
            this.paramName = paramName;
            this.expected = expected;
            this.actual = actual;
        }

        @Override
        public ValidationException createException(String functionName, ParserRuleContext ctx) {
            return new FunctionParameterTypeMismatch(ctx, functionName, paramName, expected, actual);
        }
    }

    public static class ParameterCategoryMismatch implements FunctionCallError {

        public final Type expected;

        public final Type actual;

        public ParameterCategoryMismatch(Type expected, Type actual) {
            this.expected = expected;
            this.actual = actual;
        }

        @Override
        public ValidationException createException(String functionName, ParserRuleContext ctx) {
            return new FunctionParameterCategoryMismatch(ctx, functionName, expected, actual);
        }
    }

    public static class ParameterNumberMismatch implements FunctionCallError {

        public final Integer numExpected;

        public final Integer numActual;

        public ParameterNumberMismatch(Integer numExpected, Integer numActual) {
            this.numExpected = numExpected;
            this.numActual = numActual;
        }

        @Override
        public ValidationException createException(String functionName, ParserRuleContext ctx) {
            return new WrongNumberOfFunctionParameters(ctx, functionName, numExpected, numActual);
        }
    }

    // Generic Errors

    public static class ArgumentNumberError implements FunctionCallError {

        public final Integer numExpected;

        public final Integer numEncountered;

        public ArgumentNumberError(Integer numExpected, Integer numEncountered) {
            this.numExpected = numExpected;
            this.numEncountered = numEncountered;
        }

        @Override
        public ValidationException createException(String functionName, ParserRuleContext context) {
            return new WrongNumberOfFunctionParameters(context, functionName, numExpected, numEncountered);
        }
    }


    public static class GenericTypeError implements FunctionCallError {

        public final String paramName;

        public final Type expected;

        public final Type encountered;

        public GenericTypeError(String paramName, Type expected, Type encountered) {
            this.paramName = paramName;
            this.expected = expected;
            this.encountered = encountered;
        }

        @Override
        public ValidationException createException(String functionName, ParserRuleContext context) {
            return new FunctionParameterTypeMismatch(context, functionName, paramName, expected, encountered);
        }
    }


    public static class GenericInferenceError implements FunctionCallError {

        public final GenericFunctionDeclaration declaration;

        public final List<Type> callingTypes;

        public GenericInferenceError(GenericFunctionDeclaration typeDeclaration, List<Type> callingTypes) {
            this.declaration = typeDeclaration;
            this.callingTypes = ImmutableList.copyOf(callingTypes);
        }

        @Override
        public ValidationException createException(String functionName, ParserRuleContext context) {
            return new GenericInferenceException(context, functionName, declaration, callingTypes);
        }
    }
}
