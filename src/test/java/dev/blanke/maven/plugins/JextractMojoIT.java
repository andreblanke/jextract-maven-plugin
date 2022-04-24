package dev.blanke.maven.plugins;

import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;

import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.*;

@MavenJupiterExtension
class JextractMojoIT {

    @MavenTest
    void point(final MavenExecutionResult result) {
        assertTrue(result.isSuccessful());
    }

    @MavenTest
    @EnabledOnOs({ OS.LINUX, OS.MAC })
    void unistd(final MavenExecutionResult result) {
        assertTrue(result.isSuccessful());
    }
}
