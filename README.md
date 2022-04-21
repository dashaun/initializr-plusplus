# initializer-plusplus

Spring Shell CLI for making changes to [Spring Initialzr](start.spring.io) projects.

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

## To create a release

The release will get created by the github action.
It is triggered by a new tag that matches the regex.

```text
git tag v#.#.#
git push origin v#.#.#
```