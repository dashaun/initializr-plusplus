package dev.dashaun.shell.initializr.plusplus;

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
    public String projectVersion(@ShellOption(defaultValue="0") String version) {
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(POM_FILE));
            model.setVersion(version);
            MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(new FileWriter(POM_FILE), model);
        } catch (XmlPullParserException | IOException e){
            return "There was a problem updating the project version.";
        }
        return String.format("Successfully set project version to '%s'", version);
    }

    @ShellMethod("Update the project name")
    @ShellMethodAvailability("pomFile")
    public String projectName(@ShellOption(defaultValue="${project.groupId}:${project.artifactId}") String name){
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(POM_FILE));
            model.setName(name);
            MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(new FileWriter(POM_FILE), model);
        } catch (XmlPullParserException | IOException e){
            return "There was a problem updating the project name.";
        }
        return String.format("Successfully set project name to '%s'", name);
    }

    public Availability pomFile() {
        return POM_FILE.exists()
                ? Availability.available()
                : Availability.unavailable(String.format("%s does not exist",POM_FILE.getName()));
    }

}
