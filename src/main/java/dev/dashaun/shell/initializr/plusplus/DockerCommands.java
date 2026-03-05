package dev.dashaun.shell.initializr.plusplus;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ShellComponent
public class DockerCommands {

	private static final String SPRING_CLOUD_CONFIG = "spring-cloud-config";

	private static final String SCC_IMAGE = "config:0";

	private static final String VAULT_NETWORK = "vault";

	private static final String DEFAULT_NETWORK = "default";

	@ShellMethod(value = "Add spring-cloud-config service to compose file", key = "docker compose scc")
	public String composeSpringCloudConfig() {
		File composeFile = findComposeFile();
		if (composeFile == null) {
			return "No compose.yml or compose.yaml found in the current directory.";
		}

		try {
			Map<String, Object> compose = readCompose(composeFile);

			@SuppressWarnings("unchecked")
			Map<String, Object> services = (Map<String, Object>) compose
				.computeIfAbsent("services", k -> new LinkedHashMap<>());

			services.remove(SPRING_CLOUD_CONFIG);

			@SuppressWarnings("unchecked")
			Map<String, Object> networks = (Map<String, Object>) compose
				.computeIfAbsent("networks", k -> new LinkedHashMap<>());

			if (networks.isEmpty()) {
				Map<String, Object> defaultNetwork = new LinkedHashMap<>();
				defaultNetwork.put("driver", "bridge");
				networks.put(DEFAULT_NETWORK, defaultNetwork);
			}

			if (!networks.containsKey(VAULT_NETWORK)) {
				Map<String, Object> vaultExternal = new LinkedHashMap<>();
				vaultExternal.put("external", true);
				networks.put(VAULT_NETWORK, vaultExternal);
			}

			Map<String, Object> sccService = new LinkedHashMap<>();
			sccService.put("image", SCC_IMAGE);
			sccService.put("environment", List.of(
				"SPRING_CLOUD_CONFIG_SERVER_VAULT_HOST=vault",
				"SPRING_CLOUD_CONFIG_SERVER_VAULT_PORT=8200"
			));
			sccService.put("networks", networks.containsKey(DEFAULT_NETWORK)
				? List.of(DEFAULT_NETWORK, VAULT_NETWORK)
				: List.of(VAULT_NETWORK));
			services.put(SPRING_CLOUD_CONFIG, sccService);

			writeCompose(composeFile, compose);
		}
		catch (IOException e) {
			return "There was a problem updating " + composeFile.getName();
		}

		return "Successfully configured spring-cloud-config service in " + composeFile.getName();
	}

	private File findComposeFile() {
		File yml = new File("./compose.yml");
		if (yml.exists()) return yml;
		File yaml = new File("./compose.yaml");
		if (yaml.exists()) return yaml;
		return null;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> readCompose(File file) throws IOException {
		try (FileReader reader = new FileReader(file)) {
			Map<String, Object> result = new Yaml().load(reader);
			return result != null ? result : new LinkedHashMap<>();
		}
	}

	private void writeCompose(File file, Map<String, Object> compose) throws IOException {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setIndent(2);
		options.setPrettyFlow(true);
		try (FileWriter writer = new FileWriter(file)) {
			new Yaml(options).dump(compose, writer);
		}
	}

}
