package com.nplekhanov.es6dev4j.jsloading;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

public class JsLoadingServlet extends HttpServlet {

    public static final String DIRECT_SOURCE_FILE_PATH_SYSTEM_PROPERTY = "es6dev4j.directSourceFilePath";

    private final JsLoader jsLoader;

    public JsLoadingServlet() {
        String directSourceFilePath = System.getProperty(DIRECT_SOURCE_FILE_PATH_SYSTEM_PROPERTY);
        jsLoader = new JsLoaderImpl(directSourceFilePath);
    }

    public JsLoadingServlet(JsLoader jsLoader) {
        this.jsLoader = jsLoader;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String path = request.getPathInfo();
        if (path == null) {
            String base = request.getParameter("base");
            String rootModule = request.getParameter("root");
            String v = request.getParameter("v");
            Collection<String> providedModules = new ArrayList<>();
            String provided = request.getParameter("provided");
            if (provided != null) {
                providedModules.addAll(Arrays.asList(provided.split(",")));
            }
            String scripts = Stream.concat(
                    Stream.of("" +
                            "<script>\n" +
                            "    var exports = {};// babel-standalone doesn't want to work with modules without this hack...\n" +
                            "</script>"),
                    jsLoader.collectDependentModuleNames(rootModule, providedModules).stream()
                            .map(scriptUri -> String.format("<script type=\"text/babel\" src=\"%s/%s?v=%s\"></script>",
                                    escapeHtml4(base), escapeHtml4(scriptUri), escapeHtml4(v)))
            )
                    .collect(Collectors.joining("\n"));
            response.getWriter().print(scripts);
            return;
        }
        response.setContentType("text/plain");
        response.setCharacterEncoding("utf-8");
        try {
            jsLoader.writeModuleToStream(path, response.getWriter());
        } catch (FileNotFoundException e) {
            response.sendError(404);
        }
    }
}