package dev.dashaun.shell.initializr.plusplus.models;

import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;

public class Properties {

  private final static String MAVEN_TEST_SKIP = "maven.test.skip";
  private final static String EXEC = "exec";
  private final static String REPACKAGE_CLASSIFIER = "repackage.classifier";
  private final static String SPRING_NATIVE_VERSION = "spring-native.version";
  private final static String SPRING_NATIVE_VERSION_VAL = "0.11.5";

  private final static String NATIVE_BUILDTOOLS_VERSION = "native-buildtools.version";
  private final static String NATIVE_BUILDTOOLS_VERSION_VAL = "0.9.11";

  private final static String SPRING_CLOUD_FUNCTION_VERSION = "spring-cloud-function.version";
  private final static String SPRING_CLOUD_FUNCTION_VERSION_VAL = "3.2.4";

  private final static String WRAPPER_VERSION = "wrapper.version";
  private final static String WRAPPER_VERSION_VAL = "1.0.27.RELEASE";
  private final static String AWS_LAMBDA_EVENTS = "aws-lambda-events.version";
  private final static String AWS_LAMBDA_EVENTS_VAL = "3.9.0";

  public static void awsProperties(Model model) {
    model.getProperties().setProperty(WRAPPER_VERSION, WRAPPER_VERSION_VAL);
    model.getProperties().setProperty(AWS_LAMBDA_EVENTS, AWS_LAMBDA_EVENTS_VAL);
    model.getProperties().setProperty(SPRING_CLOUD_FUNCTION_VERSION, SPRING_CLOUD_FUNCTION_VERSION_VAL);
  }

  public static void nativeProfileProperties(Profile p) {
    p.getProperties().setProperty(MAVEN_TEST_SKIP, "true");
    p.getProperties().setProperty(REPACKAGE_CLASSIFIER, EXEC);
    p.getProperties().setProperty(SPRING_NATIVE_VERSION, SPRING_NATIVE_VERSION_VAL);
    p.getProperties().setProperty(NATIVE_BUILDTOOLS_VERSION, NATIVE_BUILDTOOLS_VERSION_VAL);
  }

  public static void buildpackProperties(Profile p) {
    p.getProperties().setProperty(MAVEN_TEST_SKIP, "true");
  }
}
