[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![License][license-shield]][license-url]

[![mvn test](https://github.com/dashaun/spring-initializer-next-step/actions/workflows/mvn-test.yml/badge.svg)](https://github.com/dashaun/spring-initializer-next-step/actions/workflows/mvn-test.yml)

# initializr-plusplus
### CLI for making changes to [Spring Initializr](https://start.spring.io) projects.

- Use [Spring Initializr](https://start.spring.io) it is awesome, I highly recommend it.
- Then get your project to production.
- This tool decreases the amount of time between `T-Initialize` and `T-Production`
  - This improves the `Fifth Metric` Mean Time To Dopamine (MTTD)
- This tool provides commands via CLI and interactive shell
  - Add the `jtigver-maven-plugn` and `os-maven-plugin` as Maven Extensions
  - Update the `project-description`
  - Update the `project-name`
  - Update the `project-version`
  - Enable all `actuator` management endpoints
  - Update `spring-cloud-function-context` to a specific starter

## | [Quick Start](#quick-start) | [Usage](#usage) | [Built With](#built-with) | [Develop](#develop) |

## Quick Start

Download the latest release and put it on your path.

```bash
# Download 
curl -L https://github.com/dashaun/initializr-plusplus/releases/download/v#.#.#/initializr-plusplus-linux-amd64 --output initializr-plusplus
# Download and extract an example project from start.sprint.io
curl https://start.spring.io/starter.tgz -d dependencies=web,data-jpa -d type=maven-project -d baseDir=./ | tar -xzvf -
# Use help command
./initializr-plusplus help
# Add extensions
./initializr-plusplus extensions
# Interactive mode
./initializr-plusplus
```

## Usage

```text
AVAILABLE COMMANDS

Application Config Commands
       expose-mgmt: expose all management endpoints via web

Built-In Commands
       help: Display help about available commands
       history: Display or save the history of previously run commands
       version: Show version info
       script: Read and execute commands from a file.

Cloud Function Example
       hello-world-function-bean: Update DemoApplication with 'Hello World' @Bean Function

Maven Extension Commands
       extensions: add mvn extensions and config

Pom File Commands
       native-maven-plugin: Support for GraalVM native-image compiler.
       multi-arch-builder: Add multi-architecture builder support.
       project-name: Update the project name
       tiny-buildpack-profile: Create Native OCI Images with paketobuildpacks/builder:tiny
       project-version: Update the project version
       lambda-profile: Add AWS Lambda profile for Spring Cloud Functions
       webflux-profile: Add a 'webflux' profile for Spring Cloud Functions
       project-description: Update the project description
```

## Built with:

* [Spring Shell](https://spring.io/projects/spring-shell)

## Develop

### Normal build

- `./mvnw clean package`
- `java -jar target/plusplus-0.0.2.jar`
or
- `./mvnw spring-boot:run`

### Native build

- `./mvnw -Pnative clean native:compile -DskipTests`

### Release

- `git tag v#.#.#`
- `git push origin v#.#.#`

> gh release upload v#.#.# target/initializr-plusplus-$OS-$ARCH

### Maintaining up-to-date dependencies

Execute following command to find new dependencies

- `./mvnw versions:display-dependency-updates`

### Roadmap

See the [open issues](https://github.com/dashaun/initializr-plusplus/issues) for a list of proposed features (and known issues).

### Notes

I want to connect different released packages back into the repository.
Right now I am using the `gh` cli to do this manually.
Issue #9 is to automate this process, for Windows, Linux+ARM64, Darwin+ARM64 and Darwin+AMD64.

Before running `native:compile` on Windows
> C:\Program Files (x86)\Microsoft Visual Studio\2019\BuildTools\VC\Auxiliary\Build\vcvars64.bat
> 
### Contributing

Pull-requests are welcomed!

### License

Distributed under the Apache-2.0 License. See `LICENSE` for more information.

[contributors-shield]: https://img.shields.io/github/contributors/dashaun/spring-initializer-next-step.svg?style=for-the-badge
[contributors-url]: https://github.com/dashaun/spring-initializer-next-step/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/dashaun/spring-initializer-next-step.svg?style=for-the-badge
[forks-url]: https://github.com/dashaun/spring-initializer-next-step/network/members
[stars-shield]: https://img.shields.io/github/stars/dashaun/spring-initializer-next-step.svg?style=for-the-badge
[stars-url]: https://github.com/dashaun/spring-initializer-next-step/stargazers
[issues-shield]: https://img.shields.io/github/issues/dashaun/spring-initializer-next-step.svg?style=for-the-badge
[issues-url]: https://github.com/dashaun/spring-initializer-next-step/issues
[license-shield]: https://img.shields.io/github/license/dashaun/spring-initializer-next-step.svg?style=for-the-badge
[license-url]: https://github.com/dashaun/spring-initializer-next-step/blob/master/LICENSE.txt