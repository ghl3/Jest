package jest;


import org.antlr.v4.runtime.ParserRuleContext;

import static jest.util.getLineInfo;

public class Exception {

    public static class ClojureSourceGeneratorException extends RuntimeException {
        public ClojureSourceGeneratorException(String message) {
            super(message);
        }
    }

    public static class BadSource extends ClojureSourceGeneratorException {
        public BadSource(ParserRuleContext context) {
            super(String.format("Error - %s", getLineInfo(context)));
        }
    }

    public static class NotExpressionError extends RuntimeException {
        public NotExpressionError(ParserRuleContext context) {
            super(String.format("Error - %s", getLineInfo(context)));
        }
    }
}
