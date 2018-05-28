# Project Title

Allows moduled EcmaScript6 (ES6) rapid development without server-side rendering for Servlet-based projects

## Getting Started

this library provides a servlet that
1. scans your classpath for JS files and analyzes `import` statements to create dependency graph.
2. generates a list of `<script type="text/babel" src="/mymodule.js"></script>` tag definitions for you modules in order of their dependency relations.
3. returns module content making above work

for example you have 3 modules:
```
src/main/mysourcefolderforJs/
                             module1.js
                             module2.jsx
                             module3.jsx
```
and module1 imports module2 and module3.

servlet will generate following:
```
<script type="text/babel" src="js/module3.jsx"></script>
<script type="text/babel" src="js/module2.jsx"></script>
<script type="text/babel" src="js/module1.js"></script>
```  

You should use "./" based module locations and use a ";" at the end. You can't use mutual imports. 
```
import module1 from "./module1";
```

## Installing / Deployment

add 
```
    <dependencies>
        ...
        <dependency>
            <groupId>com.nplekhanov</groupId>
            <artifactId>es6dev4j</artifactId>
            <version>1.0</version>
        </dependency>
        ...
```
and
```
    <build>
    ...
        <resources>
        ...
            <resource>
                <directory>src/main/mysourcefolderforJs</directory>
            </resource>
            ...
        </resources>
        ...
```
to your `pom.xml`

map `com.nplekhanov.es6dev4j.jsloading.JsLoadingServlet` to a `/mywebjspath` path pattern in your web.xml or Spring MVC config

add 
```
    <script src="some3dpartymodule1.js"></script>
    <script src="some3dpartymodule2.js"></script>
    <script src="https://unpkg.com/babel-standalone@6.15.0/babel.min.js"></script>
    <jsp:include page="/mywebjspath?base=js&root=mypackage/index&provided=module1,module2"/>
```
to your jsp file. some3dpartymodule1.js and some3dpartymodule2.js stands for any third party libraries that you can use at right side of `import` statements in your ES6 code. such imports will be ignored by servlets.

### Prerequisites

You need to use any servlet container with JSP support

## Authors

* **Nikolay Plekhanov** - *Initial work* - [kkolyan](https://github.com/kkolyan)

See also the list of [contributors](https://github.com/kkolyan/es6dev4j/contributors) who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
