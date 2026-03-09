package dev.dashaun.shell.initializr.plusplus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dashaun.shell.initializr.plusplus.models.Plugins;
import org.jline.terminal.Terminal;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.springframework.shell.component.SingleItemSelector;
import org.springframework.shell.component.support.SelectorItem;
import org.springframework.shell.Availability;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.core.io.ResourceLoader;
import org.springframework.shell.style.TemplateExecutor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@ShellComponent
@RegisterReflectionForBinding(MiseCommands.MiseJvmEntry.class)
public class MiseCommands {

	private static final File POM_FILE = new File("./pom.xml");

	private static final File MISE_FILE = new File("./mise.toml");

	private static final List<String> GRAAL_JVM_IMPL = List.of("graalvm");

	private static final List<String> JAVA_PROPERTY_KEYS = List.of("java.version", "maven.compiler.release");

	private static final String DEFAULT_RELEASE_TYPE = "ga";

	private final MavenXpp3Reader reader = new MavenXpp3Reader();

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

	private final Terminal terminal;

	private final ResourceLoader resourceLoader;

	private final ObjectProvider<TemplateExecutor> templateExecutorProvider;

	public MiseCommands(Terminal terminal, ResourceLoader resourceLoader,
			ObjectProvider<TemplateExecutor> templateExecutorProvider) {
		this.terminal = terminal;
		this.resourceLoader = resourceLoader;
		this.templateExecutorProvider = templateExecutorProvider;
	}

	@ShellMethod(value = "Create mise.toml", key = "setup-mise")
	@ShellMethodAvailability("setupMiseJavaAvailability")
	public String setupMiseJava() {
		try {
			Model model;
			try (FileReader fileReader = new FileReader(POM_FILE)) {
				model = reader.read(fileReader);
			}
			boolean hasNativePlugin = Plugins.hasNativeMavenPlugin(model);
			Integer pomJavaMajor = resolvePomJavaMajor(model);
			String operatingSystem = resolveOperatingSystem();
			String architecture = resolveArchitecture();
			List<JavaOption> options = fetchJavaOptions(operatingSystem, architecture);
			if (options.isEmpty()) {
				return "No Java options were returned by mise-java for %s/%s.".formatted(operatingSystem, architecture);
			}

			List<JavaOption> filteredOptions = hasNativePlugin ? filterGraalCompatible(options) : options;
			if (filteredOptions.isEmpty()) {
				return hasNativePlugin
						? "native-maven-plugin was found, but no GraalVM-compatible JDK choices were returned by mise-java for %s/%s."
							.formatted(operatingSystem, architecture)
						: "No eligible JDK choices were returned by mise-java for %s/%s."
							.formatted(operatingSystem, architecture);
				}

				String defaultChoice = selectDefaultChoice(filteredOptions, pomJavaMajor, hasNativePlugin);
				String selectedChoice = promptForChoice(filteredOptions, defaultChoice, pomJavaMajor, hasNativePlugin);
					MiseConfigManager.writeJavaTool(MISE_FILE.toPath(), selectedChoice);
					return "Successfully created mise.toml with java = '%s'.".formatted(selectedChoice);
			}
			catch (Exception e) {
			return "There was a problem creating mise.toml: " + e.getMessage();
		}
	}

	@ShellMethod(value = "Configure hk in an existing mise.toml", key = "setup-mise-hk")
	@ShellMethodAvailability("setupMiseHkAvailability")
	public String setupMiseHk() {
		try {
			HkProjectAnalyzer.ProjectAnalysis analysis = HkProjectAnalyzer.analyze(Path.of("."), readPomModelIfPresent());
			MiseConfigManager.configureHk(MISE_FILE.toPath(), HkConfigRenderer.requiredTools(analysis));
			Files.writeString(Path.of("hk.pkl"), HkConfigRenderer.renderHkConfig(analysis));
			if (analysis.sqlDialect() != null) {
				Files.writeString(Path.of(".sqlfluff"), HkConfigRenderer.renderSqlfluffConfig(analysis.sqlDialect()));
			}
			String baseMessage = analysis.sqlDialect() == null
					? "Successfully configured hk in mise.toml and wrote hk.pkl."
					: "Successfully configured hk in mise.toml and wrote hk.pkl and .sqlfluff.";
			List<String> discoveredLinters = HkConfigRenderer.discoveredLinterNames(analysis);
			String infoMessage = discoveredLinters.isEmpty()
					? "Linters discovered and added to hk.pkl: none"
					: "Linters discovered and added to hk.pkl: " + String.join(", ", discoveredLinters);
			return baseMessage + "\n" + infoMessage;
		}
		catch (Exception e) {
			return "There was a problem configuring hk: " + e.getMessage();
		}
	}

	public Availability setupMiseJavaAvailability() {
		if (!POM_FILE.exists()) {
			return Availability.unavailable("pom.xml was not found in the current directory");
		}
		if (MISE_FILE.exists()) {
			return Availability.unavailable("mise.toml already exists in the current directory");
		}
		return Availability.available();
	}

	public Availability setupMiseHkAvailability() {
		if (!isCommandOnPath("mise")) {
			return Availability.unavailable("mise is not available on PATH");
		}
		if (!MISE_FILE.exists()) {
			return Availability.unavailable("mise.toml was not found in the current directory");
		}
		return Availability.available();
	}

	private List<JavaOption> fetchJavaOptions(String operatingSystem, String architecture)
			throws IOException, InterruptedException {
		String url = "https://mise-java.jdx.dev/jvm/%s/%s/%s.json"
			.formatted(DEFAULT_RELEASE_TYPE, operatingSystem, architecture);
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(30)).GET().build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			throw new IOException("mise-java API returned HTTP " + response.statusCode());
		}

		List<MiseJvmEntry> entries = objectMapper.readValue(response.body(), new TypeReference<>() {
		});
		Map<String, JavaOption> dedup = entries.stream()
			.filter(entry -> "jdk".equalsIgnoreCase(entry.imageType()))
			.map(entry -> {
				Integer major = parseJavaMajor(entry.javaVersion());
				if (entry.vendor() == null || major == null || major < 8) {
					return null;
				}
				String choice = entry.vendor() + "-" + major;
				return new JavaOption(choice, entry.vendor(), major, entry.jvmImpl());
			})
			.filter(option -> option != null)
			.collect(java.util.stream.Collectors.toMap(
					option -> option.choice().toLowerCase(Locale.ROOT),
					Function.identity(),
					(existing, ignored) -> existing,
					LinkedHashMap::new));

		return dedup.values()
			.stream()
			.sorted(Comparator.comparing(JavaOption::choice))
			.toList();
	}

	private List<JavaOption> filterGraalCompatible(List<JavaOption> options) {
		return options.stream()
			.filter(option -> option.jvmImpl() != null)
			.filter(option -> GRAAL_JVM_IMPL.contains(option.jvmImpl().toLowerCase(Locale.ROOT)))
			.toList();
	}

	private String selectDefaultChoice(List<JavaOption> options, Integer pomJavaMajor, boolean hasNativePlugin) {
		if (pomJavaMajor != null) {
			Optional<JavaOption> sameMajor = options.stream().filter(option -> option.major() == pomJavaMajor).findFirst();
			if (sameMajor.isPresent()) {
				return sameMajor.get().choice();
			}
		}
		if (hasNativePlugin) {
			Optional<JavaOption> preferredVendor = options.stream()
				.filter(option -> "graalvm".equalsIgnoreCase(option.vendor()) || "oracle-graalvm".equalsIgnoreCase(option.vendor())
						|| "mandrel".equalsIgnoreCase(option.vendor()))
				.findFirst();
			if (preferredVendor.isPresent()) {
				return preferredVendor.get().choice();
			}
		}
		return options.get(0).choice();
	}

	private String promptForChoice(List<JavaOption> options, String defaultChoice, Integer pomJavaMajor, boolean hasNativePlugin) {
		String title = "Select Java for mise.toml";
		if (pomJavaMajor != null && hasNativePlugin) {
			title = "Select Java for mise.toml (pom=%d, native-maven-plugin detected)".formatted(pomJavaMajor);
		}
		else if (pomJavaMajor != null) {
			title = "Select Java for mise.toml (pom=%d)".formatted(pomJavaMajor);
		}
		else if (hasNativePlugin) {
			title = "Select Java for mise.toml (native-maven-plugin detected)";
		}

		List<SelectorItem<String>> items = options.stream().map(option -> SelectorItem.of(option.choice(), option.choice())).toList();
		SingleItemSelector<String, SelectorItem<String>> selector = new SingleItemSelector<>(terminal, items, title, null);
		selector.setResourceLoader(resourceLoader);
		TemplateExecutor templateExecutor = templateExecutorProvider.getIfAvailable();
		if (templateExecutor != null) {
			selector.setTemplateExecutor(templateExecutor);
		}
		items.stream()
			.filter(item -> item.getItem().equalsIgnoreCase(defaultChoice))
			.findFirst()
			.ifPresent(selector::setDefaultExpose);

		SingleItemSelector.SingleItemSelectorContext<String, SelectorItem<String>> context = selector
			.run(SingleItemSelector.SingleItemSelectorContext.empty());
		return context.getResultItem().map(SelectorItem::getItem).orElse(defaultChoice);
	}

	private Model readPomModelIfPresent() throws Exception {
		if (!POM_FILE.exists()) {
			return null;
		}
		try (FileReader fileReader = new FileReader(POM_FILE)) {
			return reader.read(fileReader);
		}
	}

	private Integer resolvePomJavaMajor(Model model) {
		if (model.getProperties() != null) {
			for (String key : JAVA_PROPERTY_KEYS) {
				String raw = model.getProperties().getProperty(key);
				Integer major = parseJavaMajor(resolvePropertyReference(model, raw));
				if (major != null) {
					return major;
				}
			}
		}
		return resolveCompilerPluginJavaMajor(model);
	}

	private Integer resolveCompilerPluginJavaMajor(Model model) {
		List<Plugin> plugins = new ArrayList<>();
		if (model.getBuild() != null && model.getBuild().getPlugins() != null) {
			plugins.addAll(model.getBuild().getPlugins());
		}
		if (model.getProfiles() != null) {
			for (Profile profile : model.getProfiles()) {
				if (profile.getBuild() != null && profile.getBuild().getPlugins() != null) {
					plugins.addAll(profile.getBuild().getPlugins());
				}
			}
		}

		for (Plugin plugin : plugins) {
			if (!"maven-compiler-plugin".equals(plugin.getArtifactId())) {
				continue;
			}
			if (!(plugin.getConfiguration() instanceof Xpp3Dom configuration)) {
				continue;
			}
			for (String key : List.of("release", "source", "target")) {
				Xpp3Dom child = configuration.getChild(key);
				if (child == null) {
					continue;
				}
				Integer major = parseJavaMajor(resolvePropertyReference(model, child.getValue()));
				if (major != null) {
					return major;
				}
			}
		}
		return null;
	}

	private String resolvePropertyReference(Model model, String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		if (!trimmed.startsWith("${") || !trimmed.endsWith("}")) {
			return trimmed;
		}
		String key = trimmed.substring(2, trimmed.length() - 1);
		if (model.getProperties() == null) {
			return trimmed;
		}
		String resolved = model.getProperties().getProperty(key);
		return resolved == null ? trimmed : resolved;
	}

	private Integer parseJavaMajor(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		String trimmed = value.trim();
		if (trimmed.startsWith("1.")) {
			String[] pieces = trimmed.split("\\.");
			if (pieces.length >= 2) {
				return safeParseInt(pieces[1]);
			}
			return null;
		}
		StringBuilder major = new StringBuilder();
		for (char ch : trimmed.toCharArray()) {
			if (Character.isDigit(ch)) {
				major.append(ch);
			}
			else {
				break;
			}
		}
		if (major.isEmpty()) {
			return null;
		}
		return safeParseInt(major.toString());
	}

	private Integer safeParseInt(String value) {
		try {
			return Integer.parseInt(value);
		}
		catch (NumberFormatException ignored) {
			return null;
		}
	}

	private String resolveOperatingSystem() {
		String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
		if (osName.contains("mac") || osName.contains("darwin")) {
			return "macosx";
		}
		if (osName.contains("win")) {
			return "windows";
		}
		if (osName.contains("linux")) {
			return "linux";
		}
		throw new IllegalStateException("Unsupported operating system: " + osName);
	}

	private String resolveArchitecture() {
		String osArch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
		if ("aarch64".equals(osArch) || "arm64".equals(osArch)) {
			return "aarch64";
		}
		if ("x86_64".equals(osArch) || "amd64".equals(osArch)) {
			return "x86_64";
		}
		throw new IllegalStateException("Unsupported architecture: " + osArch);
	}

	private boolean isCommandOnPath(String command) {
		String pathValue = System.getenv("PATH");
		if (pathValue == null || pathValue.isBlank()) {
			return false;
		}
		List<String> candidates = new ArrayList<>();
		candidates.add(command);
		if (resolveOperatingSystem().equals("windows")) {
			String pathExt = System.getenv("PATHEXT");
			if (pathExt != null && !pathExt.isBlank()) {
				for (String extension : pathExt.split(";")) {
					candidates.add(command + extension.toLowerCase(Locale.ROOT));
				}
			}
			else {
				candidates.add(command + ".exe");
				candidates.add(command + ".cmd");
				candidates.add(command + ".bat");
			}
		}
		for (String entry : pathValue.split(File.pathSeparator)) {
			for (String candidateName : candidates) {
				Path candidate = Path.of(entry, candidateName);
				if (Files.isExecutable(candidate)) {
					return true;
				}
			}
		}
		return false;
	}

	private record JavaOption(String choice, String vendor, int major, String jvmImpl) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	record MiseJvmEntry(
			String vendor,
			@JsonProperty("java_version") String javaVersion,
			@JsonProperty("jvm_impl") String jvmImpl,
			@JsonProperty("image_type") String imageType) {
	}

}
