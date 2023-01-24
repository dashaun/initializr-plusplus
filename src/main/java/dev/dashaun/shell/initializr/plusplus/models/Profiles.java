package dev.dashaun.shell.initializr.plusplus.models;

import org.apache.maven.model.BuildBase;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;

public class Profiles {

    private static final String LAMBDA_PROFILE_ID = "lambda";
    private static final String NATIVE_PROFILE_ID = "native";
    private static final String WEBFLUX_PROFILE_ID = "webflux";
    private static final String TINY_BUILDPACK_ID = "tiny-buildpack";


    @Deprecated
    public static void removeNativeProfile(Model model) {
        //Remove existing profile
        model.getProfiles().removeIf(p -> p.getId().equalsIgnoreCase(NATIVE_PROFILE_ID));
    }

    @Deprecated
    public static Profile nativeProfile() {
        Profile p = new Profile();
        p.setId(NATIVE_PROFILE_ID);

        //Set properties
        Properties.nativeProfileProperties(p);

        p.getRepositories().add(Repositories.springReleases());
        p.getPluginRepositories().add(Repositories.springReleases());

        //Add dependencies
        p.getDependencies().add(Dependencies.junitPlatformLauncher());
        p.getDependencies().add(Dependencies.springNative());

        //Add Build section
        p.setBuild(new BuildBase());
        //Add Build Plugins
        p.getBuild().addPlugin(Plugins.springBootMavenPlugin());
        p.getBuild().addPlugin(Plugins.springBootAotPlugin());
        p.getBuild().getPlugins().add(Plugins.nativeMavenPlugin());
        return p;
    }
    
    public static void removeLambdaProfile(Model model) {
        model.getProfiles().removeIf(p -> p.getId().equalsIgnoreCase(LAMBDA_PROFILE_ID));
    }

    public static Profile lambdaProfile() {
        Profile p = new Profile();
        p.setId(LAMBDA_PROFILE_ID);

        //Update starters
        p.getDependencies().add(Dependencies.awsStarter());
        p.getDependencies().add(Dependencies.awsJavaCore());
        p.getDependencies().add(Dependencies.awsJavaEvents());

        //Build section
        p.setBuild(new BuildBase());
        //Add Build Plugins
        p.getBuild().addPlugin(Plugins.mavenAssemblyPluginNative());

        return p;
    }

    public static void removeWebfluxProfile(Model model) {
        model.getProfiles().removeIf(p -> p.getId().equalsIgnoreCase(WEBFLUX_PROFILE_ID));
    }

    public static Profile webfluxProfile() {
        Profile p = new Profile();
        p.setId(WEBFLUX_PROFILE_ID);

        //Add dependencies
        p.getDependencies().add(Dependencies.webfluxStarter());
        return p;
    }

    public static void removeTinyBuildpackProfile(Model model) {
        model.getProfiles().removeIf(p -> p.getId().equalsIgnoreCase(TINY_BUILDPACK_ID));
    }

    public static Profile tinyBuildpackProfile() {
        Profile p = new Profile();
        p.setId(TINY_BUILDPACK_ID);

        Properties.buildpackProperties(p);

        //Add Build section
        p.setBuild(new BuildBase());
        //Add Build Plugins
        p.getBuild().addPlugin(Plugins.springBootMavenPluginTinyBuildpack());

        return p;
    }
}
