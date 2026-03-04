package dev.dashaun.shell.initializr.plusplus;

import java.util.ArrayList;
import java.util.List;

final class HkConfigRenderer {

	private HkConfigRenderer() {
	}

	static List<Tool> requiredTools(HkProjectAnalyzer.ProjectAnalysis analysis) {
		List<Tool> tools = new ArrayList<>();
		tools.add(new Tool("hk"));
		tools.add(new Tool("pkl"));
		if (analysis.hasJava()) {
			tools.add(new Tool("github:google/google-java-format"));
		}
		if (analysis.hasMarkdown()) {
			tools.add(new Tool("npm:markdownlint-cli"));
		}
		if (analysis.hasCompose()) {
			tools.add(new Tool("npm:dclint"));
		}
		if (analysis.hasGithubActions()) {
			tools.add(new Tool("actionlint"));
		}
		if (analysis.hasYaml()) {
			tools.add(new Tool("yamlfmt"));
		}
		if (analysis.hasShell()) {
			tools.add(new Tool("shellcheck"));
		}
		if (analysis.hasDockerfile()) {
			tools.add(new Tool("hadolint"));
		}
		if (analysis.sqlDialect() != null) {
			tools.add(new Tool("pipx:sqlfluff"));
		}
		return tools;
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

}
