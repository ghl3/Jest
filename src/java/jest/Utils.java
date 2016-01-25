package jest;


import com.google.common.base.Predicates;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.antlr.v4.runtime.ParserRuleContext;


public class Utils {

    public interface Named {
        String getName();
    }

    public static String getLineInfo(ParserRuleContext context) {
        return String.format("%s (Line: %s Character: %s)",
            context.getText(), context.start.getLine(), context.start.getCharPositionInLine());
    }

    public static <T> Iterable<T> removeNulls(Iterable<T> lst) {
        return StreamSupport.stream(lst.spliterator(), false)
            .filter(Predicates.notNull()::apply)
            .collect(Collectors.toList());
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

        @Override
        public boolean equals(Object other) {
            if (other instanceof Pair) {
                Pair otherPair = (Pair) other;
                return Objects.equals(this.left, otherPair.left) &&
                    Objects.equals(this.right, otherPair.right);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.left, this.right);
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

    public static <T> Iterable<Pair<Integer, T>> enumerate(List<T> lst) {
        List<Pair<Integer, T>> pairs = Lists.newArrayList();
        for (int i=0; i < lst.size(); ++i) {
            pairs.add(new Pair<Integer, T>(i, lst.get(i)));
        }
        return pairs;
    }



    public static <T> List<T> getAll(List<T> items, Iterable<Integer> indices) {

        List<T> returnItems = Lists.newArrayList();

        for (Integer idx: indices) {
            returnItems.add(items.get(idx));
        }

        return ImmutableList.copyOf(returnItems);
    }


    /**
     * Return all possible pairs of distinct items
     * from the given iterable.
     * @param items
     * @param <T>
     * @return
     */
    public static <T> List<Pair<T, T>> getAllPairs(Iterable<T> items) {

        List<T> itemsList = Lists.newArrayList(Sets.newHashSet(items));

        List<Pair<T ,T>> pairs = Lists.newArrayList();

        for (int i=0; i < itemsList.size(); ++i) {
            for (int j=i+1; j < itemsList.size(); ++j){
                pairs.add(new Pair<T, T>(itemsList.get(i), itemsList.get(j)));
            }
        }
        return pairs;
    }

    /**
     * Takes an object and a type.  Returns an iterable
     * of that object cast as the given type if that object
     * implements the given type.  Else, returns an empty
     * iterator.  Useful for "pattern matching" via for loops.
     * @param left
     * @param type
     * @param <T>
     * @return
     */
    public static <T> Iterable<T> asType(Object left, Class<T> type) {
        if (type.isAssignableFrom(left.getClass())) {
            T leftAsT = (T) left;
            return ImmutableSet.of(leftAsT);
        } else {
            return ImmutableSet.of();
        }
    }

    public static Stream<Integer> range(Integer num) {
        return ContiguousSet.create(Range.closedOpen(0, num), DiscreteDomain.integers()).stream();
    }
}
