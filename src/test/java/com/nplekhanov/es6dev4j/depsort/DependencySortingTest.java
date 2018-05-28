package com.nplekhanov.es6dev4j.depsort;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DependencySortingTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testSuccess() {

        SimpleDependency user = new SimpleDependency("User");
        SimpleDependency dropDown = new SimpleDependency("Dropdown")
                .dependsOn(user);
        SimpleDependency table = new SimpleDependency("Table")
                .dependsOn(dropDown);

        SimpleDependency app = new SimpleDependency("App")
                .dependsOn(table)
                .dependsOn(new SimpleDependency("Head")
                        .dependsOn(dropDown)
                        .dependsOn(user))
                .dependsOn(new SimpleDependency("Footer"));

        List<SimpleDependency> sorted = DependencySorting.sort(Collections.singleton(app));
        String sortedAsText = sorted.stream()
                .map(SimpleDependency::getValue)
                .collect(Collectors.joining("\n"));

        String expectedAsText = "" +
                "User\n" +
                "Dropdown\n" +
                "Table\n" +
                "Head\n" +
                "Footer\n" +
                "App";
        Assert.assertEquals(expectedAsText, sortedAsText);
        for (SimpleDependency dependency : sorted) {
            System.out.println(dependency.getValue());
        }
    }

    @Test
    public void testCyclicReference() {

        thrown.expectMessage("cyclic dependency chain: [App, Table, Dropdown, User, Table]");

        SimpleDependency user = new SimpleDependency("User");
        SimpleDependency dropDown = new SimpleDependency("Dropdown")
                .dependsOn(user);
        SimpleDependency table = new SimpleDependency("Table")
                .dependsOn(dropDown);
        user.dependsOn(table); // cyclic
        SimpleDependency app = new SimpleDependency("App")
                .dependsOn(table)
                .dependsOn(new SimpleDependency("Head")
                        .dependsOn(dropDown)
                        .dependsOn(user))
                .dependsOn(new SimpleDependency("Footer"));
        DependencySorting.sort(Collections.singleton(app));
    }

    private static class SimpleDependency implements Dependency<SimpleDependency> {
        private final String value;
        private final Collection<SimpleDependency> directDependencies = new ArrayList<>();

        SimpleDependency(String value) {
            this.value = value;
        }

        SimpleDependency dependsOn(SimpleDependency dep) {
            directDependencies.add(dep);
            return this;
        }

        public String toString() {
            return String.valueOf(value);
        }

        String getValue() {
            return value;
        }

        @Override
        public Collection<SimpleDependency> getDirectDependencies() {
            return directDependencies;
        }
    }
}
