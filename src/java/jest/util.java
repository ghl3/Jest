package jest;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
//import jdk.nashorn.internal.runtime.options.Option;
import org.antlr.v4.runtime.ParserRuleContext;


public class util {

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

    public static String getLineInfo(ParserRuleContext context) {
        return String.format("%s (Line: %s Character: %s)",
            context.getText(), context.start.getLine(), context.start.getCharPositionInLine());
    }

    public static <T> Iterable<T> combine(T left, Iterable<T> right) {
        List<T> lst = Lists.newArrayList();
        lst.add(left);
        for (T t: right) {
            lst.add(t);
        }
        return ImmutableList.copyOf(lst);
    }

    public static <T> Iterable<T> combine(Iterable<T> left, Iterable<T> right) {
        List<T> lst = Lists.newArrayList();
        for (T t: left) {
            lst.add(t);
        }
        for (T t: right) {
            lst.add(t);
        }
        return ImmutableList.copyOf(lst);
    }

    public static <T> Iterable<T> combine(Iterable<T> left, T right) {
        List<T> lst = Lists.newArrayList();
        for (T t: left) {
            lst.add(t);
        }
        lst.add(right);
        return ImmutableList.copyOf(lst);
    }

    public static <T> Optional<T> last(List<T> lst) {
        if (lst.size() == 0) {
            return Optional.empty();
        } else {
            return Optional.of(lst.get(lst.size()-1));
        }
    }
}
