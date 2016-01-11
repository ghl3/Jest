package jest;


import org.antlr.v4.runtime.ParserRuleContext;

import static jest.util.getLineInfo;

public class Exception {

    public static class JestCompilerException extends RuntimeException {
        public JestCompilerException(String message) {
            super(message);
        }
    }

    public static class BadSource extends JestCompilerException {
        public BadSource(ParserRuleContext context) {
            super(String.format("Error - %s", getLineInfo(context)));
        }
    }

    public static class NotExpressionError extends RuntimeException {
        public NotExpressionError(ParserRuleContext context) {
            super(String.format("Error - %s", getLineInfo(context)));
        }
    }

    public static class TypeMismatchError extends JestCompilerException {
        public TypeMismatchError(ParserRuleContext context) {
            super(String.format("Type Mismatch - %s", getLineInfo(context)));
        }
    }

    public static class NotYetImplemented extends JestCompilerException {
        public NotYetImplemented(ParserRuleContext context) {
            super(String.format("Feature Not Yet Implemented - %s", getLineInfo(context)));
        }
    }

}
