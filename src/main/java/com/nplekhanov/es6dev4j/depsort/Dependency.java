package com.nplekhanov.es6dev4j.depsort;

import java.util.Collection;

public interface Dependency<T extends Dependency> {

    Collection<T> getDirectDependencies();

    boolean equals(Object o);

    int hashCode();
}
