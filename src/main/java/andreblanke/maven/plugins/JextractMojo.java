package andreblanke.maven.plugins;

import java.util.spi.ToolProvider;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "jextract")
public final class JextractMojo extends AbstractMojo {

    @Parameter(
        defaultValue = "${project}",
        readonly     = true,
        required     = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoFailureException {
        getJextractToolProvider();
    }

    private ToolProvider jextractToolProvider;
    private ToolProvider getJextractToolProvider() throws MojoFailureException {
        if (jextractToolProvider == null) {
            jextractToolProvider =
                ToolProvider.findFirst("jextract").orElseThrow(() -> new MojoFailureException(""));
        }
        return jextractToolProvider;
    }
}
