package dev.dashaun.shell.initializr.plusplus;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class MiseConfigManager {

	private MiseConfigManager() {
	}

	static void writeJavaTool(Path miseFile, String javaChoice) {
		try (CommentedFileConfig miseConfig = CommentedFileConfig.builder(miseFile.toFile(), TomlFormat.instance())
			.sync()
			.preserveInsertionOrder()
			.build()) {
			miseConfig.load();
			miseConfig.set("tools.java", javaChoice);
			reorderTopLevelToolsFirst(miseConfig);
			writeFlatToml(miseFile, miseConfig);
		}
	}

	static void configureHk(Path miseFile, List<HkConfigRenderer.Tool> requiredTools) {
		try (CommentedFileConfig miseConfig = CommentedFileConfig.builder(miseFile.toFile(), TomlFormat.instance())
			.sync()
			.preserveInsertionOrder()
			.build()) {
			miseConfig.load();
			for (HkConfigRenderer.Tool tool : requiredTools) {
				if ("hk".equals(tool.key())) {
					continue;
				}
				miseConfig.set("tools." + tool.key(), "latest");
			}
			miseConfig.set("tools.hk.version", "latest");
			miseConfig.set("tools.hk.postinstall", "hk install --mise");
			miseConfig.set("env.HK_MISE", 1);
			miseConfig.set("tasks.lint:check.run", "hk run check");
			miseConfig.set("tasks.lint:fix.run", "hk run fix");
			reorderTopLevelToolsFirst(miseConfig);
			writeFlatToml(miseFile, miseConfig);
		}
	}

	private static void reorderTopLevelToolsFirst(CommentedFileConfig config) {
		Map<String, Object> snapshot = new LinkedHashMap<>(config.valueMap());
		config.clear();
		if (snapshot.containsKey("tools")) {
			config.set("tools", snapshot.remove("tools"));
		}
		for (Map.Entry<String, Object> entry : snapshot.entrySet()) {
			config.set(entry.getKey(), entry.getValue());
		}
	}

	private static void writeFlatToml(Path miseFile, CommentedFileConfig config) {
		TomlWriter writer = new TomlWriter();
		writer.setIndent("");
		try (FileWriter fileWriter = new FileWriter(miseFile.toFile())) {
			writer.write(config, fileWriter);
		}
		catch (IOException e) {
			throw new IllegalStateException("Unable to write mise config file: " + miseFile, e);
		}
	}

}
