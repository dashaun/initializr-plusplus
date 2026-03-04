package dev.dashaun.shell.initializr.plusplus;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HkSupportTests {

	private final MavenXpp3Reader reader = new MavenXpp3Reader();

	@Test
	void analyzeDetectsFilesAndSqlDialect(@TempDir Path tempDir) throws Exception {
		Files.createDirectories(tempDir.resolve("src/main/java/example"));
		Files.createDirectories(tempDir.resolve("scripts"));
		Files.createDirectories(tempDir.resolve(".github/workflows"));
		Files.createDirectories(tempDir.resolve("db"));
		Files.writeString(tempDir.resolve("src/main/java/example/App.java"), "class App {}");
		Files.writeString(tempDir.resolve("README.md"), "# Readme");
		Files.writeString(tempDir.resolve("scripts/lint.sh"), "#!/usr/bin/env bash\n");
		Files.writeString(tempDir.resolve(".github/workflows/ci.yml"), "name: ci\n");
		Files.writeString(tempDir.resolve("compose.yml"), "services: {}\n");
		Files.writeString(tempDir.resolve("application.yaml"), "name: app\n");
		Files.writeString(tempDir.resolve("Dockerfile"), "FROM alpine\n");
		Files.writeString(tempDir.resolve("db/schema.sql"), "select 1;\n");
		Files.writeString(tempDir.resolve("mvnw"), "#!/usr/bin/env sh\n");

		Model model = readModel("""
				<project>
				  <modelVersion>4.0.0</modelVersion>
				  <groupId>example</groupId>
				  <artifactId>demo</artifactId>
				  <version>1.0.0</version>
				  <dependencies>
				    <dependency>
				      <groupId>org.postgresql</groupId>
				      <artifactId>postgresql</artifactId>
				      <version>42.7.4</version>
				    </dependency>
				  </dependencies>
				</project>
				""");

		HkProjectAnalyzer.ProjectAnalysis analysis = HkProjectAnalyzer.analyze(tempDir, model);

		assertTrue(analysis.hasJava());
		assertTrue(analysis.hasMarkdown());
		assertTrue(analysis.hasCompose());
		assertTrue(analysis.hasSql());
		assertTrue(analysis.hasGithubActions());
		assertTrue(analysis.hasYaml());
		assertTrue(analysis.hasShell());
		assertTrue(analysis.hasDockerfile());
		assertEquals("postgres", analysis.sqlDialect());
	}

	@Test
	void analyzeIgnoresMavenWrappersForShellDetection(@TempDir Path tempDir) throws Exception {
		Files.writeString(tempDir.resolve("mvnw"), "#!/usr/bin/env sh\n");
		Files.writeString(tempDir.resolve("mvnw.cmd"), "@echo off\n");

		HkProjectAnalyzer.ProjectAnalysis analysis = HkProjectAnalyzer.analyze(tempDir, null);

		assertFalse(analysis.hasShell());
	}

	@Test
	void miseTomlEditorUpsertsSettingsIntoExistingFile() {
		String content = """
				[tools]
				java = "graalvm-community-25"
				""";

		String updated = MiseTomlEditor.upsertAssignment(content, "tools", "hk", "\"latest\"");
		updated = MiseTomlEditor.upsertAssignment(updated, "tools", "github:google/google-java-format", "\"latest\"");
		updated = MiseTomlEditor.upsertAssignment(updated, "env", "HK_MISE", "1");
		updated = MiseTomlEditor.upsertAssignment(updated, "hooks", "enter", """
				'''
				if ! grep -q "hk run" .git/hooks/pre-commit 2>/dev/null; then
				  mise exec hk pkl -- hk install --mise
				fi
				'''""");
		updated = MiseTomlEditor.upsertAssignment(updated, "tasks.pre-commit", "run", "\"hk run pre-commit\"");

		assertTrue(updated.contains("java = \"graalvm-community-25\""));
		assertTrue(updated.contains("hk = \"latest\""));
		assertTrue(updated.contains("\"github:google/google-java-format\" = \"latest\""));
		assertTrue(updated.contains("[env]\nHK_MISE = 1"));
		assertTrue(updated.contains("[hooks]\nenter = '''"));
		assertTrue(updated.contains("[tasks.pre-commit]\nrun = \"hk run pre-commit\""));
	}

	@Test
	void hkConfigRendererAddsConditionalLintersAndTools() {
		HkProjectAnalyzer.ProjectAnalysis analysis = new HkProjectAnalyzer.ProjectAnalysis(true, true, true, true, true, true,
				true, true, "postgres");

		String hkConfig = HkConfigRenderer.renderHkConfig(analysis);
		List<HkConfigRenderer.Tool> tools = HkConfigRenderer.requiredTools(analysis);

		assertTrue(hkConfig.contains("[\"check-merge-conflict\"] = Builtins.check_merge_conflict"));
		assertTrue(hkConfig.contains("[\"check-added-large-files\"] = Builtins.check_added_large_files"));
		assertTrue(hkConfig.contains("[\"google_java_format\"] {"));
		assertTrue(hkConfig.contains("[\"markdown\"] = Builtins.markdown_lint"));
		assertTrue(hkConfig.contains("[\"dclint\"] {"));
		assertTrue(hkConfig.contains("[\"sql_fluff\"] = Builtins.sql_fluff"));
		assertTrue(hkConfig.contains("[\"actionlint\"] = Builtins.actionlint"));
		assertTrue(hkConfig.contains("[\"yaml\"] = Builtins.yamlfmt"));
		assertTrue(hkConfig.contains("[\"shellcheck\"] = Builtins.shellcheck"));
		assertTrue(hkConfig.contains("[\"hadolint\"] = Builtins.hadolint"));
		assertTrue(tools.stream().anyMatch(tool -> tool.key().equals("hk")));
		assertTrue(tools.stream().anyMatch(tool -> tool.key().equals("pkl")));
		assertTrue(tools.stream().anyMatch(tool -> tool.key().equals("github:google/google-java-format")));
		assertTrue(tools.stream().anyMatch(tool -> tool.key().equals("npm:markdownlint-cli")));
		assertTrue(tools.stream().anyMatch(tool -> tool.key().equals("npm:dclint")));
		assertTrue(tools.stream().anyMatch(tool -> tool.key().equals("actionlint")));
		assertTrue(tools.stream().anyMatch(tool -> tool.key().equals("yamlfmt")));
		assertTrue(tools.stream().anyMatch(tool -> tool.key().equals("shellcheck")));
		assertTrue(tools.stream().anyMatch(tool -> tool.key().equals("hadolint")));
		assertTrue(tools.stream().anyMatch(tool -> tool.key().equals("pipx:sqlfluff")));
	}

	@Test
	void sqlfluffConfigUsesProvidedDialect() {
		String config = HkConfigRenderer.renderSqlfluffConfig("postgres");

		assertTrue(config.contains("dialect = postgres"));
		assertTrue(config.contains("templater = raw"));
	}

	private Model readModel(String pom) throws Exception {
		return reader.read(new StringReader(pom));
	}

}
