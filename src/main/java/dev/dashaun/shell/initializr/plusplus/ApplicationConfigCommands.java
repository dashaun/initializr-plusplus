package dev.dashaun.shell.initializr.plusplus;

import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.File;
import java.io.IOException;

import static dev.dashaun.shell.initializr.plusplus.Application.writeStringToFile;

@ShellComponent
@ShellCommandGroup("./src/main/resources/application.yaml")
public class ApplicationConfigCommands {

    private final static File APPLICATION_YAML = new File("./src/main/resources/application.yaml");
    private final static File APPLICATION_PROPS = new File("./src/main/resources/application.properties");

    private final static File SRC_ASSEMBLY_JAVA = new File("./src/assembly/java.xml");
    private final static File SRC_ASSEMBLY_NATIVE = new File("./src/assembly/native.xml");

    private final static File SRC_SHELL_JAVA = new File("./src/shell/java/bootstrap");
    private final static File SRC_SHELL_NATIVE = new File("./src/shell/native/bootstrap");


    @ShellMethod("expose all management endpoints via web")
    public String exposeMgmt() {
        try {
            mgmtEndpoints();
        } catch (IOException ioException) {
            return "There was a problem editing the application.yaml";
        }
        return "Successfully setup application.yaml with management endpoints";
    }

    public static void writeNativeAssembly() throws IOException {
        srcAssemblyDir();
        srcShellDir();
        writeStringToFile(nativeAssembly(), SRC_ASSEMBLY_NATIVE);
        writeStringToFile(nativeShell(), SRC_SHELL_NATIVE);
    }

    public static void writeJavaAssembly() throws IOException {
        srcAssemblyDir();
        srcShellDir();
        writeStringToFile(javaAssembly(), SRC_ASSEMBLY_JAVA);
        writeStringToFile(javaShell(), SRC_SHELL_JAVA);
    }

    private void mgmtEndpoints() throws IOException {
        if (APPLICATION_PROPS.exists()) {
            if (!APPLICATION_PROPS.delete()) {
                throw new IOException("Couldn't remove application.properties file");
            }
        }

        if (!APPLICATION_YAML.exists()) {
            writeStringToFile(applicationYaml(), APPLICATION_YAML);
        }
    }

    private String applicationYaml() {
        return """
                management:
                  endpoints:
                    enabled-by-default: true
                    health:
                      show-details: always
                    web:
                      exposure:
                        include: '*'
                """;
    }

    private static String javaAssembly() {
        return """
                <assembly xmlns="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.2"
                	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                	xsi:schemaLocation="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.2 https://maven.apache.org/xsd/assembly-1.1.2.xsd">
                	<id>java-zip</id>
                	<formats>
                		<format>zip</format>
                	</formats>
                	<baseDirectory></baseDirectory>
                	<fileSets>
                		<fileSet>
                			<directory>target/classes</directory>
                			<outputDirectory>/</outputDirectory>
                		</fileSet>
                		<fileSet>
                			<directory>src/shell/java</directory>
                			<outputDirectory>/</outputDirectory>
                			<useDefaultExcludes>true</useDefaultExcludes>
                			<fileMode>0775</fileMode>
                			<includes>
                				<include>bootstrap</include>
                			</includes>
                		</fileSet>
                	</fileSets>
                	<dependencySets>
                		<dependencySet>
                			<outputDirectory>/lib</outputDirectory>
                			<unpack>false</unpack>
                			<scope>runtime</scope>
                		</dependencySet>
                	</dependencySets>
                </assembly>
                """;
    }

    private static String javaShell() {
        return """
                #!/bin/sh
                                
                cd ${LAMBDA_TASK_ROOT:-.}
                                
                java -Dspring.main.web-application-type=none -Dlogging.level.org.springframework=DEBUG \\
                  -noverify -XX:TieredStopAtLevel=1 -Xss256K -XX:MaxMetaspaceSize=128M \\
                  -cp .:`echo lib/*.jar | tr ' ' :` com.example.demo.DemoApplication
                """;
    }

    private static String nativeAssembly() {
        return """
                <assembly xmlns="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.2"
                	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                	xsi:schemaLocation="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.2 https://maven.apache.org/xsd/assembly-1.1.2.xsd">
                	<id>native-zip</id>
                	<formats>
                		<format>zip</format>
                	</formats>
                	<baseDirectory></baseDirectory>
                	<fileSets>
                		<fileSet>
                			<directory>src/shell/native</directory>
                			<outputDirectory>/</outputDirectory>
                			<useDefaultExcludes>true</useDefaultExcludes>
                			<fileMode>0775</fileMode>
                			<includes>
                				<include>bootstrap</include>
                			</includes>
                		</fileSet>
                		<fileSet>
                			<directory>target</directory>
                			<outputDirectory>/</outputDirectory>
                			<useDefaultExcludes>true</useDefaultExcludes>
                			<fileMode>0775</fileMode>
                			<includes>
                				<include>demo</include>
                			</includes>
                		</fileSet>
                	</fileSets>
                </assembly>
                """;
    }

    private static String nativeShell() {
        return """
                #!/bin/sh
                                
                cd ${LAMBDA_TASK_ROOT:-.}
                                
                ./demo -Dlogging.level.org.springframework=DEBUG
                """;
    }

    private static void srcAssemblyDir() throws IOException {
        File file = new File("./src/assembly");
        if (!file.exists()) {
            if (!file.mkdir()) {
                throw new IOException("Could not create src/assembly");
            }
        }
    }

    private static void srcShellDir() throws IOException {
        File file = new File("./src/shell");
        if (!file.exists()) {
            if (!file.mkdir()) {
                throw new IOException("Could not create src/shell");
            }
        }
        File n = new File("./src/shell/native");
        if (!n.exists()) {
            if (!n.mkdir()) {
                throw new IOException("Could not create src/shell/native");
            }
        }
        File j = new File("./src/shell/java");
        if (!j.exists()) {
            if (!j.mkdir()) {
                throw new IOException("Could not create src/shell/java");
            }
        }
    }

}
