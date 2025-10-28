package dev.dashaun.shell.initializr.plusplus;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import static dev.dashaun.shell.initializr.plusplus.Application.writeStringToFile;

@ShellComponent
public class MavenExtensionCommands {

    private final static File GIT_IGNORE_FILE = new File("./.gitignore");


    @ShellMethod("add mvn extensions and config")
    public String extensions() {
        try {
            mavenConfigDir();
            extensionsConfig();
            mavenGitVersioningExtension();
            gitIgnoreVersionedPom();
        } catch (IOException ioException) {
            return "There was a problem adding extensions and config";
        }
        return "Successfully added extensions and config in ./.mvn";
    }

    private void gitIgnoreVersionedPom() throws IOException {
        final String VERSIONED_POM_PATTERN = ".git-versioned-pom.xml";
        final String GIT_IGNORE_ENTRY = """
                
                ### maven-git-versioning-extension
                .git-versioned-pom.xml
                """;

        if (!GIT_IGNORE_FILE.exists()) {
            writeStringToFile(gitignoreFile(), GIT_IGNORE_FILE);
            return;
        }

        if (!containsPattern(GIT_IGNORE_FILE, VERSIONED_POM_PATTERN)) {
            appendToFile(GIT_IGNORE_FILE, GIT_IGNORE_ENTRY);
        }

    }

    private boolean containsPattern(File file, String pattern) throws IOException {
        try (var lines = Files.lines(file.toPath())) {
            return lines.anyMatch(line -> line.contains(pattern));
        }
    }

    private void appendToFile(File file, String content) throws IOException {
        Files.writeString(
                file.toPath(),
                content,
                StandardOpenOption.APPEND
        );
    }

    private void extensionsConfig() throws IOException {
        File file = new File("./.mvn/extensions.xml");
        if (!file.exists()) {
            writeStringToFile(extensionsFile(), file);
        }
    }

    private void mavenGitVersioningExtension() throws IOException {
        File file = new File("./.mvn/maven-git-versioning-extension.xml");
        if (!file.exists()) {
            writeStringToFile(mavenGitVersioningExtensionConfig(), file);
        }
    }

    private String gitignoreFile() {
        return """
                HELP.md
                target/
                .mvn/wrapper/maven-wrapper.jar
                !**/src/main/**/target/
                !**/src/test/**/target/
                
                ### STS ###
                .apt_generated
                .classpath
                .factorypath
                .project
                .settings
                .springBeans
                .sts4-cache
                
                ### IntelliJ IDEA ###
                .idea
                *.iws
                *.iml
                *.ipr
                
                ### NetBeans ###
                /nbproject/private/
                /nbbuild/
                /dist/
                /nbdist/
                /.nb-gradle/
                build/
                !**/src/main/**/build/
                !**/src/test/**/build/
                
                ### VS Code ###
                .vscode/
                
                ###maven-git-versioning-extension
                .git-versioned-pom.xml
                """;
    }

    private String extensionsFile() {
        return """
                 <extensions xmlns="http://maven.apache.org/EXTENSIONS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                             xsi:schemaLocation="http://maven.apache.org/EXTENSIONS/1.0.0 http://maven.apache.org/xsd/core-extensions-1.0.0.xsd">
                     <extension>
                         <groupId>me.qoomon</groupId>
                         <artifactId>maven-git-versioning-extension</artifactId>
                         <version>9.11.0</version>
                     </extension>
                     <extension>
                         <groupId>kr.motd.maven</groupId>
                         <artifactId>os-maven-plugin</artifactId>
                         <version>1.7.1</version>
                     </extension>
                 </extensions>
                """;
    }

    private String mavenGitVersioningExtensionConfig() {
        return """
                <configuration xmlns="https://github.com/qoomon/maven-git-versioning-extension"
                               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                               xsi:schemaLocation="https://github.com/qoomon/maven-git-versioning-extension https://qoomon.github.io/maven-git-versioning-extension/configuration-9.4.0.xsd">
                
                    <refs considerTagsOnBranches="true">
                        <ref type="tag">
                            <pattern><![CDATA[v(?<version>.*)]]></pattern>
                            <version>${ref.version}</version>
                        </ref>
                        <ref type="branch">
                            <pattern>(main|release.*)</pattern>
                            <version>${describe.tag.version.major}.${describe.tag.version.minor}.${describe.tag.version.patch.next}-SNAPSHOT</version>
                        </ref>
                        <ref type="branch">
                            <pattern><![CDATA[feature/(?<feature>.+)]]></pattern>
                            <version>${describe.tag.version}-${ref.feature}-SNAPSHOT</version>
                        </ref>
                    </refs>
                    <rev>
                        <version>${commit}</version>
                    </rev>
                </configuration>
                """;
    }

    private void mavenConfigDir() throws IOException {
        File file = new File("./.mvn");
        if (!file.exists()) {
            if (!file.mkdir()) {
                throw new IOException("Couldn't create directory");
            }
        }
    }

}
