package jest;


import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
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


    public static class Pair<T, U> {
        public final T left;
        public final U right;
        public Pair(T left, U right) {
            this.left = left;
            this.right = right;
        }
    }

    public static class Triplet<T, U, V> {
        public final T _0;
        public final U _1;
        public final V _2;

        public Triplet(T _0, U _1, V _2) {
            this._0 = _0;
            this._1 = _1;
            this._2 = _2;
        }
    }


    public static <T, U> Iterable<Pair<T, U>> zip(Iterable<T> left, Iterable<U> right) {

        Iterator<T> leftItr = left.iterator();
        Iterator<U> rightItr = right.iterator();

        List<Pair<T, U>> result = Lists.newArrayList();

        while (leftItr.hasNext() && rightItr.hasNext()) {
            T t = leftItr.next();
            U u = rightItr.next();
            result.add(new Pair<T, U>(t, u));
        }

        return result;
    }

    public static <T, U, V> Iterable<Triplet<T, U, V>> zip(Iterable<T> left, Iterable<U> middle, Iterable<V> right) {

        Iterator<T> leftItr = left.iterator();
        Iterator<U> middleItr = middle.iterator();
        Iterator<V> rightItr = right.iterator();


        List<Triplet<T, U, V>> result = Lists.newArrayList();

        while (leftItr.hasNext() && middleItr.hasNext() && rightItr.hasNext()) {
            T t = leftItr.next();
            U u = middleItr.next();
            V v = rightItr.next();
            result.add(new Triplet<T, U, V>(t, u, v));
        }

        return result;
    }
}
