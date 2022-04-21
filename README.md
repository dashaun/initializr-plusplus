[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]

[![mvn test](https://github.com/dashaun/spring-initializer-next-step/actions/workflows/mvn-test.yml/badge.svg)](https://github.com/dashaun/spring-initializer-next-step/actions/workflows/mvn-test.yml)

# initializr-plusplus
## CLI for making changes to [Spring Initialzr](https://start.spring.io) projects.

### Built with:

* [Spring Shell](https://spring.io/projects/spring-shell)

## | [Usage](#usage) | [Develop](#develop) |

## Usage

```text
AVAILABLE COMMANDS

./.mvn/
        jgitver: add jgitver extension and config

./pom.xml
        project-description: Update the project description
        project-name: Update the project name
        project-version: Update the project version

Built-In Commands
        clear: Clear the shell screen.
        exit, quit: Exit the shell.
        help: Display help about available commands.
        history: Display or save the history of previously run commands
        script: Read and execute commands from a file.
        stacktrace: Display the full stacktrace of the last error.
        version: Show version info
        
```

## Develop

### Normal build

- `./mvnw clean package`

### Native build

- `./mvnw clean package -Pnative`

### Release

- `git tag v#.#.#`
- `git push --follow-tags origin main`

## Maintaining up-to-date dependencies

Execute following command to find new dependencies

````
./mvnw versions:display-dependency-updates
````

<!-- ROADMAP -->
## Roadmap

- [X] jgitver extension
- [X] jgitver config
- [X] update the artifact version in pom.xml
- [ ] rename Application
- [ ] Add github actions
- [ ] add native build profiles
- [X] Update project name
- [X] Update project description
- [X] Update the prompt
- [ ] gitcommit id plugin
- [ ] registry placeholders in <properties>

See the [open issues](https://github.com/dashaun/spring-initializer-next-step/issues) for a list of proposed features (and known issues).

<!-- CONTRIBUTING -->
## Contributing

Pull-requests are welcomed!

<!-- LICENSE -->
## License

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