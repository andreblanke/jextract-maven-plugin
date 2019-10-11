package andreblanke.maven.plugins;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.spi.ToolProvider;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
     * --package-map <String>
     */
    //<editor-fold desc="JextractCliOptions">
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
        value  = "--no-locations",
        isFlag = true)
    private boolean noLocations;

    @Parameter(
        defaultValue = "${project.build.directory}/${project.artifactId}-${project.version}.jar",
        required     = true)
    @JextractCliOption("-o")
    private File outputFile;

    @Parameter
    @JextractCliOption(
        value  = "--record-library-path",
        isFlag = true)
    private boolean recordLibraryPath;

    /*
     * Note that this field annotated with the JextractCliOption annotation is not marked as a flag via "isFlag = true"
     * despite being of type boolean.
     * The reason for this is an inconsistency in the jextract CLI, as --static-forwarder, which defaults to true,
     * is specified as "--static-forwarder <Boolean>" instead of e.g. "--no-static-forwarder".
     */
    @Parameter
    @JextractCliOption("--static-forwarder")
    private boolean staticForwarder;

    @Parameter(
        defaultValue = "${project.build.outputDirectory}",
        required     = true)
    @JextractCliOption("--src-dump-dir")
    private File sourceDumpDirectory;

    @Parameter(
        defaultValue = "${project.groupId}",
        required     = true)
    @JextractCliOption("-t")
    private String targetPackage;
    //</editor-fold>

    @Parameter
    private List<File> headerFiles;

    @Override
    public void execute() throws MojoFailureException {
        String[] args = getJextractArgs();

        getLog().info(String.format("Running jextract with arguments: %s", Arrays.toString(args)));

        getJextractToolProvider().run(System.out, System.err, args);

        project.getArtifact().setFile(outputFile);
    }

    //<editor-fold desc="getJextractArgs()">
    @Contract("_ -> fail")
    @SuppressWarnings("unchecked")
    private static <T, E extends Exception> T throwUnchecked(Exception exception) throws E {
        throw (E) exception;
    }

    private List<String> getJextractCliOptionArgs() {
        return Arrays
            .stream(JextractMojo.class.getDeclaredFields())
            .filter(field -> field.getAnnotation(JextractCliOption.class) != null)
            .flatMap(field -> {
                final var annotation     = field.getAnnotation(JextractCliOption.class);
                final Class<?> fieldType = field.getType();

                if (annotation.isFlag()) {
                    if (boolean.class.isAssignableFrom(fieldType) || Boolean.class.isAssignableFrom(fieldType))
                        return Stream.of(annotation.value());
                    return throwUnchecked(new MojoExecutionException(this, "", ""));
                }

                try {
                    if (Collection.class.isAssignableFrom(fieldType)) {
                        var iterable = (Iterable<?>) field.get(this);

                        return
                            StreamSupport
                                .stream(iterable.spliterator(), false)
                                .flatMap(element -> Stream.of(annotation.value(), Objects.toString(element)));
                    }
                    return Stream.of(annotation.value(), Objects.toString(field.get(this)));
                } catch (final IllegalAccessException exception) {
                    /* Should not happen, as we are only accessing fields declared inside this class. */
                    return throwUnchecked(exception);
                }
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
    //</editor-fold>

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
