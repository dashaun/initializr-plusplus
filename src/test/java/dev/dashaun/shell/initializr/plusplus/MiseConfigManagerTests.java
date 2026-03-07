package dev.dashaun.shell.initializr.plusplus;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static dev.dashaun.shell.initializr.plusplus.MiseTomlAssert.assertThat;

class MiseConfigManagerTests {

	@Test
	void writeJavaToolWritesToolsJava(@TempDir Path tempDir) {
		Path miseFile = tempDir.resolve("mise.toml");

		MiseConfigManager.writeJavaTool(miseFile, "graalvm-25");

		try (CommentedFileConfig config = CommentedFileConfig.builder(miseFile.toFile(), TomlFormat.instance()).sync().build()) {
			config.load();
			assertThat(config).hasTool("java", "graalvm-25");
		}
	}

	@Test
	void configureHkMergesExpectedSections(@TempDir Path tempDir) {
		Path miseFile = tempDir.resolve("mise.toml");
		MiseConfigManager.writeJavaTool(miseFile, "temurin-21");

		MiseConfigManager.configureHk(miseFile,
				List.of(new HkConfigRenderer.Tool("hk"), new HkConfigRenderer.Tool("pkl"),
						new HkConfigRenderer.Tool("github:google/google-java-format")));

			try (CommentedFileConfig config = CommentedFileConfig.builder(miseFile.toFile(), TomlFormat.instance()).sync().build()) {
				config.load();
			assertThat(config)
				.hasTool("java", "temurin-21")
				.hasTool("pkl", "latest")
				.hasTool("github:google/google-java-format", "latest")
				.hasToolField("hk", "version", "latest")
				.hasToolField("hk", "postinstall", "hk install --mise")
				.hasEnvVar("HK_MISE", 1)
				.hasTaskRun("lint:check", "hk run check")
				.hasTaskRun("lint:fix", "hk run fix")
				.doesNotHaveTask("pre-commit");
		}
	}

}
