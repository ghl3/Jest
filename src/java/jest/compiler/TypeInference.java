package jest.compiler;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jest.Utils.Pair;
import jest.compiler.Types.GenericParameter;
import jest.compiler.Types.Type;
import org.antlr.v4.runtime.misc.MultiMap;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

import static jest.Utils.enumerate;

// https://github.com/chocoteam/choco3

public class TypeInference {


    public static void solve() {

        // 1. Create a Solver
        Solver solver = new Solver("my first problem");
        // 2. Create variables through the variable factory
        IntVar x = VariableFactory.integer("X", 0, 5, solver);
        IntVar y = VariableFactory.bounded("Y", 0, 5, solver);
        // 3. Create and post constraints by using constraint factories
        solver.post(IntConstraintFactory.arithm(x, "+", y, "<", 5));
        // 4. Define the search strategy
        solver.set(IntStrategyFactory.lexico_LB(new IntVar[]{x, y}));
        // 5. Launch the resolution process

        solver.findSolution();
        solver.findSolution();
        //6. Print search statistics
        Chatterbox.printStatistics(solver);

    }


    public static boolean hasGenericsSolution(List<Pair<GenericParameter, Type>> constraints) {

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

            if (typeMap.containsKey(constraint.right)) {
                IntVar right = typeMap.get(constraint.right);
                solver.post(IntConstraintFactory.arithm(left, "=", right));
            } else if (concreteTypeIntMap.containsKey(constraint.right)) {
                int right = concreteTypeIntMap.get(constraint.right);
                solver.post(IntConstraintFactory.arithm(left, "=", right));
            } else {
                throw new RuntimeException();
            }
        }

        if (solver.findSolution()) {
            //6. Print search statistics
            Chatterbox.printStatistics(solver);

            for (GenericParameter type: genericTypes) {
                IntVar var = typeMap.get(type);

                Type concreteType = concreteTypeIntMap.inverse().get(var.getValue());

                System.out.println(String.format("Generic Type: %s %s = %s",
                    type, type.getName(), concreteType));
            }

            return true;
        } else {
            return false;
        }

    }
}
