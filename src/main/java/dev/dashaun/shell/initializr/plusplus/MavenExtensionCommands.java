package dev.dashaun.shell.initializr.plusplus;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.File;
import java.io.IOException;

import static dev.dashaun.shell.initializr.plusplus.Application.writeStringToFile;

@ShellComponent
public class MavenExtensionCommands {

    @ShellMethod("add mvn extensions and config")
    public String extensions() {
        try {
            mavenConfigDir();
            extensionsConfig();
            mavenGitVersioningExtension();
        } catch (IOException ioException) {
            return "There was a problem adding extensions and config";
        }
        return "Successfully added extensions and config in ./.mvn";
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
