package dev.dashaun.shell.initializr.plusplus;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

public class MiseTomlAssert extends AbstractAssert<MiseTomlAssert, CommentedFileConfig> {

	private MiseTomlAssert(CommentedFileConfig actual) {
		super(actual, MiseTomlAssert.class);
	}

	public static MiseTomlAssert assertThat(CommentedFileConfig config) {
		return new MiseTomlAssert(config);
	}

	public MiseTomlAssert hasTool(String name, String version) {
		Assertions.assertThat(actual.<String>get("tools." + name))
			.as("mise tool '%s'", name)
			.isEqualTo(version);
		return this;
	}

	public MiseTomlAssert hasToolField(String name, String field, String value) {
		Assertions.assertThat(actual.<String>get("tools." + name + "." + field))
			.as("mise tool '%s' field '%s'", name, field)
			.isEqualTo(value);
		return this;
	}

	public MiseTomlAssert hasEnvVar(String key) {
		Assertions.assertThat(actual.contains("env." + key))
			.as("mise env var '%s'", key)
			.isTrue();
		return this;
	}

	public MiseTomlAssert hasEnvVar(String key, Object value) {
		Object actualValue = actual.get("env." + key);
		Assertions.assertThat(actualValue)
			.as("mise env var '%s'", key)
			.isEqualTo(value);
		return this;
	}

	public MiseTomlAssert hasTaskRun(String taskName, String runCommand) {
		Assertions.assertThat(actual.<String>get("tasks." + taskName + ".run"))
			.as("mise task '%s' run", taskName)
			.isEqualTo(runCommand);
		return this;
	}

	public MiseTomlAssert doesNotHaveTask(String taskName) {
		Assertions.assertThat(actual.contains("tasks." + taskName))
			.as("mise task '%s'", taskName)
			.isFalse();
		return this;
	}

	public MiseTomlAssert preservesSchemaComment() {
		Assertions.assertThat(actual.getComment(""))
			.as("root-level comment")
			.contains("schema");
		return this;
	}

}
