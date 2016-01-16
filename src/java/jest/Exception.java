package jest;


import java.util.function.Supplier;
import org.antlr.v4.runtime.ParserRuleContext;

import static jest.Utils.getLineInfo;

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


    public static  Supplier<BadSource> jestException(final ParserRuleContext ctx) {
        return new Supplier<BadSource>() {
            @Override
            public BadSource get() {
                return new BadSource(ctx);
            }
        };
    }


    public static <T extends JestCompilerException> Supplier<T> jestException(final Class<T> clazz, final ParserRuleContext ctx) {
        return new Supplier<T>() {
            @Override
            public T get() {
                try {
                    return clazz.getConstructor(ParserRuleContext.class).newInstance(ctx);
                } catch (Throwable thr) {
                    throw new JestCompilerException("Error throwing exception!");
                }
            }
        };
    }
}
