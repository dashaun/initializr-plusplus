package dev.dashaun.shell.initializr.plusplus;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.File;
import java.io.IOException;

import static dev.dashaun.shell.initializr.plusplus.Application.writeStringToFile;

@ShellComponent
//@ShellCommandGroup("./.mvn/")
public class MavenExtensionCommands {

  @ShellMethod("add jgitver extension and config")
  public String jgitver() {
    try {
      mavenConfigDir();
      extensionsConfig();
      jgitverConfig();
    } catch (IOException ioException) {
      return "There was a problem adding jgitver extension and config";
    }
    return "Successfully added jgitver extension and config in ./.mvn";
  }

  private void extensionsConfig() throws IOException {
    File file = new File("./.mvn/extensions.xml");
    if (!file.exists()) {
      writeStringToFile(extensionsFile(), file);
    }
  }

  private void jgitverConfig() throws IOException {
    File file = new File("./.mvn/jgitver.config.xml");
    if (!file.exists()) {
      writeStringToFile(jgitverConfigFile(), file);
    }
  }

  private String extensionsFile() {
    return """
                <extensions xmlns="http://maven.apache.org/EXTENSIONS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                            xsi:schemaLocation="http://maven.apache.org/EXTENSIONS/1.0.0 http://maven.apache.org/xsd/core-extensions-1.0.0.xsd">
                    <extension>
                        <groupId>fr.brouillard.oss</groupId>
                        <artifactId>jgitver-maven-plugin</artifactId>
                        <version>1.9.0</version>
                    </extension>
                </extensions>
                """;
  }

  private String jgitverConfigFile() {
    return """
                <configuration xmlns="http://jgitver.github.io/maven/configuration/1.1.0"
                               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                               xsi:schemaLocation="http://jgitver.github.io/maven/configuration/1.1.0 https://jgitver.github.io/maven/configuration/jgitver-configuration-v1_1_0.xsd">
                    <strategy>CONFIGURABLE</strategy>
                    <policy>NEAREST</policy>
                    <autoIncrementPatch>true</autoIncrementPatch>
                    <useCommitDistance>false</useCommitDistance>
                    <useDirty>false</useDirty>
                    <useGitCommitId>false</useGitCommitId>
                    <useSnapshot>false</useSnapshot> <!-- use -SNAPSHOT in CONFIGURABLE strategy -->
                    <gitCommitIdLength>8</gitCommitIdLength>  <!-- between [8,40] -->
                    <nonQualifierBranches>main</nonQualifierBranches> <!-- comma separated, example "master,integration" -->
                    <useDefaultBranchingPolicy>true</useDefaultBranchingPolicy>   <!-- uses jgitver#BranchingPolicy#DEFAULT_FALLBACK as fallback branch policy-->
                </configuration>
                """;
  }

  private void mavenConfigDir() throws IOException {
    File file = new File("./mvn");
    if (!file.exists()) {
      if (!file.mkdir()) {
        throw new IOException("Couldn't create directory");
      }
    }
  }
}
