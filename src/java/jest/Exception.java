package jest;

import java.util.function.Supplier;
import jest.compiler.DeclaredTypes.Type;
import org.antlr.v4.runtime.ParserRuleContext;

import static jest.Utils.getLineInfo;

public class Exception {

    // Translation Exceptions

    public static class JestTranslationException extends RuntimeException {
        public JestTranslationException(String message, ParserRuleContext ctx) {
            super(String.format("%s - %s", message, getLineInfo(ctx)));
        }
    }


    public static class BadSource extends JestTranslationException {
        public BadSource(ParserRuleContext context) {
            super("Invalid Source Code", context);
        }
    }


    public static class NotYetImplemented extends JestTranslationException {
        public NotYetImplemented(ParserRuleContext context, String feature) {
            super(String.format("The Feature %s has not yet been implemented", feature), context);
        }
    }


    // Validation Exceptions

    public static class ValidationException extends RuntimeException {
        public ValidationException(String message, ParserRuleContext ctx) {
            super(String.format("%s - %s", message, getLineInfo(ctx)));
        }
    }


    public static class NotExpression extends ValidationException {
        public NotExpression(ParserRuleContext context) {
            super("Expected an expression, but encountered something else", context);
        }
    }


    public static class VariableTypeMismatch extends ValidationException {
        public VariableTypeMismatch(ParserRuleContext context, String varName, Type expected, Type encountered) {
            super(String.format("Expected variable %s to have type %s but found type %s",
                varName, expected, encountered), context);
        }
    }


    public static class WrongNumberOfFunctionParameters extends ValidationException {
        public WrongNumberOfFunctionParameters(ParserRuleContext context, Integer expected, Integer encountered) {
            super(String.format("Expected %s function parameters but encountered %s",
                expected, encountered), context);
        }
    }


    public static class FunctionParameterTypeMismatch extends ValidationException {
        public FunctionParameterTypeMismatch(ParserRuleContext context, String funcName, String paramName,
                                             Type expected, Type encountered) {
            super(String.format("Expected parameter %s for function %s to have type %s but has type %s",
                paramName, funcName, expected, encountered), context);
        }
    }


    public static class VariableAlreadyDeclared extends ValidationException {
        public VariableAlreadyDeclared(ParserRuleContext ctx, String variableName) {
            super(String.format("Trying to declare a variable with name %s but a variable " +
                "with that name has already been declared in this scope", variableName),
                ctx);
        }
    }


    public static class FunctionAlreadyDeclared extends ValidationException {
        public FunctionAlreadyDeclared(ParserRuleContext ctx, String variableName) {
            super(String.format("Trying to declare a function with name %s but a variable " +
                    "with that name has already been declared in this scope", variableName),
                ctx);
        }
    }


    public static class UnknownVariable extends ValidationException {
        public UnknownVariable(ParserRuleContext ctx, String variableName) {
            super(String.format("Encountered an unknown variable with name %s", variableName),
                ctx);
        }
    }


    public static class UnknownFunction extends ValidationException {
        public UnknownFunction(ParserRuleContext ctx, String variableName) {
            super(String.format("Encountered an unknown function with name %s", variableName),
                ctx);
        }
    }


    public static  Supplier<BadSource> jestException(final ParserRuleContext ctx) {
        return new Supplier<BadSource>() {
            @Override
            public BadSource get() {
                return new BadSource(ctx);
            }
        };
    }

}
