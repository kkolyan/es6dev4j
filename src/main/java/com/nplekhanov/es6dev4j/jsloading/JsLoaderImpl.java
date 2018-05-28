package com.nplekhanov.es6dev4j.jsloading;

import com.nplekhanov.es6dev4j.depsort.Dependency;
import com.nplekhanov.es6dev4j.depsort.DependencySorting;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsLoaderImpl implements JsLoader {

    private String jsSrcDirectFilePath;

    public JsLoaderImpl(@Nullable  String jsSrcDirectFilePath) {
        this.jsSrcDirectFilePath = jsSrcDirectFilePath;
    }

    public JsLoaderImpl() {
    }

    @Override
    public void writeModuleToStream(String moduleName, PrintWriter stream) throws IOException {

        JsResource resource = new JsResource(moduleName);
        if (!resource.getResource().exists()) {
            throw new FileNotFoundException(moduleName);
        }
        for (String line : IOUtils.readLines(resource.getResource().getInputStream(), "utf-8")) {
            if (line.startsWith("import ")) {
                stream.write("// doesn't needed, because all modules are already loaded globally // ");
            }
            stream.println(line);
        }
    }

    @Override
    public Collection<String> collectDependentModuleNames(String rootModule, Collection<String> providedModules) throws IOException {
        HashMap<String, JsResource> deps = new HashMap<>();
        collectDeps(rootModule, deps, providedModules, null);

        JsResource root = deps.get(rootModule);
        return DependencySorting.sort(Collections.singleton(root)).stream()
                .map(JsResource::getUri)
                .collect(Collectors.toList());
    }

    private void collectDeps(String depName, Map<String, JsResource> collected, Collection<String> provided, JsResource parent) throws IOException {
        if (collected.containsKey(depName)) {
            return;
        }
        if (provided.contains(depName)) {
            return;
        }

        JsResource dep = resolveResourceByModuleName(depName);

        String location = getParentUri(depName);

        for (String line : IOUtils.readLines(dep.getResource().getInputStream(), "utf-8")) {
            if (line.startsWith("import ")) {
                String directDep = line.replaceAll("import .* from \\\"(.*)\\\";", "$1");
                if (directDep.equals(line)) {
                    throw new IllegalStateException("can't parse " + dep + " and line \"" + line + "\"");
                }
                String childDepName;
                if (directDep.contains("/")) {
                    childDepName = normalizeUri(location + "/" + directDep);
                } else {
                    childDepName = directDep;
                }
                collectDeps(childDepName, collected, provided, dep);
            }
        }
        if (parent != null) {
            parent.getDirectDependencies().add(dep);
        }
        collected.put(depName, dep);
    }

    private String getParentUri(String depName) {
        int lastSlash = depName.lastIndexOf("/");
        String location;
        if (lastSlash >= 0) {
            location = depName.substring(0, lastSlash);
        } else {
            location = ".";
        }
        return location;
    }

    private String normalizeUri(String uri) {
        try {
            return new URI(uri)
                    .normalize()
                    .toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private JsResource resolveResourceByModuleName(String depName) throws FileNotFoundException {

        JsResource[] resources = Stream.of("js", "jsx")
                .map(ext -> depName + "." + ext)
                .map(JsResource::new)
                .filter(x -> x.getResource().exists())
                .toArray(JsResource[]::new);

        if (resources.length != 1) {
            throw new FileNotFoundException("can't found unique file for name " + depName + ". matched files: " + Arrays.toString(resources));
        }
        return resources[0];
    }

    private class JsResource implements Dependency<JsResource> {
        final String uri;
        final Resource resource;
        private final Collection<JsResource> directDependencies = new ArrayList<>();

        JsResource(String uri) {
            this.uri = uri;
            if (jsSrcDirectFilePath == null) {
                resource = new ClassPathResource(uri);
            } else {
                resource = new FileSystemResource(jsSrcDirectFilePath + uri);
            }
        }

        String getUri() {
            return uri;
        }

        Resource getResource() {
            return resource;
        }

        @Override
        public Collection<JsResource> getDirectDependencies() {
            return directDependencies;
        }
    }
}
