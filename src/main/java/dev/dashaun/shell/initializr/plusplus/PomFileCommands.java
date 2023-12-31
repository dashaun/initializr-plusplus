package dev.dashaun.shell.initializr.plusplus;

import dev.dashaun.shell.initializr.plusplus.models.*;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.*;

import java.io.*;

@ShellComponent
public class PomFileCommands {

	private final MavenXpp3Reader reader;

	public PomFileCommands() {
		this.reader = new MavenXpp3Reader();
	}

	private final static File POM_FILE = new File("./pom.xml");

	@ShellMethod("Update the project version")
	public String projectVersion(@ShellOption(defaultValue = "0") String version) {
		try {
			Model model = reader.read(new FileReader(POM_FILE));
			model.setVersion(version);
			MavenXpp3Writer writer = new MavenXpp3Writer();
			writer.write(new FileWriter(POM_FILE), model);
		}
		catch (XmlPullParserException | IOException e) {
			return "There was a problem updating the project version.";
		}
		return "Successfully set project version to '%s'".formatted(version);
	}

	@ShellMethod("Update the project description")
	public String projectDescription(@ShellOption(defaultValue = "") String description) {
		try {
			Model model = reader.read(new FileReader(POM_FILE));
			if ("".equals(description)) {
				model.setDescription(null);
			}
			else {
				model.setDescription(description);
			}
			MavenXpp3Writer writer = new MavenXpp3Writer();
			writer.write(new FileWriter(POM_FILE), model);
		}
		catch (XmlPullParserException | IOException e) {
			return "There was a problem updating the project description.";
		}
		return "".equals(description) ? "Successfully removed the project description."
				: "Successfully set project name to '%s'".formatted(description);
	}

	@ShellMethod("Update the project name")
	public String projectName(@ShellOption(defaultValue = "${project.groupId}.${project.artifactId}") String name) {
		try {
			Model model = reader.read(new FileReader(POM_FILE));
			model.setName(name);
			MavenXpp3Writer writer = new MavenXpp3Writer();
			writer.write(new FileWriter(POM_FILE), model);
		}
		catch (XmlPullParserException | IOException e) {
			return "There was a problem updating the project name.";
		}
		return "Successfully set project name to '%s'".formatted(name);
	}

	@ShellMethod("Add AWS Lambda profile for Spring Cloud Functions")
	public String lambdaProfile() {
		try {
			Model model = reader.read(new FileReader(POM_FILE));

			Profiles.removeLambdaProfile(model);
			model.getProfiles().add(Profiles.lambdaProfile());

			// Write updated model to file
			MavenXpp3Writer writer = new MavenXpp3Writer();
			writer.write(new FileWriter(POM_FILE), model);

			// Create assembly configs
			ApplicationConfigCommands.writeJavaAssembly();
			ApplicationConfigCommands.writeNativeAssembly();

		}
		catch (XmlPullParserException | IOException e) {
			return "There was a problem configuring Lambda use.";
		}
		return "Successfully configure for Lambda use.";
	}

	@ShellMethod("Add a 'webflux' profile for Spring Cloud Functions")
	public String webfluxProfile() {
		try {
			Model model = reader.read(new FileReader(POM_FILE));

			// Update starters
			Profiles.removeWebfluxProfile(model);
			model.getProfiles().add(Profiles.webfluxProfile());

			// Write updated model to file
			MavenXpp3Writer writer = new MavenXpp3Writer();
			writer.write(new FileWriter(POM_FILE), model);
		}
		catch (XmlPullParserException | IOException e) {
			return "There was a problem updating for webflux functions.";
		}
		return "Successfully configured to use webflux functions";
	}

	@ShellMethod("Support for GraalVM native-image compiler.")
	public String nativeMavenPlugin() {
		try {
			Model model = reader.read(new FileReader(POM_FILE));

			// Update plugins
			Plugins.addNativeMavenPlugin(model);

			// Write updated model to file
			MavenXpp3Writer writer = new MavenXpp3Writer();
			writer.write(new FileWriter(POM_FILE), model);

		}
		catch (XmlPullParserException | IOException e) {
			return "There was a problem adding native-maven-plugin.";
		}
		return "Successfully added native-maven-plugin.";
	}

	@ShellMethod("Add multi-architecture builder support.")
	public String multiArchBuilder() {
		try {
			Model model = reader.read(new FileReader(POM_FILE));

			// Update plugins
			Plugins.addMultiArchBuilder(model);

			// Write updated model to file
			MavenXpp3Writer writer = new MavenXpp3Writer();
			writer.write(new FileWriter(POM_FILE), model);

		}
		catch (XmlPullParserException | IOException e) {
			return "There was a problem adding multi-architecture builder support.";
		}
		return "Successfully added multi-architecture builder support.";
	}

	@ShellMethod("Use Zulu JDK for spring-boot:build-image with JVM args")
	public String zuluBuilder() {
		try {
			Model model = reader.read(new FileReader(POM_FILE));

			// Update plugins
			Plugins.addZuluBuilder(model);

			// Write updated model to file
			MavenXpp3Writer writer = new MavenXpp3Writer();
			writer.write(new FileWriter(POM_FILE), model);

		}
		catch (XmlPullParserException | IOException e) {
			return "There was a problem adding multi-architecture builder support.";
		}
		return "Successfully added multi-architecture builder support.";
	}

	@ShellMethod("Add Spring Java Format Maven Plugin and validate goal.")
	public String springFormat() {
		try {
			Model model = reader.read(new FileReader(POM_FILE));

			// Update plugins
			Plugins.addSpringFormat(model);

			// Write updated model to file
			MavenXpp3Writer writer = new MavenXpp3Writer();
			writer.write(new FileWriter(POM_FILE), model);
		}
		catch (XmlPullParserException | IOException e) {
			return "There was a problem adding Spring Java Format Maven Plugin.";
		}
		return "Successfully added Spring Java Format Maven Plugin.";
	}

	@ShellMethod("Add Spring REST Docs")
	public String springRestDocs() {
		try {
			Model model = reader.read(new FileReader(POM_FILE));

			// Update plugins
			Plugins.addSpringRESTDocs(model);

			// Write updated model to file
			MavenXpp3Writer writer = new MavenXpp3Writer();
			writer.write(new FileWriter(POM_FILE), model);
		}
		catch (XmlPullParserException | IOException e) {
			return "There was a problem adding Spring REST docs.";
		}
		return "Successfully added Spring REST docs";
	}

}