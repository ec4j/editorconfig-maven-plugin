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
package org.ec4j.maven.lint.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public final class Constants {

    /**
     * A list file patterns that match non-source files.
     * <p>
     * Based on <a href=
     * "https://github.com/mycila/license-maven-plugin/blob/19dd07fa44cfff69f1c07f73598c65bbe4a8438d/license-maven-plugin/src/main/java/com/mycila/maven/plugin/license/Default.java#L27">com.mycila.maven.plugin.license.Default</a>
     * class from {@code mycila/license-maven-plugin} by Mathieu Carbou and others, licensed under Apache License,
     * Version 2.0
     */
    public static final Set<String> DEFAULT_EXCLUDES = Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
            // Miscellaneous typical temporary files
            "**/*~", //
            "**/#*#", //
            "**/.#*", //
            "**/%*%", //
            "**/._*", //
            "**/.repository/**", //

            // CVS
            "**/CVS", //
            "**/CVS/**", //
            "**/.cvsignore", //

            // RCS
            "**/RCS", //
            "**/RCS/**", //

            // SCCS
            "**/SCCS", //
            "**/SCCS/**", //

            // Visual SourceSafe
            "**/vssver.scc", //

            // Subversion
            "**/.svn", //
            "**/.svn/**", //

            // Arch
            "**/.arch-ids", //
            "**/.arch-ids/**", //

            // Bazaar
            "**/.bzr", //
            "**/.bzr/**", //

            // SurroundSCM
            "**/.MySCMServerInfo", //

            // Mac
            "**/.DS_Store", //

            // Serena Dimensions Version 10
            "**/.metadata", //
            "**/.metadata/**", //

            // Mercurial
            "**/.hg", //
            "**/.hg/**", //

            // git
            "**/.git", //
            "**/.git/**", //

            // BitKeeper
            "**/BitKeeper", //
            "**/BitKeeper/**", //
            "**/ChangeSet", //
            "**/ChangeSet/**", //

            // darcs
            "**/_darcs", //
            "**/_darcs/**", //
            "**/.darcsrepo", //
            "**/.darcsrepo/**", //
            "**/-darcs-backup*", //
            "**/.darcs-temp-mail", //

            // maven project's temporary files
            "**/target/**", //
            "**/test-output/**", //
            "**/release.properties", //
            "**/dependency-reduced-pom.xml", //
            "**/release-pom.xml", //
            "**/pom.xml.releaseBackup", //

            // code coverage tools
            "**/cobertura.ser", //
            "**/.clover/**", //

            // Eclipse project files
            "**/.classpath", //
            "**/.project", //
            "**/.settings/**", //

            // IDEA projet files
            "**/*.iml", //
            "**/*.ipr", //
            "**/*.iws", //
            ".idea/**", //

            // Netbeans
            "**/nb-configuration.xml", //

            // binary files - images
            "**/*.jpg", //
            "**/*.png", //
            "**/*.gif", //
            "**/*.ico", //
            "**/*.bmp", //
            "**/*.tiff", //
            "**/*.tif", //
            "**/*.cr2", //
            "**/*.xcf", //

            // binary code
            "**/*.bin", //
            "**/*.class", //
            "**/*.exe", //
            "**/*.dll", //
            "**/*.so", //

            // checksum files
            "**/*.md5", //
            "**/*.sha1", //

            // binary files - archives
            "**/*.jar", //
            "**/*.zip", //
            "**/*.rar", //
            "**/*.tar", //
            "**/*.tar.gz", //
            "**/*.tar.bz2", //
            "**/*.gz", //

            // binary files - documents
            "**/*.xls", //
            "**/*.doc", //
            "**/*.ppt", //
            "**/*.odt", //
            "**/*.ods", //
            "**/*.pdf", //
            "**/*.xlsx", //
            "**/*.docx", //
            "**/*.pptx", //

            // keys and keystores
            "**/*.gpg", //
            "**/*.jks", //
            "**/*.keystore", //
            "**/*.p12", //
            "**/*.pfx", //


            // flash
            "**/*.swf", //

            // fonts
            "**/*.eot", //
            "**/*.ttf", //
            "**/*.woff" //

    )));

    private Constants() {
    }


}
