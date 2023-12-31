package dev.dashaun.shell.initializr.plusplus;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.File;
import java.io.IOException;

import static dev.dashaun.shell.initializr.plusplus.Application.writeStringToFile;

@ShellComponent
public class PipelineCommands {

	@ShellMethod("pipelines to create multi-architecture manifests")
	public String multiArchManifests() {
		try {
			circleciDir();
			githubDir();
			addCircleCIConfig();
			addGithubWorkflow();
		}
		catch (IOException ioException) {
			return "There was a problem adding pipeline configs";
		}
		return "Successfully added pipeline configs";
	}

	private void addCircleCIConfig() throws IOException {
		File file = new File("./.circleci/config.yml");
		if (!file.exists()) {
			writeStringToFile(circleCIConfigFile(), file);
		}
		else {
			throw new IOException("File already exists");
		}
	}

	private void addGithubWorkflow() throws IOException {
		File file = new File("./.github/workflows/main.yml");
		if (!file.exists()) {
			writeStringToFile(githubWorkflowFile(), file);
		}
		else {
			throw new IOException("File already exists");
		}
	}

	private String circleCIConfigFile() {
		return """
				version: 2.1

				orbs:
				  docker: circleci/docker@2.4.0
				  sdkman: joshdholtz/sdkman@0.2.0

				jobs:
				  arm64-native:
				    machine:
				      image: ubuntu-2004:current
				      resource_class: arm.medium
				    steps:
				      - checkout
				      - sdkman/setup-sdkman
				      - sdkman/sdkman-install:
				          candidate: java
				          version: 22.3.r17-grl
				      - run:
				          name: "mvnw -Pnative spring-boot:build-image"
				          command: "./mvnw -Pnative spring-boot:build-image"
				      - docker/check:
				          docker-username: DOCKER_LOGIN
				          docker-password: DOCKERHUB_PASSWORD
				      - docker/push:
				          image: dashaun/$CIRCLE_PROJECT_REPONAME
				          tag: $CIRCLE_TAG-aarch_64

				workflows:
				  arm64-native-workflow:
				    jobs:
				      - arm64-native:
				          context:
				            - dashaun-dockerhub
				          filters:
				            tags:
				              only: /^v.*/
				            branches:
				              ignore: /.*/
				""";
	}

	private String githubWorkflowFile() {
		return """
				name: Native-AMD64

				on:
				  push:
				    tags:
				      - "v*"

				env:
				  IMAGE_NAME: dashaun/${GITHUB_REPOSITORY#*/}

				jobs:
				  build:

				    runs-on: ubuntu-latest

				    steps:
				      #Login to DockerHub
				      - name: Login to DockerHub
				        uses: docker/login-action@v2
				        with:
				          username: dashaun
				          password: ${{ secrets.DOCKERHUB_TOKEN }}
				      - uses: actions/setup-java@v2
				        with:
				          distribution: 'liberica' # See 'Supported distributions' for available options
				          java-version: '17'
				      - name: Checkout master
				        uses: actions/checkout@v3
				        with:
				          submodules: true
				      #Build Image
				      - name: Build Image
				        run: ./mvnw -Pnative spring-boot:build-image
				      #Deploy the image to the Docker registry
				      - name: Push Images to Docker Registry
				        run: docker push -a $IMAGE_NAME


				  manifest:
				    needs: build
				    runs-on: ubuntu-latest
				    steps:
				      - name: Login to DockerHub
				        uses: docker/login-action@v2
				        with:
				          username: dashaun
				          password: ${{ secrets.DOCKERHUB_TOKEN }}
				      - name: pull-arm64
				        uses: nick-fields/retry@v2
				        with:
				          timeout_minutes: 5
				          retry_wait_seconds: 60
				          max_attempts: 6
				          command: docker pull $IMAGE_NAME:$GITHUB_REF_NAME-aarch_64
				      - name: create-manifest
				        run: |
				          docker manifest create $IMAGE_NAME:$GITHUB_REF_NAME --amend $IMAGE_NAME:$GITHUB_REF_NAME-x86_64 --amend $IMAGE_NAME:$GITHUB_REF_NAME-aarch_64
				          docker manifest push $IMAGE_NAME:$GITHUB_REF_NAME
				          docker manifest create $IMAGE_NAME:latest --amend $IMAGE_NAME:$GITHUB_REF_NAME-x86_64 --amend $IMAGE_NAME:$GITHUB_REF_NAME-aarch_64
				          docker manifest push $IMAGE_NAME:latest
				""";
	}

	private void circleciDir() throws IOException {
		File file = new File("./.circleci");
		if (!file.exists()) {
			if (!file.mkdir()) {
				throw new IOException("Couldn't create directory");
			}
		}
	}

	private void githubDir() throws IOException {
		File file = new File("./.github/workflows");
		if (!file.exists()) {
			if (!file.mkdirs()) {
				throw new IOException("Couldn't create directory");
			}
		}
	}

}
