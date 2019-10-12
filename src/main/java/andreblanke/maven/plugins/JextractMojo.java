package andreblanke.maven.plugins;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.spi.ToolProvider;
import java.util.stream.Stream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@Mojo(
    name         = "jextract",
    defaultPhase = LifecyclePhase.PACKAGE)
public final class JextractMojo extends AbstractMojo {

    @Parameter(
        defaultValue = "${project}",
        readonly     = true,
        required     = true)
    private MavenProject project;

    /*
     * jextract CLI options which are not (yet) supported by this plugin:
     *
     * -?, -h, --help
     * --dry-run
     */

    // <editor-fold desc="JextractCliOptions">
    @Parameter
    @JextractCliOption("-C")
    private List<String> clangArgs;

    @Parameter
    @JextractCliOption("-I")
    private List<File> includeFiles;

    @Parameter
    @JextractCliOption("-L")
    private List<File> libraryPaths;

    @Parameter(
        defaultValue = "${project.build.outputDirectory}",
        required     = true)
    @JextractCliOption("-d")
    private File generatedClassFilesDirectory;

    @Parameter
    @JextractCliOption(
        value  = "--dry-run",
        isFlag = true)
    private boolean dryRun;

    @Parameter
    @JextractCliOption("--exclude-headers")
    private String excludeHeaders;

    @Parameter
    @JextractCliOption("--exclude-symbols")
    private String excludeSymbols;

    @Parameter
    @JextractCliOption("--include-headers")
    private String includeHeaders;

    @Parameter
    @JextractCliOption("--include-symbols")
    private String includeSymbols;

    @Parameter
    @JextractCliOption("-l")
    private List<String> libraries;

    @Parameter
    @JextractCliOption("--log")
    private String logLevel;

    @Parameter
    @JextractCliOption("--missing-symbols")
    private MissingSymbolAction missingSymbolAction;

    @Parameter
    @JextractCliOption(
        value     = "--no-locations",
        isFlag    = true)
    private boolean noLocations;

    @Parameter(
        defaultValue = "${project.build.directory}/${project.artifactId}-${project.version}.jar",
        required     = true)
    @JextractCliOption("-o")
    private File outputFile;

    @Parameter
    @JextractCliOption("--package-map")
    private Properties packageMapping;

    @Parameter
    @JextractCliOption(
        value     = "--record-library-path",
        isFlag    = true)
    private boolean recordLibraryPath;

    /*
     * Note that this field annotated with the JextractCliOption annotation is not marked as a flag via "isFlag = true"
     * despite being of type boolean.
     * The reason for this is an inconsistency in the jextract CLI, as --static-forwarder, which defaults to true,
     * is specified as "--static-forwarder <Boolean>" instead of e.g. "--no-static-forwarder".
     */
    /*
     * This parameter is of type Boolean instead of boolean to omit it from the output of getJextractArgs() unless it
     * is explicitly set.
     */
    @Parameter
    @JextractCliOption("--static-forwarder")
    private Boolean staticForwarder;

    @Parameter(
        defaultValue = "${project.build.directory}/generated-sources",
        required     = true)
    @JextractCliOption("--src-dump-dir")
    private File sourceDumpDirectory;

    @Parameter(defaultValue = "${project.groupId}")
    @JextractCliOption("-t")
    private String targetPackage;
    // </editor-fold>

    @Parameter
    private List<File> headerFiles;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Set<String> logLevelNames = getDefaultJavaUtilLoggingLevelNames();

        if (!logLevelNames.contains(logLevel))
            throw new MojoFailureException(
                this,
                String.format("Unknown %1$s name: '%2$s'.", Level.class.getName(), logLevel),
                String.format("Valid names include: %s", logLevelNames));
        final String[] args = getJextractArgs();

        getLog().info(String.format("Running jextract with arguments: %s", Arrays.toString(args)));

        getJextractToolProvider().run(System.out, System.err, args);

        if (!dryRun) {
            if (!outputFile.exists())
                throw new MojoExecutionException("");

            project.getArtifact().setFile(outputFile);
        }
    }

    private static Set<String> getDefaultJavaUtilLoggingLevelNames() {
        return Arrays.stream(Level.class.getFields())
            .filter(field -> Modifier.isStatic(field.getModifiers()) && Level.class.isAssignableFrom(field.getType()))
            .map(Field::getName)
            .collect(toSet());
    }

    // <editor-fold desc="getJextractArgs()">
    @Contract("_ -> fail")
    @SuppressWarnings("unchecked")
    private static <T, E extends Exception> T throwUnchecked(Exception exception) throws E {
        throw (E) exception;
    }

    private List<String> getJextractCliOptionArgs() {
        return Arrays.stream(JextractMojo.class.getDeclaredFields())
            .filter(field -> field.getAnnotation(JextractCliOption.class) != null)
            .flatMap(field -> {
                final Object fieldValue;

                final var annotation     = field.getAnnotation(JextractCliOption.class);
                final Class<?> fieldType = field.getType();

                try {
                    fieldValue = field.get(this);
                } catch (final IllegalAccessException exception) {
                    /*
                     * Should not happen, as we are only accessing fields declared inside this class but rethrow
                     * sneakily just in case.
                     */
                    return throwUnchecked(exception);
                }

                if (fieldValue == null)
                    return Stream.of();

                if (annotation.isFlag()) {
                    /* Fields of non-boolean types cannot be flag options. */
                    if (!boolean.class.isAssignableFrom(fieldType) && !Boolean.class.isAssignableFrom(fieldType))
                        return throwUnchecked(
                            new MojoFailureException(
                                String.format("Field '%s' is marked as flag but not of type boolean.", field.getName())));
                    return ((boolean) fieldValue) ? Stream.of(annotation.value()) : Stream.of();
                }

                if (Collection.class.isAssignableFrom(fieldType)) {
                    return
                        ((Collection<?>) fieldValue)
                            .stream()
                            .flatMap(element -> Stream.of(annotation.value(), Objects.toString(element)));
                }
                if (Properties.class.isAssignableFrom(fieldType)) {
                    return
                        ((Properties) fieldValue)
                            .entrySet()
                            .stream()
                            .flatMap(entry ->Stream.of(
                                annotation.value(),
                                String.format("%1$s=%2$s", entry.getKey(), entry.getValue())));
                }
                return Stream.of(annotation.value(), fieldValue.toString());
            })
            .collect(toList());
    }

    @NotNull
    private String[] getJextractArgs() {
        final List<String> cliOptionArgs = getJextractCliOptionArgs();

        cliOptionArgs.add("--");
        cliOptionArgs.addAll(
            headerFiles
                .stream()
                .map(File::toString)
                .collect(toList()));

        return cliOptionArgs.toArray(new String[0]);
    }
    // </editor-fold>

    @NotNull
    private ToolProvider getJextractToolProvider() throws MojoFailureException {
        return ToolProvider
            .findFirst("jextract")
            .orElseThrow(() -> new MojoFailureException(
                this,
                "jextract not found",
                "The jextract executable could not be found. Perhaps your JDK is outdated or configured incorrectly?"));
    }
}
