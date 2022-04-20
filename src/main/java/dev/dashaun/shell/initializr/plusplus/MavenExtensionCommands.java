package dev.dashaun.shell.initializr.plusplus;

import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@ShellComponent
@ShellCommandGroup("./.mvn/")
public class MavenExtensionCommands {

    @ShellMethod("add jgitver extension and config")
    public String jgitver() {
        if (mavenConfigDir() && extensionsConfig() && jgitverConfig()) {
            return "Successfully added jgitver extension and config in ./.mvn";
        }
        return "There was a problem adding jgitver extension and config";
    }

    private boolean writeStringToFile(String data, File file) {
        try {
            FileWriter fileWriter = new FileWriter(file, false);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(data);
            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException ioe) {
            return false;
        }
        return true;
    }

    private boolean extensionsConfig() {
        File file = new File("./.mvn/extensions.xml");
        if (file.exists()) {
            return true;
        }
        return writeStringToFile(extensionsFile(),file);
    }

    private boolean jgitverConfig() {
        File file = new File("./.mvn/jgitver.config.xml");
        if (file.exists()) {
            return true;
        }
        return writeStringToFile(jgitverConfigFile(),file);
    }

    private String extensionsFile(){
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

    private String jgitverConfigFile(){
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

    private boolean mavenConfigDir() {
        File file = new File("./mvn");
        if (file.exists()) {
            return true;
        }
        return file.mkdir();
    }
}
