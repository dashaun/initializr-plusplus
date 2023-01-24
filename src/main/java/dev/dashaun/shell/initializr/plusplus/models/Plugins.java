package dev.dashaun.shell.initializr.plusplus.models;

import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public class Plugins {

    private static final String MAVEN_ASSEMBLY_PLUGIN = "maven-assembly-plugin";

    private static final String SPRING_BOOT_GROUP_ID = "org.springframework.boot";
    private final static String SPRING_EXPERIMENTAL_GROUP_ID = "org.springframework.experimental";


    private final static String SPRING_AOT_MAVEN_PLUGIN = "spring-aot-maven-plugin";
    private final static String SPRING_BOOT_MAVEN_PLUGIN = "spring-boot-maven-plugin";

    private final static String GRAALVM_BUILDTOOLS_GROUP_ID = "org.graalvm.buildtools";
    private final static String NATIVE_MAVEN_PLUGIN = "native-maven-plugin";
    @Deprecated
    public static void removeNativePlugins(Model model){
        model.getBuild().getPlugins().removeIf(p -> p.getGroupId().equalsIgnoreCase(SPRING_BOOT_GROUP_ID) && p.getArtifactId().equalsIgnoreCase(SPRING_BOOT_MAVEN_PLUGIN));
        model.getBuild().getPlugins().removeIf(p -> p.getGroupId().equalsIgnoreCase(SPRING_EXPERIMENTAL_GROUP_ID) && p.getArtifactId().equalsIgnoreCase(SPRING_AOT_MAVEN_PLUGIN));
        model.getBuild().getPlugins().removeIf(p -> p.getArtifactId().equalsIgnoreCase(MAVEN_ASSEMBLY_PLUGIN));
    }
    
    public static void addNativeMavenPlugin(Model model){
        model.getBuild().getPlugins().removeIf(p -> p.getGroupId().equalsIgnoreCase(GRAALVM_BUILDTOOLS_GROUP_ID) && p.getArtifactId().equalsIgnoreCase(NATIVE_MAVEN_PLUGIN));
        model.getBuild().getPlugins().add(Plugins.nativeMavenPlugin());
    }

    public static Plugin springBootMavenPlugin() {
        Plugin p = new Plugin();
        p.setGroupId(SPRING_BOOT_GROUP_ID);
        p.setArtifactId(SPRING_BOOT_MAVEN_PLUGIN);
        Xpp3Dom c = new Xpp3Dom("configuration");
        Xpp3Dom classifier = new Xpp3Dom("classifier");
        classifier.setValue("${repackage.classifier}");
        c.addChild(classifier);
        p.setConfiguration(c);
        return p;
    }

    public static Plugin springBootMavenPluginTinyBuildpack() {
        Plugin p = new Plugin();
        p.setGroupId(SPRING_BOOT_GROUP_ID);
        p.setArtifactId(SPRING_BOOT_MAVEN_PLUGIN);
        Xpp3Dom c = new Xpp3Dom("configuration");
        Xpp3Dom classifier = new Xpp3Dom("classifier");
        c.addChild(classifier);
        Xpp3Dom image = new Xpp3Dom("image");
        Xpp3Dom builder = new Xpp3Dom("builder");
        builder.setValue("paketobuildpacks/builder:tiny");
        image.addChild(builder);
        Xpp3Dom env = new Xpp3Dom("env");
        Xpp3Dom bpNativeImage = new Xpp3Dom("BP_NATIVE_IMAGE");
        bpNativeImage.setValue("true");
        env.addChild(bpNativeImage);
        image.addChild(env);
        c.addChild(image);
        p.setConfiguration(c);
        return p;
    }

    @Deprecated
    public static Plugin springBootAotPlugin() {
        Plugin p = new Plugin();
        p.setGroupId(SPRING_EXPERIMENTAL_GROUP_ID);
        p.setArtifactId(SPRING_AOT_MAVEN_PLUGIN);
        p.setVersion("${spring-native.version}");

        p.getExecutions().add(generate());
        p.getExecutions().add(testGenerate());

        Xpp3Dom configuration = new Xpp3Dom("configuration");
        Xpp3Dom removeYamlSupport = new Xpp3Dom("removeYamlSupport");
        removeYamlSupport.setValue("true");
        configuration.addChild(removeYamlSupport);
        p.setConfiguration(configuration);

        return p;
    }

    public static Plugin nativeMavenPlugin() {
        Plugin p = new Plugin();
        p.setGroupId("org.graalvm.buildtools");
        p.setArtifactId("native-maven-plugin");
//        p.setVersion("${native-buildtools.version}");
//        p.setExtensions(true);
//        p.getExecutions().add(testNative());
//        p.getExecutions().add(buildNative());
        return p;
    }

    public static Plugin mavenAssemblyPluginJava() {
        Plugin p = new Plugin();
        p.setArtifactId("maven-assembly-plugin");
        p.getExecutions().add(javaZip());

        Xpp3Dom configuration = new Xpp3Dom("configuration");
        Xpp3Dom descriptors = new Xpp3Dom("descriptors");
        Xpp3Dom descriptor = new Xpp3Dom("descriptor");
        descriptor.setValue("src/assembly/java.xml");
        descriptors.addChild(descriptor);
        configuration.addChild(descriptors);

        p.setConfiguration(configuration);
        return p;
    }

    public static Plugin mavenAssemblyPluginNative() {
        Plugin p = new Plugin();
        p.setArtifactId(MAVEN_ASSEMBLY_PLUGIN);
        p.getExecutions().add(nativeZip());

        Xpp3Dom configuration = new Xpp3Dom("configuration");
        Xpp3Dom descriptors = new Xpp3Dom("descriptors");
        Xpp3Dom descriptor = new Xpp3Dom("descriptor");
        descriptor.setValue("src/assembly/native.xml");
        descriptors.addChild(descriptor);
        configuration.addChild(descriptors);

        p.setConfiguration(configuration);
        return p;
    }

    private static PluginExecution generate() {
        PluginExecution generate = new PluginExecution();
        generate.setId("generate");
        generate.getGoals().add("generate");
        return generate;
    }

    private static PluginExecution testGenerate(){
        PluginExecution testGenerate = new PluginExecution();
        testGenerate.setId("test-generate");
        testGenerate.getGoals().add("test-generate");
        return testGenerate;
    }

    @Deprecated
    private static PluginExecution buildNative() {
        PluginExecution tn = new PluginExecution();
        tn.setId("build-native");
        tn.setPhase("package");
        tn.getGoals().add("build");
        return tn;
    }

    @Deprecated
    private static PluginExecution testNative() {
        PluginExecution tn = new PluginExecution();
        tn.setId("test-native");
        tn.setPhase("test");
        tn.getGoals().add("test");
        return tn;
    }

    private static PluginExecution javaZip() {
        PluginExecution javaZip = new PluginExecution();
        javaZip.setId("java-zip");
        javaZip.setPhase("package");
        javaZip.setInherited(false);
        javaZip.getGoals().add("single");
        return javaZip;
    }

    private static PluginExecution nativeZip() {
        PluginExecution nativeZip = new PluginExecution();
        nativeZip.setId("native-zip");
        nativeZip.setPhase("package");
        nativeZip.setInherited(false);
        nativeZip.getGoals().add("single");
        return nativeZip;
    }
}
