package dev.blanke.maven.plugins;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.spi.ToolProvider;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Mojo that generates Java classes from C headers files by delegating to {@code jextract}.
 */
@Mojo(name = "jextract", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public final class JextractMojo extends AbstractMojo {

    @Parameter
    private List<String> clangArgs = new ArrayList<>();

    @Parameter(alias = "includes")
    private List<String> includePaths = new ArrayList<>();

    @Parameter(
        defaultValue = "${project.build.outputDirectory}",
        readonly     = true,
        required     = true)
    private File outputDirectory;

    @Parameter
    private File dumpIncludes;

    @Parameter(alias = "headerClass")
    private String headerClassName;

    @Parameter(alias = "functions")
    private List<String> includeFunctions = new ArrayList<>();

    @Parameter(alias = "macros")
    private List<String> includeMacros = new ArrayList<>();

    @Parameter(alias = "structs")
    private List<String> includeStructs = new ArrayList<>();

    @Parameter(alias = "typedefs")
    private List<String> includeTypedefs = new ArrayList<>();

    @Parameter(alias = "unions")
    private List<String> includeUnions = new ArrayList<>();

    @Parameter(alias = "vars")
    private List<String> includeVars = new ArrayList<>();

    @Parameter(alias = "libs")
    private List<String> libraries = new ArrayList<>();

    @Parameter(defaultValue = "${project.groupId}.${project.artifactId}", alias = "package")
    private String targetPackage;

    @Parameter(required = true, alias = "header")
    private File headerFile;

    @Override
    public void execute() throws MojoExecutionException {
        final var args = getJextractArgs();
        getLog().info("Running jextract with arguments: %s".formatted(Arrays.toString(args)));

        final int exitCode;
        try {
            exitCode = getJextractToolProvider().run(System.out, System.err, args);
        } catch (final ExceptionInInitializerError error) {
            if (!(error.getException() instanceof IllegalCallerException))
                throw error;
            throw new MojoExecutionException(
                "Please start Maven with the '--enable-native-access jdk.incubator.jextract' command-line option " +
                "by setting the MAVEN_OPTS environment variable or by using a .mvn/jvm.config file.",
                error);
        }

        if (exitCode != 0) {
            getLog().error(
                new MojoFailureException("jextract terminated with non-zero exit code: %d".formatted(exitCode)));
        }
    }

    /**
     * Retrieves a {@link ToolProvider} instance capable of running the {@code jextract} tool.
     *
     * @return A {@code ToolProvider} for running {@code jextract}.
     * @throws MojoExecutionException if no {@code ToolProvider} named {@code jextract} could be found.
     */
    private ToolProvider getJextractToolProvider() throws MojoExecutionException {
        try {
            return ToolProvider
                .findFirst("jextract")
                .orElseThrow(() -> new NoSuchElementException("Failed to locate jextract ToolProvider"));
        } catch (final NoSuchElementException exception) {
            /*
             * Re-throw the exception as a MojoExecutionException and add potential causes stating why the jextract
             * tool might not have been located.
             */
            if (ModuleFinder.ofSystem().find("jdk.incubator.jextract").isEmpty()) {
                getLog().info("The jdk.incubator.jextract module is absent.");
                throw new MojoExecutionException(
                    "Please ensure the Java runtime used to invoke Maven is recent enough and includes jextract.",
                    exception);
            } else {
                /*
                 * Since the jextract ToolProvider is located in an incubator module, it does not automatically
                 * participate in service binding and must therefore be manually added using the --add-modules
                 * command-line option.
                 *
                 * See https://stackoverflow.com/q/61307077 for a related StackOverflow issue.
                 */
                getLog().info("The jdk.incubator.jextract module is present.");
                throw new MojoExecutionException(
                    "Please start the Maven with the '--add-modules jdk.incubator.jextract' command-line option " +
                    "by setting the MAVEN_OPTS environment variable or by using a .mvn/jvm.config file.",
                    exception);
            }
        }
    }

    /**
     * Builds the command-line arguments passed to the {@code jextract} {@link ToolProvider} from the {@link Parameter}
     * fields of this Mojo.
     *
     * @return The command-line arguments passed to {@code jextract}.
     */
    private String[] getJextractArgs() {
        final var args = new ArrayList<String>();

        for (var clangArg : clangArgs) {
            args.add("-C");
            args.add(clangArg);
        }
        for (var includePath : includePaths) {
            args.add("-I");
            args.add(includePath);
        }

        args.add("-d");
        args.add(outputDirectory.getPath());

        if (dumpIncludes != null) {
            args.add("--dump-includes");
            args.add(dumpIncludes.getPath());
        }

        if (headerClassName != null) {
            args.add("--header-class-name");
            args.add(headerClassName);
        }

        for (var includeFunction : includeFunctions) {
            args.add("--include-function");
            args.add(includeFunction);
        }
        for (var includeMacro : includeMacros) {
            args.add("--include-macro");
            args.add(includeMacro);
        }
        for (var includeStruct : includeStructs) {
            args.add("--include-struct");
            args.add(includeStruct);
        }
        for (var includeTypedef : includeTypedefs) {
            args.add("--include-typedef");
            args.add(includeTypedef);
        }
        for (var includeUnion : includeUnions) {
            args.add("--include-union");
            args.add(includeUnion);
        }
        for (var includeVar : includeVars) {
            args.add("--include-var");
            args.add(includeVar);
        }

        for (var library : libraries) {
            args.add("-l");
            args.add(library);
        }

        if (targetPackage != null) {
            args.add("--target-package");
            args.add(targetPackage);
        }

        args.add(headerFile.getPath());

        return args.toArray(String[]::new);
    }
}
