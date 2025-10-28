package dev.dashaun.shell.initializr.plusplus;

import dev.dashaun.shell.initializr.plusplus.models.Dependencies;
import dev.dashaun.shell.initializr.plusplus.models.Plugins;
import dev.dashaun.shell.initializr.plusplus.models.Profiles;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

	@Command
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

    @ShellMethod("Add Spring Data Gemfire")
    public String springDataGemfire() {
        try {
            Model model = reader.read(new FileReader(POM_FILE));

            // Add dependencies
            Dependencies.addSpringDataGemfire(model);

            // Write updated model to file
            MavenXpp3Writer writer = new MavenXpp3Writer();
            writer.write(new FileWriter(POM_FILE), model);
        }
        catch (XmlPullParserException | IOException e) {
            return "There was a problem adding Spring REST docs.";
        }
        return "Successfully added Spring REST docs";
    }

    @ShellMethod(value = "Remove empty tags from a pom.xml file", key = "clean-pom")
    public String cleanPom(
            @ShellOption(value = "--file", defaultValue = "pom.xml", help = "Path to the pom.xml file")
            String filePath) {

        try {
            File pomFile = new File(filePath);

            if (!pomFile.exists()) {
                return "Error: File not found: " + filePath;
            }

            // Read the pom.xml
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model;

            try (FileReader fileReader = new FileReader(pomFile)) {
                model = reader.read(fileReader);
            }

            // Clean empty elements
            int removedCount = cleanModel(model);

            // Write back the cleaned pom.xml
            MavenXpp3Writer writer = new MavenXpp3Writer();
            try (FileWriter fileWriter = new FileWriter(pomFile)) {
                writer.write(fileWriter, model);
            }

            return String.format("Successfully cleaned %s - removed %d empty tag(s)",
                    filePath, removedCount);

        } catch (Exception e) {
            return "Error cleaning pom.xml: " + e.getMessage();
        }
    }

    private int cleanModel(Model model) {
        AtomicInteger count = new AtomicInteger();

        // Clean dependencies with empty elements
        if (model.getDependencies() != null) {
            model.getDependencies().removeIf(dep -> {
                boolean isEmpty = (dep.getGroupId() == null || dep.getGroupId().trim().isEmpty()) &&
                        (dep.getArtifactId() == null || dep.getArtifactId().trim().isEmpty());
                if (isEmpty) return true;

                // Clean empty classifier, type, scope
                if (dep.getClassifier() != null && dep.getClassifier().trim().isEmpty()) {
                    dep.setClassifier(null);
                }
                if (dep.getType() != null && dep.getType().trim().isEmpty()) {
                    dep.setType(null);
                }
                if (dep.getScope() != null && dep.getScope().trim().isEmpty()) {
                    dep.setScope(null);
                }
                if (dep.getSystemPath() != null && dep.getSystemPath().trim().isEmpty()) {
                    dep.setSystemPath(null);
                }

                // Clean empty exclusions
                if (dep.getExclusions() != null) {
                    dep.getExclusions().removeIf(ex ->
                            (ex.getGroupId() == null || ex.getGroupId().trim().isEmpty()) &&
                                    (ex.getArtifactId() == null || ex.getArtifactId().trim().isEmpty())
                    );
                    if (dep.getExclusions().isEmpty()) {
                        dep.setExclusions(null);
                    }
                }

                return false;
            });
        }

        // Clean properties with empty values
        if (model.getProperties() != null) {
            List<String> keysToRemove = new ArrayList<>();
            model.getProperties().forEach((key, value) -> {
                if (value == null || value.toString().trim().isEmpty()) {
                    keysToRemove.add(key.toString());
                    count.getAndIncrement();
                }
            });
            keysToRemove.forEach(model.getProperties()::remove);
        }

        // Clean empty modules
        if (model.getModules() != null) {
            int beforeSize = model.getModules().size();
            model.getModules().removeIf(module -> module == null || module.trim().isEmpty());
            count.addAndGet((beforeSize - model.getModules().size()));
        }

        // Clean developer fields and remove completely empty developers
        if (model.getDevelopers() != null) {
            model.getDevelopers().forEach(dev -> {
                if (dev.getName() != null && dev.getName().trim().isEmpty()) {
                    dev.setName(null);
                }
                if (dev.getEmail() != null && dev.getEmail().trim().isEmpty()) {
                    dev.setEmail(null);
                }
                if (dev.getOrganization() != null && dev.getOrganization().trim().isEmpty()) {
                    dev.setOrganization(null);
                }
                if (dev.getUrl() != null && dev.getUrl().trim().isEmpty()) {
                    dev.setUrl(null);
                }
                if (dev.getId() != null && dev.getId().trim().isEmpty()) {
                    dev.setId(null);
                }
            });

            // Remove completely empty developers
            int beforeSize = model.getDevelopers().size();
            model.getDevelopers().removeIf(dev ->
                    (dev.getName() == null || dev.getName().trim().isEmpty()) &&
                            (dev.getEmail() == null || dev.getEmail().trim().isEmpty()) &&
                            (dev.getId() == null || dev.getId().trim().isEmpty()) &&
                            (dev.getOrganization() == null || dev.getOrganization().trim().isEmpty()) &&
                            (dev.getUrl() == null || dev.getUrl().trim().isEmpty()) &&
                            (dev.getRoles() == null || dev.getRoles().isEmpty())
            );
            count.addAndGet((beforeSize - model.getDevelopers().size()));

            // Remove developers list if empty
            if (model.getDevelopers().isEmpty()) {
                model.setDevelopers(null);
            }
        }

        // Clean contributor fields and remove completely empty contributors
        if (model.getContributors() != null) {
            model.getContributors().forEach(contrib -> {
                if (contrib.getName() != null && contrib.getName().trim().isEmpty()) {
                    contrib.setName(null);
                }
                if (contrib.getEmail() != null && contrib.getEmail().trim().isEmpty()) {
                    contrib.setEmail(null);
                }
                if (contrib.getOrganization() != null && contrib.getOrganization().trim().isEmpty()) {
                    contrib.setOrganization(null);
                }
                if (contrib.getUrl() != null && contrib.getUrl().trim().isEmpty()) {
                    contrib.setUrl(null);
                }
            });

            // Remove completely empty contributors
            int beforeSize = model.getContributors().size();
            model.getContributors().removeIf(contrib ->
                    (contrib.getName() == null || contrib.getName().trim().isEmpty()) &&
                            (contrib.getEmail() == null || contrib.getEmail().trim().isEmpty()) &&
                            (contrib.getOrganization() == null || contrib.getOrganization().trim().isEmpty()) &&
                            (contrib.getUrl() == null || contrib.getUrl().trim().isEmpty()) &&
                            (contrib.getRoles() == null || contrib.getRoles().isEmpty())
            );
            count.addAndGet((beforeSize - model.getContributors().size()));

            // Remove contributors list if empty
            if (model.getContributors().isEmpty()) {
                model.setContributors(null);
            }
        }

        // Clean license fields
        if (model.getLicenses() != null) {
            model.getLicenses().removeIf(license ->
                    (license.getName() == null || license.getName().trim().isEmpty()) &&
                            (license.getUrl() == null || license.getUrl().trim().isEmpty())
            );
        }

        // Clean SCM fields
        if (model.getScm() != null) {
            if (model.getScm().getConnection() != null && model.getScm().getConnection().trim().isEmpty()) {
                model.getScm().setConnection(null);
            }
            if (model.getScm().getDeveloperConnection() != null && model.getScm().getDeveloperConnection().trim().isEmpty()) {
                model.getScm().setDeveloperConnection(null);
            }
            if (model.getScm().getUrl() != null && model.getScm().getUrl().trim().isEmpty()) {
                model.getScm().setUrl(null);
            }
            if (model.getScm().getTag() != null && model.getScm().getTag().trim().isEmpty()) {
                model.getScm().setTag(null);
            }

            // Remove SCM if completely empty
            if ((model.getScm().getConnection() == null || model.getScm().getConnection().trim().isEmpty()) &&
                    (model.getScm().getDeveloperConnection() == null || model.getScm().getDeveloperConnection().trim().isEmpty()) &&
                    (model.getScm().getUrl() == null || model.getScm().getUrl().trim().isEmpty()) &&
                    (model.getScm().getTag() == null || model.getScm().getTag().trim().isEmpty())) {
                model.setScm(null);
                count.getAndIncrement();
            }
        }

        // Clean empty URL field
        if (model.getUrl() != null && model.getUrl().trim().isEmpty()) {
            model.setUrl(null);
            count.getAndIncrement();
        }

        // Clean empty name field
        if (model.getName() != null && model.getName().trim().isEmpty()) {
            model.setName(null);
            count.getAndIncrement();
        }

        // Clean empty description field
        if (model.getDescription() != null && model.getDescription().trim().isEmpty()) {
            model.setDescription(null);
            count.getAndIncrement();
        }

        return count.get();
    }
}