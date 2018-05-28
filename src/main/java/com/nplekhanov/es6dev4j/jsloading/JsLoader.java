package com.nplekhanov.es6dev4j.jsloading;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

public interface JsLoader {

    void writeModuleToStream(String name, PrintWriter stream) throws IOException;

    Collection<String> collectDependentModuleNames(String rootModule, Collection<String> providedModules) throws IOException;
}
