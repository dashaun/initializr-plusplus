package dev.dashaun.shell.initializr.plusplus;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@ShellComponent
@ShellCommandGroup("./pom.xml")
public class PomFileCommands {

    private final static File POM_FILE = new File("./pom.xml");

    @ShellMethod("Update the artifact version")
    public String artifactVersion(@ShellOption(defaultValue="0") String version) {
        if (pomFile() && updateArtifactVersion(version)) {
            return String.format("Successfully set artifact version to '%s'", version);
        }
        return "There was a problem updating the artifact version";
    }

    private boolean pomFile() {
        return POM_FILE.exists();
    }

    private boolean updateArtifactVersion(String artifactVersion){
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(POM_FILE));
            model.setVersion(artifactVersion);
            MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(new FileWriter(POM_FILE), model);
        } catch (XmlPullParserException | IOException e){
            return false;
        }
        return true;
    }
}
