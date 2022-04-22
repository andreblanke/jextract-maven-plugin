package dev.blanke.maven.plugins;

import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenExecutionResult;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

@MavenJupiterExtension
class JextractMojoIT {

    @MavenTest
    @EnabledOnOs({ OS.LINUX, OS.MAC })
    void unistd(final MavenExecutionResult result) {
        Assertions.assertTrue(result.isSuccessful());
    }
}
