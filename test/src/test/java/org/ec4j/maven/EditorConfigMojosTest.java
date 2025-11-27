/*
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;

public class EditorConfigMojosTest {

    private static final Path basedir = Paths.get(System.getProperty("basedir", "."));

    @Test
    public void check() throws Exception {

        final Verifier<EditorConfigCheckMojo> mavenExec = new Verifier<>(
                "defaults",
                EditorConfigCheckMojo.class,
                Arrays.asList("log.txt"));

        mavenExec //
                .execute() //
                .assertLogText("[TRACE] Processing file '.editorconfig' using linter org.ec4j.linters.TextLinter") //
                .assertLogText("[DEBUG] No formatting violations found in file '.editorconfig'") //
                .assertLogText("[TRACE] Processing file 'pom.xml' using linter org.ec4j.linters.TextLinter") //
                .assertLogText("[TRACE] Processing file 'pom.xml' using linter org.ec4j.linters.XmlLinter") //
                .assertLogText("[DEBUG] No formatting violations found in file 'pom.xml'") //
                .assertLogText(
                        "[TRACE] Processing file 'src/main/java/org/ec4j/maven/it/defaults/App.java' using linter org.ec4j.linters.TextLinter"
                                .replace('/', File.separatorChar)) //
                .assertLogText(
                        "[DEBUG] No formatting violations found in file 'src/main/java/org/ec4j/maven/it/defaults/App.java'"
                                .replace('/', File.separatorChar)) //
                .assertLogText(
                        "[TRACE] Processing file 'src/main/resources/trailing-whitespace.txt' using linter org.ec4j.linters.TextLinter"
                                .replace('/', File.separatorChar)) //
                .assertLogText(
                        "[ERROR] src/main/resources/trailing-whitespace.txt@1,7: Delete 2 characters - violates trim_trailing_whitespace = true, reported by org.ec4j.linters.TextLinter"
                                .replace('/', File.separatorChar)) //
                .assertLogText(
                        "[TRACE] Processing file 'src/main/resources/indent.xml' using linter org.ec4j.linters.TextLinter"
                                .replace('/', File.separatorChar)) //
                .assertLogText(
                        "[TRACE] Processing file 'src/main/resources/indent.xml' using linter org.ec4j.linters.XmlLinter"
                                .replace('/', File.separatorChar)) //
                .assertLogText(
                        "[ERROR] src/main/resources/indent.xml@23,5: Delete 1 character - violates indent_style = space, indent_size = 2, reported by org.ec4j.linters.XmlLinter"
                                .replace('/', File.separatorChar)) //
                .assertLogText(
                        "[ERROR] src/main/resources/indent.xml@24,3: Delete 2 characters - violates indent_style = space, indent_size = 2, reported by org.ec4j.linters.XmlLinter"
                                .replace('/', File.separatorChar)) //
                .assertLogText("[TRACE] Processing file 'README.adoc' using linter org.ec4j.linters.TextLinter") //
                .assertLogText(
                        "[ERROR] README.adoc@2,1: Delete 2 characters - violates trim_trailing_whitespace = true, reported by org.ec4j.linters.TextLinter") //
                .assertLogText("[INFO] Checked 6 files") //
                .assertLogText("[INFO] BUILD FAILURE") //
                .assertLogText("There are .editorconfig violations. You may want to run") //
                .assertLogText("    mvn editorconfig:format") //
                .assertLogText("to fix them automagically.") //
        ;
    }

    @Test
    public void encoding() throws Exception {
        final Verifier<EditorConfigCheckMojo> mavenExec = new Verifier<EditorConfigCheckMojo>(
                "encoding",
                EditorConfigCheckMojo.class,
                Arrays.asList("log.txt"));

        mavenExec //
                .execute() //
                .assertErrorFreeLog() //
                .assertLogText("[TRACE] Processing file '.editorconfig' using linter org.ec4j.linters.TextLinter") //
                .assertLogText("[TRACE] Creating a Resource for path '.editorconfig' with encoding 'UTF-8'") //
                .assertLogText("[DEBUG] No formatting violations found in file '.editorconfig'") //
                .assertLogText(
                        "[TRACE] Processing file 'src/main/resources/simplelogger.properties' using linter org.ec4j.linters.TextLinter"
                                .replace('/', File.separatorChar)) //
                .assertLogText(
                        "[TRACE] Creating a Resource for path 'src/main/resources/simplelogger.properties' with encoding 'ISO-8859-1'"
                                .replace('/', File.separatorChar)) //
                .assertLogText(
                        "[DEBUG] No formatting violations found in file 'src/main/resources/simplelogger.properties'"
                                .replace('/', File.separatorChar)) //
                .assertLogText("[INFO] Checked 3 files") //
        ;

    }

    @Test
    public void format() throws Exception {
        final Verifier<EditorConfigFormatMojo> mavenExec = new Verifier<>(
                "defaults",
                EditorConfigFormatMojo.class,
                Arrays.asList("log.txt"));
        final Path expectedBaseDir = basedir.resolve("src/test/projects/defaults-formatted");

        mavenExec //
                .execute() //
                .assertErrorFreeLog() //
                .assertLogText("[TRACE] Processing file '.editorconfig' using linter org.ec4j.linters.TextLinter") //
                .assertLogText("[DEBUG] No formatting violations found in file '.editorconfig'") //
                .assertLogText("[TRACE] Processing file 'pom.xml' using linter org.ec4j.linters.TextLinter") //
                .assertLogText("[TRACE] Processing file 'pom.xml' using linter org.ec4j.linters.XmlLinter") //
                .assertLogText("[DEBUG] No formatting violations found in file 'pom.xml'") //
                .assertLogText(
                        "[TRACE] Processing file 'src/main/java/org/ec4j/maven/it/defaults/App.java' using linter org.ec4j.linters.TextLinter"
                                .replace('/', File.separatorChar)) //
                .assertLogText(
                        "[DEBUG] No formatting violations found in file 'src/main/java/org/ec4j/maven/it/defaults/App.java'"
                                .replace('/', File.separatorChar)) //
                .assertLogText(
                        "[TRACE] Processing file 'src/main/resources/trailing-whitespace.txt' using linter org.ec4j.linters.TextLinter"
                                .replace('/', File.separatorChar)) //
                .assertLogText(
                        "[INFO] src/main/resources/trailing-whitespace.txt@1,7: Delete 2 characters - violates trim_trailing_whitespace = true, reported by org.ec4j.linters.TextLinter"
                                .replace('/', File.separatorChar)) //
                .assertLogText(
                        "[TRACE] Processing file 'src/main/resources/indent.xml' using linter org.ec4j.linters.TextLinter"
                                .replace('/', File.separatorChar)) //
                .assertLogText(
                        "[TRACE] Processing file 'src/main/resources/indent.xml' using linter org.ec4j.linters.XmlLinter"
                                .replace('/', File.separatorChar)) //
                .assertLogText(
                        "[INFO] src/main/resources/indent.xml@23,5: Delete 1 character - violates indent_style = space, indent_size = 2, reported by org.ec4j.linters.XmlLinter"
                                .replace('/', File.separatorChar)) //
                .assertLogText(
                        "[INFO] src/main/resources/indent.xml@24,3: Delete 2 characters - violates indent_style = space, indent_size = 2, reported by org.ec4j.linters.XmlLinter"
                                .replace('/', File.separatorChar)) //
                .assertLogText("[TRACE] Processing file 'README.adoc' using linter org.ec4j.linters.TextLinter") //
                .assertLogText(
                        "[INFO] README.adoc@2,1: Delete 2 characters - violates trim_trailing_whitespace = true, reported by org.ec4j.linters.TextLinter") // ;
                .assertLogText("[INFO] Formatted 3 out of 6 files") //
                .assertFilesEqual(expectedBaseDir,
                        ".editorconfig",
                        "pom.xml",
                        "README.adoc",
                        "src/main/java/org/ec4j/maven/it/defaults/App.java",
                        "src/main/resources/indent.xml",
                        "src/main/resources/trailing-whitespace.txt");

    }

    @Test
    public void submodulesProfileless() throws Exception {

        {
            final Verifier<EditorConfigCheckMojo> mavenExec = new Verifier<>(
                    "submodules",
                    EditorConfigCheckMojo.class,
                    Arrays.asList("log.txt"));
            mavenExec.mojo.modules = Arrays.asList("module-1", "module-2");

            mavenExec //
                    .execute() //
                    .assertErrorFreeLog()
                    .assertNoLogText(
                            "[TRACE] Processing file 'module-1/good-1.adoc' using linter org.ec4j.linters.TextLinter") //
                    .assertNoLogText(
                            "[TRACE] Processing file 'module-2/bad.xml' using linter org.ec4j.linters.TextLinter") //
                    .assertNoLogText("[TRACE] Processing file 'bad.xml' using linter org.ec4j.linters.TextLinter") //
            ;
        }
        {
            final Verifier<EditorConfigCheckMojo> mavenExec = new Verifier<>(
                    "submodules",
                    EditorConfigCheckMojo.class,
                    Arrays.asList("log.txt"));

            mavenExec.mojo.basedir = mavenExec.mojo.basedir.toPath().resolve("module-1").toFile();

            mavenExec //
                    .execute() //
                    .assertErrorFreeLog()
                    .assertLogText("[TRACE] Processing file 'good-1.adoc' using linter org.ec4j.linters.TextLinter") //
            ;
        }
    }

    //    @Test
    //    public void submodulesWithModule2() throws Exception {
    //        final Verifier<EditorConfigCheckMojo> mavenExec = new Verifier<>(
    //                "submodules",
    //                EditorConfigCheckMojo.class,
    //                Arrays.asList("log.txt"));
    //        // .withCliOption("-Pwith-module-2") //
    //
    //        mavenExec //
    //                .execute() //
    //                .assertErrorFreeLog()
    //                .assertLogText("[TRACE] Processing file 'good-1.adoc' using linter org.ec4j.linters.TextLinter") //
    //                .assertNoLogText(
    //                        "[TRACE] Processing file 'module-1/good-1.adoc' using linter org.ec4j.linters.TextLinter") //
    //                .assertLogText("[TRACE] Processing file 'good.xml' using linter org.ec4j.linters.TextLinter") //
    //                .assertNoLogText(
    //                        "[TRACE] Processing file 'module-2/good.xml' using linter org.ec4j.linters.TextLinter") //
    //                .assertNoLogText(
    //                        "[TRACE] Processing file 'module-2/bad.xml' using linter org.ec4j.linters.TextLinter") //
    //                .assertNoLogText("[TRACE] Processing file 'bad.xml' using linter org.ec4j.linters.TextLinter") //
    //        ;
    //    }

    @Test
    public void excludesFile() throws Exception {
        final Verifier<EditorConfigCheckMojo> mavenExec = new Verifier<>(
                "excludes-file",
                EditorConfigCheckMojo.class,
                Arrays.asList("log.txt"));

        Path ignoreTxt = mavenExec.mojo.basedir.toPath().resolve("ignore.txt");
        mavenExec.mojo.excludesFile = ignoreTxt.toFile();

        mavenExec //
                .execute() //
                .assertErrorFreeLog() //
                .assertLogText("[DEBUG] Using excludesFile '" + ignoreTxt + "'");
    }

    static class Verifier<T extends AbstractEditorConfigMojo> {
        private final T mojo;
        private final LogRecorder logger;

        public Verifier(
                String projectDir,
                Class<T> mojoClass,
                List<String> excludes) throws IOException {

            final Path testingSrcDir = Paths.get("src/test/projects/" + projectDir);
            final Path testingProjectDir = Paths.get("target/" + projectDir + "-" + UUID.randomUUID());
            FileUtils.copyDirectory(testingSrcDir.toFile(), testingProjectDir.toFile());
            logger = new LogRecorder(testingProjectDir);
            if (mojoClass == EditorConfigCheckMojo.class) {
                mojo = (T) new EditorConfigCheckMojo(logger);
            } else if (mojoClass == EditorConfigFormatMojo.class) {
                mojo = (T) new EditorConfigFormatMojo(logger);
            } else {
                throw new IllegalStateException("Unexpected mojo type " + mojoClass);
            }
            mojo.encoding = StandardCharsets.UTF_8.name();
            mojo.basedir = testingProjectDir.toAbsolutePath().normalize().toFile();
            mojo.excludes = excludes;
        }

        public LogRecorder execute() {
            try {
                mojo.execute();
            } catch (MojoExecutionException e) {
                logger.info("BUILD ERROR");
                for (String line : e.getMessage().split("[\r\n]+")) {
                    logger.messages.add(line);
                }
            } catch (MojoFailureException e) {
                logger.info("BUILD FAILURE");
                for (String line : e.getMessage().split("[\r\n]+")) {
                    logger.messages.add(line);
                }
            }
            return logger;
        }

    }

    static class LogRecorder extends LegacyAbstractLogger {

        final List<String> messages = new CopyOnWriteArrayList<>();

        private final Path basedir;

        public LogRecorder(Path basedir) {
            this.basedir = basedir;
        }

        public LogRecorder assertNoLogText(String message) {
            if (messages.contains(message)) {
                throw new AssertionError("The log should not contain\n\n    " + message + "\n\nbut contains\n\n    "
                        + messages.stream().collect(Collectors.joining("\n    ")) + "\n.");
            }
            return this;
        }

        public LogRecorder assertErrorFreeLog() {
            final String errors = messages.stream().filter(s -> s.startsWith("[ERROR] ")).collect(Collectors.joining("\n    "));
            if (!errors.isEmpty()) {
                throw new AssertionError("The log should not contain any errors but contains:\n\n    "
                        + errors + "\n.");
            }
            return this;
        }

        public LogRecorder assertFilesEqual(Path expectedDirectory, String... relativePaths) {
            for (String relPath : relativePaths) {
                Assertions.assertThat(basedir.resolve(relPath)).hasSameTextualContentAs(expectedDirectory.resolve(relPath));
            }
            return this;
        }

        public LogRecorder assertLogText(String message) {

            if (!messages.contains(message)) {
                throw new AssertionError("The log should contain\n\n    " + message + "\n\nbut contains only\n\n    "
                        + messages.stream().collect(Collectors.joining("\n    ")) + "\n.");
            }
            return this;
        }

        public boolean isTraceEnabled() {
            return true;
        }

        public boolean isDebugEnabled() {
            return true;
        }

        public boolean isInfoEnabled() {
            return true;
        }

        public boolean isWarnEnabled() {
            return true;
        }

        public boolean isErrorEnabled() {
            return true;
        }

        protected void handleNormalizedLoggingCall(Level level, Marker marker, String msg, Object[] args, Throwable throwable) {
            messages.add(String.format("[%s] %s", level, MessageFormatter.arrayFormat(msg, args).getMessage().trim()));
        }

        @Override
        protected String getFullyQualifiedCallerName() {
            return null;
        }
    }

}
