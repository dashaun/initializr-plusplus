package dev.dashaun.shell.initializr.plusplus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.CommandRegistry;
import org.springframework.shell.MethodTarget;

import javax.validation.constraints.NotNull;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(properties = { "spring.shell.interactive.enabled=false" })
public class ApplicationTests {

	@Autowired
	private CommandRegistry commandRegistry;

	@Test
	public void jgitver() {
		final String command = "jgitver";
		final MethodTarget commandTarget = lookupCommand(commandRegistry, command);
		assertThat(commandTarget).isNotNull();
	}

	@Test
	public void projectDescription() {
		final String command = "project-description";
		final MethodTarget commandTarget = lookupCommand(commandRegistry, command);
		assertThat(commandTarget).isNotNull();
	}

	@Test
	public void projectName() {
		final String command = "project-name";
		final MethodTarget commandTarget = lookupCommand(commandRegistry, command);
		assertThat(commandTarget).isNotNull();
	}

	@Test
	public void projectVersion() {
		final String command = "project-version";
		final MethodTarget commandTarget = lookupCommand(commandRegistry, command);
		assertThat(commandTarget).isNotNull();
	}

	protected MethodTarget lookupCommand(@NotNull final CommandRegistry registry,
										 @NotNull final String command) {
		return registry.listCommands().get(command);
	}

}
