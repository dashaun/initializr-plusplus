package dev.dashaun.shell.initializr.plusplus;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

final class HkProjectAnalyzer {

	private static final List<String> IGNORED_SEGMENTS = List.of(".git", "target", "node_modules", ".idea");

	private HkProjectAnalyzer() {
	}

	static ProjectAnalysis analyze(Path root, Model model) throws IOException {
		boolean hasJava = false;
		boolean hasMarkdown = false;
		boolean hasCompose = false;
		boolean hasSql = false;
		boolean hasGithubActions = false;
		boolean hasYaml = false;
		boolean hasShell = false;
		boolean hasDockerfile = false;

		try (Stream<Path> stream = Files.walk(root)) {
			List<Path> files = stream.filter(Files::isRegularFile).sorted(Comparator.naturalOrder()).toList();
			for (Path file : files) {
				Path relativePath = root.relativize(file);
				if (isIgnored(relativePath)) {
					continue;
				}

				String normalizedPath = relativePath.toString().replace('\\', '/');
				String fileName = relativePath.getFileName().toString();
				String lowerName = fileName.toLowerCase(Locale.ROOT);

				hasJava |= lowerName.endsWith(".java");
				hasMarkdown |= lowerName.endsWith(".md") || lowerName.endsWith(".markdown");
				hasCompose |= isComposeFile(normalizedPath, lowerName);
				hasSql |= lowerName.endsWith(".sql");
				hasGithubActions |= normalizedPath.startsWith(".github/workflows/")
						&& (lowerName.endsWith(".yml") || lowerName.endsWith(".yaml"));
				hasYaml |= lowerName.endsWith(".yml") || lowerName.endsWith(".yaml");
				hasShell |= (lowerName.endsWith(".sh") || lowerName.endsWith(".bash")) && !isIgnoredShellFile(fileName);
				hasDockerfile |= lowerName.equals("dockerfile") || lowerName.startsWith("dockerfile.");
			}
		}

		String sqlDialect = hasSql ? resolveSqlDialect(model).orElse(null) : null;
		return new ProjectAnalysis(hasJava, hasMarkdown, hasCompose, hasSql, hasGithubActions, hasYaml, hasShell,
				hasDockerfile, sqlDialect);
	}

	private static boolean isIgnored(Path relativePath) {
		for (Path segment : relativePath) {
			if (IGNORED_SEGMENTS.contains(segment.toString())) {
				return true;
			}
		}
		return false;
	}

	private static boolean isComposeFile(String normalizedPath, String lowerName) {
		if (!(lowerName.endsWith(".yml") || lowerName.endsWith(".yaml"))) {
			return false;
		}
		return normalizedPath.contains("/compose") || normalizedPath.contains("/docker-compose") || lowerName.startsWith("compose")
				|| lowerName.startsWith("docker-compose");
	}

	private static boolean isIgnoredShellFile(String fileName) {
		return "mvnw".equals(fileName) || "mvnw.cmd".equals(fileName);
	}

	private static Optional<String> resolveSqlDialect(Model model) {
		if (model == null || model.getDependencies() == null) {
			return Optional.empty();
		}
		for (Dependency dependency : model.getDependencies()) {
			String groupId = lower(dependency.getGroupId());
			String artifactId = lower(dependency.getArtifactId());
			if (groupId == null || artifactId == null) {
				continue;
			}
			if ("org.postgresql".equals(groupId) || "postgresql".equals(artifactId)) {
				return Optional.of("postgres");
			}
			if ("com.mysql".equals(groupId) || "mysql-connector-j".equals(artifactId)
					|| "mysql-connector-java".equals(artifactId)) {
				return Optional.of("mysql");
			}
			if ("org.mariadb.jdbc".equals(groupId) || artifactId.contains("mariadb")) {
				return Optional.of("mariadb");
			}
			if ("com.oracle.database.jdbc".equals(groupId) || "ojdbc11".equals(artifactId)
					|| "ojdbc8".equals(artifactId)) {
				return Optional.of("oracle");
			}
			if ("com.microsoft.sqlserver".equals(groupId) || artifactId.contains("sqlserver")) {
				return Optional.of("tsql");
			}
			if ("org.xerial".equals(groupId) && artifactId.contains("sqlite")) {
				return Optional.of("sqlite");
			}
			if ("com.h2database".equals(groupId) || "h2".equals(artifactId)) {
				return Optional.of("ansi");
			}
		}
		return Optional.empty();
	}

	private static String lower(String value) {
		return value == null ? null : value.toLowerCase(Locale.ROOT);
	}

	record ProjectAnalysis(boolean hasJava, boolean hasMarkdown, boolean hasCompose, boolean hasSql,
			boolean hasGithubActions, boolean hasYaml, boolean hasShell, boolean hasDockerfile, String sqlDialect) {
	}

}
