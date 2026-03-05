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
			githubDir();
			addGithubWorkflow();
		}
		catch (IOException ioException) {
			return "There was a problem adding pipeline configs";
		}
		return "Successfully added pipeline configs";
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

	private String githubWorkflowFile() {
		return """
				name: Multi-Architecture Image

				on:
				  push:
				    tags:
				      - "v*"

				env:
				  IMAGE_NAME: dashaun/${GITHUB_REPOSITORY#*/}

				jobs:
				  build-amd64:
				    runs-on: ubuntu-latest
				    steps:
				      - uses: actions/checkout@v4
				      - name: Setup Java with SDKMAN
				        run: |
				          curl -s "https://get.sdkman.io" | bash
				          source "$HOME/.sdkman/bin/sdkman-init.sh"
				          sdk install java 25.0.2.r25-nik
				          echo "JAVA_HOME=$HOME/.sdkman/candidates/java/current" >> $GITHUB_ENV
				          echo "$HOME/.sdkman/candidates/java/current/bin" >> $GITHUB_PATH
				      - name: Login to DockerHub
				        uses: docker/login-action@v3
				        with:
				          username: ${{ secrets.DOCKERHUB_USERNAME }}
				          password: ${{ secrets.DOCKERHUB_TOKEN }}
				      - name: Build amd64 image
				        run: ./mvnw -Pnative spring-boot:build-image -Dspring-boot.build-image.imageName=$IMAGE_NAME:$GITHUB_REF_NAME-x86_64
				      - name: Push amd64 image
				        run: docker push $IMAGE_NAME:$GITHUB_REF_NAME-x86_64

				  build-arm64:
				    runs-on: ubuntu-24.04-arm
				    steps:
				      - uses: actions/checkout@v4
				      - name: Setup Java with SDKMAN
				        run: |
				          curl -s "https://get.sdkman.io" | bash
				          source "$HOME/.sdkman/bin/sdkman-init.sh"
				          sdk install java 25.0.2.r25-nik
				          echo "JAVA_HOME=$HOME/.sdkman/candidates/java/current" >> $GITHUB_ENV
				          echo "$HOME/.sdkman/candidates/java/current/bin" >> $GITHUB_PATH
				      - name: Login to DockerHub
				        uses: docker/login-action@v3
				        with:
				          username: ${{ secrets.DOCKERHUB_USERNAME }}
				          password: ${{ secrets.DOCKERHUB_TOKEN }}
				      - name: Build arm64 image
				        run: ./mvnw -Pnative spring-boot:build-image -Dspring-boot.build-image.imageName=$IMAGE_NAME:$GITHUB_REF_NAME-aarch_64
				      - name: Push arm64 image
				        run: docker push $IMAGE_NAME:$GITHUB_REF_NAME-aarch_64

				  manifest:
				    needs: [build-amd64, build-arm64]
				    runs-on: ubuntu-latest
				    steps:
				      - name: Login to DockerHub
				        uses: docker/login-action@v3
				        with:
				          username: ${{ secrets.DOCKERHUB_USERNAME }}
				          password: ${{ secrets.DOCKERHUB_TOKEN }}
				      - name: Create and push manifest
				        run: |
				          docker manifest create $IMAGE_NAME:$GITHUB_REF_NAME \\
				            --amend $IMAGE_NAME:$GITHUB_REF_NAME-x86_64 \\
				            --amend $IMAGE_NAME:$GITHUB_REF_NAME-aarch_64
				          docker manifest push $IMAGE_NAME:$GITHUB_REF_NAME
				          docker manifest create $IMAGE_NAME:latest \\
				            --amend $IMAGE_NAME:$GITHUB_REF_NAME-x86_64 \\
				            --amend $IMAGE_NAME:$GITHUB_REF_NAME-aarch_64
				          docker manifest push $IMAGE_NAME:latest
				""";
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
