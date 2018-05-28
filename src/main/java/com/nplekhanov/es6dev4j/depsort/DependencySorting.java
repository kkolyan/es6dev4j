package com.nplekhanov.es6dev4j.depsort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DependencySorting {

    public static <D extends Dependency<D>> List<D> sort(Collection<? extends D> roots) {
        Set<D> satisfied = new LinkedHashSet<>();

        for (D root : roots) {
            collectSatisfied(root, satisfied, new LinkedHashSet<>());
        }

        return new ArrayList<>(satisfied);
    }

    private static <D extends Dependency<D>> boolean collectSatisfied(
            D dependency,
            Set<? super D> satisfiedNodes,
            LinkedHashSet<D> hashedStack) {

        if (!hashedStack.add(dependency)) {
            List<D> fullCyclic = new ArrayList<>(hashedStack);
            fullCyclic.add(dependency);
            throw new IllegalStateException("cyclic dependency chain: " + fullCyclic);
        }

        boolean satisfied = true;
        for (D dep : dependency.getDirectDependencies()) {
            satisfied = satisfied && collectSatisfied(dep, satisfiedNodes, hashedStack);
        }
        if (satisfied) {
            satisfiedNodes.add(dependency);
        }
        hashedStack.remove(dependency);
        return satisfied;
    }
}