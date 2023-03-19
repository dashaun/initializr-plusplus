package dev.dashaun.shell.initializr.plusplus.models;

import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryPolicy;

public class Repositories {

  private static final String SPRING_RELEASES = "spring-releases";
  private static final String SPRING_RELEASES_NAME = "Spring Releases";
  private static final String SPRING_RELEASES_URL = "https://repo.spring.io/release";

  public static void removeSpringReleases(Model model) {
    model.getRepositories().removeIf(r -> r.getId().equalsIgnoreCase(SPRING_RELEASES));
    model.getPluginRepositories().removeIf(pr -> pr.getId().equalsIgnoreCase(SPRING_RELEASES));
  }

  public static Repository springReleases() {
    Repository springReleases = new Repository();
    springReleases.setId(SPRING_RELEASES);
    springReleases.setName(SPRING_RELEASES_NAME);
    springReleases.setUrl(SPRING_RELEASES_URL);
    RepositoryPolicy rp = new RepositoryPolicy();
    rp.setEnabled(false);
    springReleases.setSnapshots(rp);
    return springReleases;
  }
}
