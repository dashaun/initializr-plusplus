package dev.dashaun.shell.initializr.plusplus;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.File;
import java.io.IOException;

import static dev.dashaun.shell.initializr.plusplus.Application.writeStringToFile;

@ShellComponent
public class ReadMeCommands {

	@ShellMethod("add ReadMe.md file")
	public String addReadMe() {
		try {
			writeFile();
		}
		catch (IOException ioException) {
			return "There was a problem adding pipeline configs";
		}
		return "Successfully added pipeline configs";
	}

	private void writeFile() throws IOException {
		File file = new File("./README.md");
		if (!file.exists()) {
			writeStringToFile(readMeDotMD(), file);
		}
		else {
			throw new IOException("File already exists");
		}
	}

	private String readMeDotMD() {
		return """
				[![Forks][forks-shield]][forks-url]
				[![Stargazers][stars-shield]][stars-url]
				[![Issues][issues-shield]][issues-url]
				# Project Name
				## Prerequisites
				## Quick Start
				## Attributions
				## Related Videos
				<!-- MARKDOWN LINKS & IMAGES -->
				<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
				[forks-shield]: https://img.shields.io/github/forks/[org]/[repository-name].svg?style=for-the-badge
				[forks-url]: https://github.com/[org]/[repository-name]/forks
				[stars-shield]: https://img.shields.io/github/stars/[org]/[repository-name].svg?style=for-the-badge
				[stars-url]: https://github.com/[org]/[repository-name]/stargazers
				[issues-shield]: https://img.shields.io/github/issues/[org]/[repository-name].svg?style=for-the-badge
				[issues-url]: https://github.com/[org]/[repository-name]/issues
				[org]: dashaun
				[repository]: initializr-plusplus
				""";
	}

}
