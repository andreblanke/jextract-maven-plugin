package dev.blanke.maven.plugins;

import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;

import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static com.soebes.itf.extension.assertj.MavenExecutionResultAssert.assertThat;

@MavenJupiterExtension
final class JextractMojoIT {

    @MavenTest
    void missing(final MavenExecutionResult result) {
        assertThat(result).isFailure();
    }

    @MavenTest
    void point(final MavenExecutionResult result) {
        assertThat(result).isSuccessful()
            .project()
            .hasTarget()
                .withJarFile()
                .containsOnlyOnce("org/jextract/point/point_h.class");
    }

    @MavenTest
    @EnabledOnOs(OS.LINUX)
    void unistd(final MavenExecutionResult result) {
        assertThat(result).isSuccessful()
            .project()
            .hasTarget()
                .withJarFile()
                .containsOnlyOnce("org/unix/unistd/unistd_h.class");
    }
}
