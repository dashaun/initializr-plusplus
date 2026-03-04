# Copilot Instructions

## What this project is

`initializr-plusplus` is a Spring Shell CLI tool that modifies Spring Initializr-generated projects. It operates on a **target project's files in the current working directory** (not its own source), reading/writing `./pom.xml`, `./src/main/resources/application.yaml`, `./.mvn/`, and similar paths at runtime.

## Build & run

```bash
# Standard JVM build
./mvnw clean package
java -jar target/plusplus-0.jar

# Or run directly
./mvnw spring-boot:run

# Native binary (GraalVM required)
./mvnw -Pnative clean native:compile -DskipTests

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ApplicationTests
```

Java version is managed via SDKMAN: `java=25.0.2-librca` (see `.sdkmanrc`).

## Architecture

All shell commands are implemented as `@ShellComponent` classes in `dev.dashaun.shell.initializr.plusplus`:

- **`PomFileCommands`** – reads/writes `./pom.xml` using `MavenXpp3Reader` / `MavenXpp3Writer`
- **`ApplicationConfigCommands`** – writes `./src/main/resources/application.yaml` and assembly descriptors
- **`MavenExtensionCommands`** – creates `./.mvn/extensions.xml` and related config files
- **`PipelineCommands`** – generates CI pipeline configs (CircleCI, GitHub Actions) in the target project
- **`CloudFunctionExample`** – injects a Spring Cloud Function `@Bean` into `DemoApplication.java`
- **`ReadMeCommands`** – adds a README.md to the target project

Model helpers live in `dev.dashaun.shell.initializr.plusplus.models`:
- **`Plugins`** – factory methods that build `org.apache.maven.model.Plugin` objects
- **`Dependencies`** – factory methods that build `org.apache.maven.model.Dependency` objects
- **`Profiles`**, **`Properties`**, **`Repositories`** – same pattern for their respective Maven model types

## Key conventions

**Idempotent pom mutations**: Before adding a plugin or dependency, always remove any existing entry with the same `groupId`/`artifactId` first, then add the new one. See `Plugins.addNativeMavenPlugin()` for the pattern.

**Command return values are user-facing strings**: Every `@ShellMethod` returns a `String` shown directly in the shell. Return a human-readable success message or an error description — no exceptions should bubble up.

**All file writes go through `Application.writeStringToFile()`**: This static helper overwrites the file. Use it for creating new config files; use `Files.writeString(..., StandardOpenOption.APPEND)` only when appending (e.g., `.gitignore` entries).

**Tests require `spring.shell.interactive.enabled=false`**: The `@SpringBootTest` in `ApplicationTests` sets this property so the interactive shell doesn't block tests.

**`application.yml` disables logging and banner**: Root logging is set to `off` and the Spring banner is suppressed for a clean CLI experience. Keep this as-is.

## Releases

Releases are triggered by pushing a git tag (`v#.#.#`). GitHub Actions builds native binaries on Linux, macOS, and Windows and uploads them to the GitHub Release. The `maven-git-versioning-extension` derives the version from the tag at build time (configured in `.mvn/`).
