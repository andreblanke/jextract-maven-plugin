package andreblanke.maven.plugins;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public final class JextractMojoTest extends AbstractMojoTestCase {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @AfterEach
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testJextractSQLite3() throws Exception {
        var pom = getTestFile("src/test/resources/unit/pom.xml");

        Assertions.assertNotNull(pom);
        Assertions.assertTrue(pom.exists());

        var mojo = (JextractMojo) lookupMojo("jextract", pom);

        Assertions.assertNotNull(mojo);

        var targetPackageField = JextractMojo.class.getDeclaredField("targetPackage");

        targetPackageField.setAccessible(true);

        Assertions.assertEquals("andreblanke.maven.plugins", targetPackageField.get(mojo));
    }
}
