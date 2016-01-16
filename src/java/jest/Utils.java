package jest;


import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import org.antlr.v4.runtime.ParserRuleContext;


public class Utils {

    public static String getLineInfo(ParserRuleContext context) {
        return String.format("%s (Line: %s Character: %s)",
            context.getText(), context.start.getLine(), context.start.getCharPositionInLine());
    }

    public static <T> Iterable<T> removeNulls(Iterable<T> lst) {
        return Iterables.filter(lst, Predicates.notNull());
    }

    public static <T> Iterable<T> combine(T left, Iterable<T> right) {
        List<T> lst = Lists.newArrayList();
        lst.add(left);
        for (T t: right) {
            lst.add(t);
        }
        return ImmutableList.copyOf(removeNulls(lst));
    }

    public static <T> Iterable<T> combine(Iterable<T> left, Iterable<T> right) {
        List<T> lst = Lists.newArrayList();
        for (T t: left) {
            lst.add(t);
        }
        for (T t: right) {
            lst.add(t);
        }
        return ImmutableList.copyOf(removeNulls(lst));
    }

    public static <T> Iterable<T> combine(Iterable<T> left, T right) {
        List<T> lst = Lists.newArrayList();
        for (T t: left) {
            lst.add(t);
        }
        lst.add(right);
        return ImmutableList.copyOf(removeNulls(lst));
    }

    public static <T> Iterable<T> allButLast(List<T> lst) {
        if (lst.size() == 0 || lst.size() == 1) {
            return ImmutableList.of();
        } else {
            return lst.subList(0, lst.size()-2);
        }
    }

    public static <T> Optional<T> last(List<T> lst) {
        if (lst.size() == 0) {
            return Optional.empty();
        } else {
            return Optional.of(lst.get(lst.size()-1));
        }
    }
}
