package dev.dashaun.shell.initializr.plusplus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

final class HkConfigRenderer {

	private HkConfigRenderer() {
	}

	private static final List<LinterDiscovery> LINTER_DISCOVERIES = List.of(
			new LinterDiscovery("github:google/google-java-format", "google_java_format",
					HkProjectAnalyzer.ProjectAnalysis::hasJava),
			new LinterDiscovery("npm:markdownlint-cli", "markdown", HkProjectAnalyzer.ProjectAnalysis::hasMarkdown),
			new LinterDiscovery("npm:dclint", "dclint", HkProjectAnalyzer.ProjectAnalysis::hasCompose),
			new LinterDiscovery("pipx:sqlfluff", "sql_fluff", analysis -> analysis.sqlDialect() != null),
			new LinterDiscovery("actionlint", "actionlint", HkProjectAnalyzer.ProjectAnalysis::hasGithubActions),
			new LinterDiscovery("yamlfmt", "yaml", HkProjectAnalyzer.ProjectAnalysis::hasYaml),
			new LinterDiscovery("shellcheck", "shellcheck", HkProjectAnalyzer.ProjectAnalysis::hasShell),
			new LinterDiscovery("hadolint", "hadolint", HkProjectAnalyzer.ProjectAnalysis::hasDockerfile));

	static List<Tool> requiredTools(HkProjectAnalyzer.ProjectAnalysis analysis) {
		List<Tool> tools = new ArrayList<>();
		tools.add(new Tool("hk"));
		tools.add(new Tool("pkl"));
		for (LinterDiscovery discovery : LINTER_DISCOVERIES) {
			if (discovery.predicate().test(analysis)) {
				tools.add(new Tool(discovery.toolKey()));
			}
		}
		return tools;
	}

	static List<String> discoveredLinterNames(HkProjectAnalyzer.ProjectAnalysis analysis) {
		List<String> linters = new ArrayList<>();
		for (LinterDiscovery discovery : LINTER_DISCOVERIES) {
			if (discovery.predicate().test(analysis)) {
				linters.add(discovery.linterName());
			}
		}
		return linters;
	}

	static String renderHkConfig(HkProjectAnalyzer.ProjectAnalysis analysis) {
		StringBuilder linters = new StringBuilder();
		appendLine(linters, "    [\"check-merge-conflict\"] = Builtins.check_merge_conflict");
		appendLine(linters, "    [\"check-added-large-files\"] = Builtins.check_added_large_files");
		appendLine(linters, "    [\"mise\"] = Builtins.mise");
		appendLine(linters, "    [\"mixed-line-ending\"] = Builtins.mixed_line_ending");
		appendLine(linters, "    [\"newlines\"] = Builtins.newlines");
		appendLine(linters, "    [\"pkl-format\"] = Builtins.pkl_format");
		appendLine(linters, "    [\"trailing-whitespace\"] = Builtins.trailing_whitespace");
		if (analysis.hasJava()) {
			appendLine(linters, """
					    ["google_java_format"] {
					      glob = List("**/*.java")
					      exclude = List("**/target/**")
					      check = "google-java-format --dry-run --set-exit-if-changed {{files}}"
					      fix = "google-java-format --replace {{files}}"
					      batch = true
					    }""");
		}
		if (analysis.hasMarkdown()) {
			appendLine(linters, "    [\"markdown\"] = Builtins.markdown_lint");
		}
		if (analysis.hasCompose()) {
			appendLine(linters, """
					    ["dclint"] {
					      glob =
					        List(
					          "**/compose*.yml",
					          "**/compose*.yaml",
					          "**/docker-compose*.yml",
					          "**/docker-compose*.yaml",
					        )
					      exclude = List("**/node_modules/**", "**/target/**")
					      check = "dclint {{files}}"
					      fix = "dclint --fix {{files}}"
					      batch = true
					    }""");
		}
		if (analysis.sqlDialect() != null) {
			appendLine(linters, "    [\"sql_fluff\"] = Builtins.sql_fluff");
		}
		if (analysis.hasGithubActions()) {
			appendLine(linters, "    [\"actionlint\"] = Builtins.actionlint");
		}
		if (analysis.hasYaml()) {
			appendLine(linters, "    [\"yaml\"] = Builtins.yamlfmt");
		}
		if (analysis.hasShell()) {
			appendLine(linters, "    [\"shellcheck\"] = Builtins.shellcheck");
		}
		if (analysis.hasDockerfile()) {
			appendLine(linters, "    [\"hadolint\"] = Builtins.hadolint");
		}

		return """
				amends "package://github.com/jdx/hk/releases/download/v1.36.0/hk@1.36.0#/Config.pkl"
				import "package://github.com/jdx/hk/releases/download/v1.36.0/hk@1.36.0#/Builtins.pkl"

				local linters = new Mapping<String, Step> {
				%s
				}

				hooks {
				    ["pre-commit"] {
				        fix = true
				        stash = "git"
				        steps = linters
				    }
				    ["fix"] {
				        fix = true
				        steps = linters
				    }
				    ["check"] {
				        steps = linters
				    }
				}
				""".formatted(linters.toString().stripTrailing());
	}

	static String renderSqlfluffConfig(String dialect) {
		return """
				[sqlfluff]
				dialect = %s
				templater = raw
				max_line_length = 100

				[sqlfluff:indentation]
				indented_joins = False
				tab_space_size = 2
				""".formatted(dialect);
	}

	private static void appendLine(StringBuilder builder, String value) {
		if (!builder.isEmpty()) {
			builder.append('\n');
		}
		builder.append(value);
	}

	record Tool(String key) {
	}

	private record LinterDiscovery(String toolKey, String linterName,
			Predicate<HkProjectAnalyzer.ProjectAnalysis> predicate) {
	}

}
