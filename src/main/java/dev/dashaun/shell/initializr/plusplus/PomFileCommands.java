package dev.dashaun.shell.initializr.plusplus;

import dev.dashaun.shell.initializr.plusplus.models.*;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@ShellComponent
@ShellCommandGroup("./pom.xml")
public class PomFileCommands {

    private final static File POM_FILE = new File("./pom.xml");

    @ShellMethod("Update the project version")
    @ShellMethodAvailability("pomFile")
    public String projectVersion(@ShellOption(defaultValue = "0") String version) {
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(POM_FILE));
            model.setVersion(version);
            MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(new FileWriter(POM_FILE), model);
        } catch (XmlPullParserException | IOException e) {
            return "There was a problem updating the project version.";
        }
        return String.format("Successfully set project version to '%s'", version);
    }

    @ShellMethod("Update the project description")
    @ShellMethodAvailability("pomFile")
    public String projectDescription(@ShellOption(defaultValue = "") String description, boolean delete) {
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(POM_FILE));
            if (delete) {
                model.setDescription(null);
            } else {
                model.setDescription(description);
            }
            MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(new FileWriter(POM_FILE), model);
        } catch (XmlPullParserException | IOException e) {
            return "There was a problem updating the project description.";
        }
        return delete
                ? "Successfully removed the project description."
                : String.format("Successfully set project name to '%s'", description);
    }

    @ShellMethod("Update the project name")
    @ShellMethodAvailability("pomFile")
    public String projectName(@ShellOption(defaultValue = "${project.groupId}:${project.artifactId}") String name) {
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(POM_FILE));
            model.setName(name);
            MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(new FileWriter(POM_FILE), model);
        } catch (XmlPullParserException | IOException e) {
            return "There was a problem updating the project name.";
        }
        return String.format("Successfully set project name to '%s'", name);
    }

    @ShellMethod("Add AWS Lambda profile for Spring Cloud Functions")
    @ShellMethodAvailability("pomFile")
    public String lambdaProfile() {
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(POM_FILE));

            Profiles.removeLambdaProfile(model);
            model.getProfiles().add(Profiles.lambdaProfile());

            //Write updated model to file
            MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(new FileWriter(POM_FILE), model);

            //Create assembly configs
            ApplicationConfigCommands.writeJavaAssembly();
            ApplicationConfigCommands.writeNativeAssembly();

        } catch (XmlPullParserException | IOException e) {
            return "There was a problem configuring Lambda use.";
        }
        return "Successfully configure for Lambda use.";
    }

    @ShellMethod("Add a 'webflux' profile for Spring Cloud Functions")
    @ShellMethodAvailability("pomFile")
    public String webfluxProfile() {
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(POM_FILE));

            //Update starters
            Profiles.removeWebfluxProfile(model);
            model.getProfiles().add(Profiles.webfluxProfile());

            //Write updated model to file
            MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(new FileWriter(POM_FILE), model);
        } catch (XmlPullParserException | IOException e) {
            return "There was a problem updating for webflux functions.";
        }
        return "Successfully configured to use webflux functions";
    }


    @ShellMethod("Support for compiling Spring applications to native executables using the GraalVM native-image compiler.")
    @ShellMethodAvailability("pomFile")
    public String nativeMavenPlugin() {
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(POM_FILE));

            //Update plugins
            Plugins.addNativeMavenPlugin(model);

            //Write updated model to file
            MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(new FileWriter(POM_FILE), model);

        } catch (XmlPullParserException | IOException e) {
            return "There was a problem adding native-maven-plugin.";
        }
        return "Successfully added native-maven-plugin.";
    }

    @ShellMethod("Create Native OCI Images with paketobuildpacks/builder:tiny")
    @ShellMethodAvailability("pomFile")
    public String tinyBuildpackProfile() {
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(POM_FILE));

            //Update starters
            Profiles.removeNativeProfile(model);
            model.getProfiles().add(Profiles.nativeProfile());

            //Write updated model to file
            MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(new FileWriter(POM_FILE), model);

        } catch (XmlPullParserException | IOException e) {
            return "There was a problem configuring Spring Native.";
        }
        return "Successfully configured Spring Native.";
    }

    public Availability pomFile() {
        return POM_FILE.exists()
                ? Availability.available()
                : Availability.unavailable(String.format("%s does not exist", POM_FILE.getName()));
    }

}