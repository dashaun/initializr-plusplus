package dev.dashaun.shell.initializr.plusplus;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ShellComponent
public class OnboardingCommands {

	private static final String VAULT_ADDR = "http://127.0.0.1:8200";

	private static final String SECRETS_DIR = ".initializrplusplus";

	private static final String SECRETS_FILE = "secrets.properties";

	private static final String VAULT = "vault";

	private static final String VAULT_IMAGE = "hashicorp/vault:latest";

	private static final String VAULT_NETWORK = "vault";

	private static final String VAULT_PORT = "8200:8200";

	private static final String VAULT_VOLUME = "vault-data";

	private static final String VAULT_LOCAL_CONFIG = "VAULT_LOCAL_CONFIG={\"storage\":{\"file\":{\"path\":\"/vault/file\"}},\"listener\":[{\"tcp\":{\"address\":\"0.0.0.0:8200\",\"tls_disable\":1}}],\"ui\":true}";

	@ShellMethod(value = "Add vault OSS service to ~/compose.yaml", key = "onboard vault")
	public String onboardVault() {
		File composeFile = new File(System.getProperty("user.home"), "compose.yaml");

		try {
			Map<String, Object> compose = composeFile.exists() ? readCompose(composeFile) : new LinkedHashMap<>();

			@SuppressWarnings("unchecked")
			Map<String, Object> services = (Map<String, Object>) compose
				.computeIfAbsent("services", k -> new LinkedHashMap<>());

			services.remove(VAULT);

			Map<String, Object> vaultService = new LinkedHashMap<>();
			vaultService.put("image", VAULT_IMAGE);
			vaultService.put("ports", List.of(VAULT_PORT));
			vaultService.put("networks", List.of(VAULT_NETWORK));
			vaultService.put("environment", List.of(VAULT_LOCAL_CONFIG));
			vaultService.put("volumes", List.of(VAULT_VOLUME + ":/vault/file"));
			vaultService.put("cap_add", List.of("IPC_LOCK"));
			vaultService.put("command", "vault server -config=/vault/config");
			services.put(VAULT, vaultService);

			@SuppressWarnings("unchecked")
			Map<String, Object> networks = (Map<String, Object>) compose
				.computeIfAbsent("networks", k -> new LinkedHashMap<>());
			Map<String, Object> vaultNetwork = new LinkedHashMap<>();
			vaultNetwork.put("driver", "bridge");
			networks.put(VAULT_NETWORK, vaultNetwork);

			@SuppressWarnings("unchecked")
			Map<String, Object> volumes = (Map<String, Object>) compose
				.computeIfAbsent("volumes", k -> new LinkedHashMap<>());
			volumes.putIfAbsent(VAULT_VOLUME, null);

			writeCompose(composeFile, compose);
		}
		catch (IOException e) {
			return "There was a problem updating " + composeFile.getAbsolutePath();
		}

		return "Successfully configured vault service in " + composeFile.getAbsolutePath();
	}

	@ShellMethod(value = "Start vault, initialize, and save credentials to ~/.initializrplusplus/secrets.properties", key = "onboard vault init")
	public String onboardVaultInit() {
		File composeFile = new File(System.getProperty("user.home"), "compose.yaml");
		if (!composeFile.exists()) {
			return "No ~/compose.yaml found. Run 'onboard vault' first.";
		}

		try {
			Process process = new ProcessBuilder("docker", "compose", "-f", composeFile.getAbsolutePath(), "up", "-d", VAULT)
				.redirectErrorStream(true)
				.start();
			if (process.waitFor() != 0) {
				return "Failed to start vault container.";
			}
		}
		catch (IOException | InterruptedException e) {
			return "Failed to start vault: " + e.getMessage();
		}

		HttpClient client = HttpClient.newHttpClient();
		ObjectMapper mapper = new ObjectMapper();

		boolean ready = false;
		for (int i = 0; i < 20; i++) {
			try {
				Thread.sleep(1000);
				HttpRequest req = HttpRequest.newBuilder().uri(URI.create(VAULT_ADDR + "/v1/sys/health")).GET().build();
				client.send(req, HttpResponse.BodyHandlers.discarding());
				ready = true;
				break;
			}
			catch (Exception ignored) {
			}
		}
		if (!ready) {
			return "Vault did not become reachable within timeout.";
		}

		try {
			HttpRequest req = HttpRequest.newBuilder().uri(URI.create(VAULT_ADDR + "/v1/sys/health")).GET().build();
			JsonNode health = mapper.readTree(client.send(req, HttpResponse.BodyHandlers.ofString()).body());
			if (health.path("initialized").asBoolean()) {
				return "Vault is already initialized. Credentials not overwritten.";
			}
		}
		catch (Exception e) {
			return "Failed to check vault status: " + e.getMessage();
		}

		String unsealKey;
		String rootToken;
		try {
			HttpRequest req = HttpRequest.newBuilder()
				.uri(URI.create(VAULT_ADDR + "/v1/sys/init"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\"secret_shares\":1,\"secret_threshold\":1}"))
				.build();
			JsonNode result = mapper.readTree(client.send(req, HttpResponse.BodyHandlers.ofString()).body());
			unsealKey = result.get("keys").get(0).asText();
			rootToken = result.get("root_token").asText();
		}
		catch (Exception e) {
			return "Failed to initialize vault: " + e.getMessage();
		}

		try {
			// Unseal
			client.send(HttpRequest.newBuilder()
				.uri(URI.create(VAULT_ADDR + "/v1/sys/unseal"))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\"key\":\"" + unsealKey + "\"}"))
				.build(), HttpResponse.BodyHandlers.discarding());

			// Enable KV v2 at secret/
			client.send(HttpRequest.newBuilder()
				.uri(URI.create(VAULT_ADDR + "/v1/sys/mounts/secret"))
				.header("X-Vault-Token", rootToken)
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\"type\":\"kv\",\"options\":{\"version\":\"2\"}}"))
				.build(), HttpResponse.BodyHandlers.discarding());

			// Write demo secret
			String demoSecret = "secret-" + UUID.randomUUID();
			client.send(HttpRequest.newBuilder()
				.uri(URI.create(VAULT_ADDR + "/v1/secret/data/demo"))
				.header("X-Vault-Token", rootToken)
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString("{\"data\":{\"value\":\"" + demoSecret + "\"}}"))
				.build(), HttpResponse.BodyHandlers.discarding());
		}
		catch (Exception e) {
			return "Vault initialized but failed to write demo secret: " + e.getMessage();
		}

		File secretsDir = new File(System.getProperty("user.home"), SECRETS_DIR);
		secretsDir.mkdirs();
		File secretsFile = new File(secretsDir, SECRETS_FILE);
		try (FileWriter writer = new FileWriter(secretsFile)) {
			writer.write("vault.TOKEN=" + rootToken + "\n");
			writer.write("vault.KEY=" + unsealKey + "\n");
		}
		catch (IOException e) {
			return "Vault initialized but failed to write credentials: " + e.getMessage();
		}

		return "Vault initialized. Credentials saved to " + secretsFile.getAbsolutePath();
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
