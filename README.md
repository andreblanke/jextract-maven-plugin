# jextract-maven-plugin

## Table of contents

 * [Table of contents](#table-of-contents)
 * [Usage](#usage)
     * [Configuration](#configuration)
 * [License](#license)

## Usage

Using the `jextract-maven-plugin` currently requires a Project Panama early-access JDK build,
which can be obtained by [building it from source](http://hg.openjdk.java.net/panama/dev/)
or by [downloading a pre-built binary](http://jdk.java.net/panama/).

The following commands are assumed to be executed with the `JAVA_HOME` environment variable
pointing to such an early-access JDK build.

### Configuration

Currently all configurable parameters of the `jextract-maven-plugin` are a wrapper for the
command line arguments of the `jextract` executable.

#### Default parameters

| Parameter name                 | `jextract` option(s)     | Default value                                                             | 
|--------------------------------|--------------------------|---------------------------------------------------------------------------|
| `generatedClassFilesDirectory` | `-d`                     | `${project.build.outputDirectory}`                                        |
| `outputFile`                   | `-o`                     | `${project.build.directory}/${project.artifactId}-${project.version}.jar` |
| `sourceDumpDirectory`          | `--src-dump-dir`         | `${project.build.directory}/generated-sources`                            |
| `targetPackage`                | `-t`, `--target-package` | `${project.groupId}`                                                      |

Unless changed, `${project.build.outputDirectory}` corresponds to `target/classes`, whereas
`${project.build.directory}` corresponds to `target`. 

## License

This project is licensed under the Apache-2.0 license. For more information see [LICENSE](LICENSE).
