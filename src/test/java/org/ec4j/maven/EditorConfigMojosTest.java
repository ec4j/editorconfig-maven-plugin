/**
 * Copyright (c) 2017 EditorConfig Maven Plugin
 * project contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ec4j.maven;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecution;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenRuntime.MavenRuntimeBuilder;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;

@RunWith(MavenJUnitTestRunner.class)
@MavenVersions({ "3.5.0" })
public class EditorConfigMojosTest {
    private static final Path basedir = Paths.get(System.getProperty("basedir", "."));
    @Rule
    public final TestResources resources = new TestResources();
    public final MavenRuntime verifier;

    public EditorConfigMojosTest(MavenRuntimeBuilder runtimeBuilder) throws Exception {
        this.verifier = runtimeBuilder //
                .withCliOptions("-Dorg.slf4j.simpleLogger.log." + CheckMojo.class.getPackage().getName() + "=trace") //
                .build();
    }

    @Test
    public void format() throws Exception {

        File projDir = resources.getBasedir("defaults");

        MavenExecution mavenExec = verifier.forProject(projDir) //
                .withCliOption("-B") // batch
        ;

        MavenExecutionResult result = mavenExec //
                .execute("clean", "editorconfig:format") //
                .assertErrorFreeLog() //
                .assertLogText(
                        "[TRACE] Processing file '.editorconfig' using validator org.ec4j.maven.validator.TextValidator") //
                .assertLogText("[DEBUG] No formatting violations found in file '.editorconfig'") //
                .assertLogText(
                        "[TRACE] Processing file 'pom.xml' using validator org.ec4j.maven.validator.TextValidator") //
                .assertLogText(
                        "[TRACE] Processing file 'pom.xml' using validator org.ec4j.maven.validator.XmlValidator") //
                .assertLogText("[DEBUG] No formatting violations found in file 'pom.xml'") //
                .assertLogText(
                        "[TRACE] Processing file 'src/main/java/org/ec4j/maven/it/defaults/App.java' using validator org.ec4j.maven.validator.TextValidator") //
                .assertLogText(
                        "[DEBUG] No formatting violations found in file 'src/main/java/org/ec4j/maven/it/defaults/App.java'") //
                .assertLogText(
                        "[TRACE] Processing file 'src/main/resources/trailing-whitespace.txt' using validator org.ec4j.maven.validator.TextValidator") //
                .assertLogText(
                        "[INFO] src/main/resources/trailing-whitespace.txt@1,7: Delete 2 characters - violates trim_trailing_whitespace = true, reported by org.ec4j.maven.validator.TextValidator") //
                .assertLogText(
                        "[TRACE] Processing file 'src/main/resources/indent.xml' using validator org.ec4j.maven.validator.TextValidator") //
                .assertLogText(
                        "[TRACE] Processing file 'src/main/resources/indent.xml' using validator org.ec4j.maven.validator.XmlValidator") //
                .assertLogText(
                        "[INFO] src/main/resources/indent.xml@23,5: Delete 1 character - violates indent_style = space, indent_size = 2, reported by org.ec4j.maven.validator.XmlValidator") //
                .assertLogText(
                        "[INFO] src/main/resources/indent.xml@24,3: Delete 2 characters - violates indent_style = space, indent_size = 2, reported by org.ec4j.maven.validator.XmlValidator") //
                .assertLogText(
                        "[TRACE] Processing file 'README.adoc' using validator org.ec4j.maven.validator.TextValidator") //
                .assertLogText(
                        "[INFO] README.adoc@2,1: Delete 2 characters - violates trim_trailing_whitespace = true, reported by org.ec4j.maven.validator.TextValidator") // ;
                .assertLogText("[INFO] Formatted 3 out of 6 files") //
        ;

        final Path actualBaseDir = result.getBasedir().toPath();
        final Path expectedBaseDir = basedir.resolve("src/test/projects/defaults-formatted");
        assertFilesEqual(actualBaseDir, expectedBaseDir, ".editorconfig");
        assertFilesEqual(actualBaseDir, expectedBaseDir, "pom.xml");
        assertFilesEqual(actualBaseDir, expectedBaseDir, "README.adoc");
        assertFilesEqual(actualBaseDir, expectedBaseDir, "src/main/java/org/ec4j/maven/it/defaults/App.java");
        assertFilesEqual(actualBaseDir, expectedBaseDir, "src/main/resources/indent.xml");
        assertFilesEqual(actualBaseDir, expectedBaseDir, "src/main/resources/trailing-whitespace.txt");

    }

    private void assertFilesEqual(Path actualBaseDir, Path expectedBaseDir, String relPath) throws IOException {
        final String contentActual = new String(Files.readAllBytes(actualBaseDir.resolve(relPath)), StandardCharsets.UTF_8);
        final String contentExpected = new String(Files.readAllBytes(expectedBaseDir.resolve(relPath)), StandardCharsets.UTF_8);
        Assert.assertEquals(relPath, contentExpected, contentActual);
    }

    @Test
    public void check() throws Exception {

        File projDir = resources.getBasedir("defaults");

        MavenExecution mavenExec = verifier.forProject(projDir) //
                .withCliOption("-B") // batch
        ;

        mavenExec //
                .execute("clean", "verify") //
                .assertLogText(
                        "[TRACE] Processing file '.editorconfig' using validator org.ec4j.maven.validator.TextValidator") //
                .assertLogText("[DEBUG] No formatting violations found in file '.editorconfig'") //
                .assertLogText(
                        "[TRACE] Processing file 'pom.xml' using validator org.ec4j.maven.validator.TextValidator") //
                .assertLogText(
                        "[TRACE] Processing file 'pom.xml' using validator org.ec4j.maven.validator.XmlValidator") //
                .assertLogText("[DEBUG] No formatting violations found in file 'pom.xml'") //
                .assertLogText(
                        "[TRACE] Processing file 'src/main/java/org/ec4j/maven/it/defaults/App.java' using validator org.ec4j.maven.validator.TextValidator") //
                .assertLogText(
                        "[DEBUG] No formatting violations found in file 'src/main/java/org/ec4j/maven/it/defaults/App.java'") //
                .assertLogText(
                        "[TRACE] Processing file 'src/main/resources/trailing-whitespace.txt' using validator org.ec4j.maven.validator.TextValidator") //
                .assertLogText(
                        "[ERROR] src/main/resources/trailing-whitespace.txt@1,7: Delete 2 characters - violates trim_trailing_whitespace = true, reported by org.ec4j.maven.validator.TextValidator") //
                .assertLogText(
                        "[TRACE] Processing file 'src/main/resources/indent.xml' using validator org.ec4j.maven.validator.TextValidator") //
                .assertLogText(
                        "[TRACE] Processing file 'src/main/resources/indent.xml' using validator org.ec4j.maven.validator.XmlValidator") //
                .assertLogText(
                        "[ERROR] src/main/resources/indent.xml@23,5: Delete 1 character - violates indent_style = space, indent_size = 2, reported by org.ec4j.maven.validator.XmlValidator") //
                .assertLogText(
                        "[ERROR] src/main/resources/indent.xml@24,3: Delete 2 characters - violates indent_style = space, indent_size = 2, reported by org.ec4j.maven.validator.XmlValidator") //
                .assertLogText(
                        "[TRACE] Processing file 'README.adoc' using validator org.ec4j.maven.validator.TextValidator") //
                .assertLogText(
                        "[ERROR] README.adoc@2,1: Delete 2 characters - violates trim_trailing_whitespace = true, reported by org.ec4j.maven.validator.TextValidator") //
                .assertLogText("[INFO] Checked 6 files") //
                .assertLogText("[INFO] BUILD FAILURE") //
                .assertLogText(
                        "There are .editorconfig violations. You may want to run mvn editorconfig:format to fix them automagically.") //
        ;
    }

}
