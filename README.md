# jextract-maven-plugin

A Maven plugin to enable the execution of Project Panama's [`jextract`](https://github.com/openjdk/jextract) tool as
part of a build, allowing the generation of Java bindings from native library headers.

## Requirements

- [Project Panama Early Access-Access Builds](https://jdk.java.net/panama/)
  - While the plugin can be built using JDK 17 and newer, the early access build must be used to invoke Maven so that
    the plugin can find the `jextract` [ToolProvider][1]. Make sure to set the `JAVA_HOME` environment variable
    accordingly and follow the steps in the [Allowing access to `jextract`](#allowing-access-to-jextract) section.
- [LLVM >= 9](https://releases.llvm.org/download.html)

## Usage

### Allowing access to `jextract`

Because `jextract` is still in the incubation stage, the JVM used to run Maven must be explicitly configured to resolve
the `jdk.incubator.jextract` module and to allow native access from it. This is accomplished using the below
command-line arguments:

```text
--add-modules jdk.incubator.jextract --enable-native-access jdk.incubator.jextract
```

To pass these arguments to Maven you can use either the `MAVEN_OPTS` environment variable or the `.mvn/jvm.config` file
which should be located in your project's top-level directory.

See https://maven.apache.org/configure.html for more details on configuring Maven and
https://openjdk.java.net/jeps/11 for why this is necessary.

These steps will no longer be necessary once `jextract` has left the incubation stage.

### Configuring the plugin

The plugin can be used like any other Maven plugin by adding it to the `plugins` block of your project's `pom.xml` and
defining an execution of the `jextract` goal:

```xml
<plugin>
  <groupId>dev.blanke.maven.plugins</groupId>
  <artifactId>jextract-maven-plugin</artifactId>
  <version>1.0-SNAPSHOT</version>
  <configuration>
    <!-- "header" is the only required configuration element. -->
    <header>...</header>
    <!--
      You may wish to change the target package that contains the generated bindings,
      which otherwise defaults to "${project.groupId}.${project.artifactId}".
    -->
    <package>...</package>
  </configuration>
  <executions>
    <execution>
      <id>jextract</id>
      <goals>
        <goal>jextract</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

If your Maven project consists of more than native bindings, it is recommended to keep the executions of the plugin
in a separate Maven module upon which the other modules depend.

The configuration elements exposed by the plugin mostly correspond to the command-line arguments of `jextract`,
the only two exceptions being the `--source` and `-d` arguments which are currently not supported.

<details>
<summary>Overview over all configuration elements</summary>

```xml
<configuration>
  <!--
    Path to the C header file for which native bindings should be generated.
    The alias "header" may be used.
  -->
  <headerFile>...</headerFile>
  <!-- List of arguments passed through to Clang. -->
  <clangArgs>
    <clangArg>...</clangArg>
  </clangArgs>
  <!--
    List of paths from which #include'd files of the headerFile should be resolved.
    The alias "includes" may be used.
  -->
  <includePaths>
    <includePath>...</includePath>
  </includePaths>
  <!--
    File into which included symbols should be dumped.
    See the "include[Functions,Macros,Structs,Typedefs,Unions,Vars]" elements below.
  -->
  <dumpIncludes>...</dumpIncludes>
  <!--
    Name of the class which should be generated for the headerFile.
    The alias "headerClass" may be used.
  -->
  <headerClassName>...</headerClassName>
  <!--
    List of libraries that will be loaded by the generated bindings.
    The alias "libs" may be used.
  -->
  <libraries>
    <library>...</library>
  </libraries>
  <!--
    List of functions to include in the generated bindings.
    The alias "functions" may be used.
  -->
  <includeFunctions>
    <includeFunction>...</includeFunction>
  </includeFunctions>
  <!--
    List of macros to include in the generated bindings.
    The alias "macros" may be used.
  -->
  <includeMacros>
    <includeMacro>...</includeMacro>
  </includeMacros>
  <!--
    List of structs to include in the generated bindings.
    The alias "structs" may be used.
  -->
  <includeStructs>
    <includeStruct>...</includeStruct>
  </includeStructs>
  <!--
    List of typedefs to include in the generated bindings.
    The alias "typedefs" may be used.
  -->
  <includeTypedefs>
    <includeTypedefs>...</includeTypedefs>
  </includeTypedefs>
  <!--
    List of unions to include in the generated bindings.
    The alias "unions" may be used.
  -->
  <includeUnions>
    <includeUnion>...</includeUnion>
  </includeUnions>
  <!--
    List of variables to include in the generated bindings.
    The alias "vars" may be used.
  -->
  <includeVars>
    <includeVar>...</includeVar>
  </includeVars>
  <!--
    Name of the package the generated classes should belong to.
    Instead of "targetPackage", the alias "package" may be used.
  -->
  <targetPackage>...</targetPackage>
</configuration>
```
</details>

## Examples

See the [integration tests](src/test/resources-its/dev/blanke/maven/plugins/JextractMojoIT) of the plugin for examples.

## License

This project is licensed under the Apache-2.0 license. See the [LICENSE](LICENSE) file for more information.

[1]: https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/spi/ToolProvider.html
