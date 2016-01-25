package jest.compiler;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jest.Utils.Pair;
import jest.compiler.Generics.GenericInferenceSummary;
import jest.compiler.Types.GenericParameter;
import jest.compiler.Types.Type;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.bind.DefaultSearchBinder;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import static jest.Utils.enumerate;

// https://github.com/chocoteam/choco3

public class TypeInference {

    public static GenericInferenceSummary inferGenericTypes(Iterable<Pair<GenericParameter, Type>> constraints) {

        Set<Type> allTypes = Sets.newHashSet();
        for(Pair<GenericParameter, Type> types: constraints) {
            allTypes.add(types.left);
            allTypes.add(types.right);
        }

        Set<Type> concreateTypes = allTypes.stream()
            .filter((x) -> !GenericParameter.class.isAssignableFrom(x.getClass()))
            .collect(Collectors.toSet());

        // Create a 2-way map of concrete types to integers
        BiMap<Type, Integer> concreteTypeIntMap = HashBiMap.create();
        for (Pair<Integer, Type> pair: enumerate(Lists.newArrayList(concreateTypes))) {
            concreteTypeIntMap.put(pair.right, pair.left);
        }

        int numConcreteTypes = concreateTypes.size();

        Set<GenericParameter> genericTypes = allTypes.stream()
            .filter((x) -> GenericParameter.class.isAssignableFrom(x.getClass()))
            .map((x) -> (GenericParameter) x)
            .collect(Collectors.toSet());

        Solver solver = new Solver("genericsSolve");

        // Setup all variables
        BiMap<GenericParameter, IntVar> typeMap = HashBiMap.create();

        for (GenericParameter type: genericTypes) {
            String name = String.valueOf(type.hashCode());
            IntVar x = VariableFactory.integer(name, 0, numConcreteTypes, solver);
            typeMap.put(type, x);
        }

        for (Pair<GenericParameter, Type> constraint: constraints) {

            IntVar left = typeMap.get(constraint.left);

            if (GenericParameter.class.isAssignableFrom(constraint.right.getClass()) && typeMap.containsKey((GenericParameter)constraint.right)) {
                IntVar right = typeMap.get((GenericParameter)constraint.right);
                solver.post(IntConstraintFactory.arithm(left, "=", right));
            } else if (concreteTypeIntMap.containsKey(constraint.right)) {
                int right = concreteTypeIntMap.get(constraint.right);
                solver.post(IntConstraintFactory.arithm(left, "=", right));
            } else {
                throw new RuntimeException();
            }
        }

        // Set a reasonable search strategy
        solver.set(IntStrategyFactory.domOverWDeg(typeMap.values().toArray(new IntVar[typeMap.size()]), 0));

        if (solver.findSolution()) {
            Map<GenericParameter, Type> inferredTypes = Maps.newHashMap();

            for (GenericParameter type: genericTypes) {
                IntVar var = typeMap.get(type);
                Type concreteType = concreteTypeIntMap.inverse().get(var.getValue());
                inferredTypes.put(type, concreteType);
            }

            return GenericInferenceSummary.success(inferredTypes);
        } else {
            return GenericInferenceSummary.failure();
        }
    }
}
