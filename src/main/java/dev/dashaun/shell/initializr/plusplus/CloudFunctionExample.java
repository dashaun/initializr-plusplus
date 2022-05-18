package dev.dashaun.shell.initializr.plusplus;

import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.io.File;
import java.io.IOException;

import static dev.dashaun.shell.initializr.plusplus.Application.writeStringToFile;

@ShellComponent
@ShellCommandGroup("spring-cloud-functions")
public class CloudFunctionExample {

    private final static File DEMO_APPLICATION_SRC = new File("./src/main/java/com/example/demo/DemoApplication.java");

    @ShellMethod("Update DemoApplication with 'Hello World' @Bean Function")
    public String helloWorldFunctionBean() {
        try {
            writeStringToFile(DemoApplicationWithFunctionBean(),DEMO_APPLICATION_SRC );
        } catch (IOException ioException) {
            return "There was a problem adding the function";
        }
        return "Successfully added the function";
    }

    private String DemoApplicationWithFooFunction() {
        return """
                package com.example.demo;
                                
                import org.springframework.boot.SpringBootConfiguration;
                import org.springframework.cloud.function.context.FunctionRegistration;
                import org.springframework.cloud.function.context.FunctionType;
                import org.springframework.cloud.function.context.FunctionalSpringApplication;
                import org.springframework.context.ApplicationContextInitializer;
                import org.springframework.context.support.GenericApplicationContext;
                                
                import java.util.function.Function;
                                
                @SpringBootConfiguration
                public class DemoApplication implements ApplicationContextInitializer<GenericApplicationContext> {
                                
                    public static void main(String[] args) {
                        FunctionalSpringApplication.run(DemoApplication.class, args);
                    }
                                
                    public Function<Foo, String> hello() {
                        return foo -> String.format("Hello, %s", foo);
                    }
                                
                    @Override
                    public void initialize(GenericApplicationContext context) {
                        context.registerBean("hello", FunctionRegistration.class,
                                () -> new FunctionRegistration<>(hello())
                                        .type(FunctionType.from(Foo.class).to(String.class).getType()));
                    }
                                
                }
                                
                class Foo {
                    private String value;
                                
                    public String getValue() {
                        return value;
                    }
                                
                    public void setValue(String value) {
                        this.value = value;
                    }
                                
                    @Override
                    public String toString() {
                        return value;
                    }
                }
                """;
    }

    private String DemoApplicationWithFunctionalBeanDef() {
        return """
                package com.example.demo;
                                
                import org.springframework.boot.SpringBootConfiguration;
                import org.springframework.cloud.function.context.FunctionRegistration;
                import org.springframework.cloud.function.context.FunctionType;
                import org.springframework.cloud.function.context.FunctionalSpringApplication;
                import org.springframework.context.ApplicationContextInitializer;
                import org.springframework.context.support.GenericApplicationContext;
                                
                import java.util.function.Function;
                                
                @SpringBootConfiguration
                public class DemoApplication implements ApplicationContextInitializer<GenericApplicationContext> {
                                
                    public static void main(String[] args) {
                        FunctionalSpringApplication.run(DemoApplication.class, args);
                    }
                                
                    public Function<String, String> hello() {
                        return value -> String.format("Hello, %s", value);
                    }
                                
                    @Override
                    public void initialize(GenericApplicationContext context) {
                        context.registerBean("hello", FunctionRegistration.class,
                                () -> new FunctionRegistration<>(hello())
                                        .type(FunctionType.from(String.class).to(String.class).getType()));
                    }
                                
                }
                """;
    }

    private String DemoApplicationWithFunctionBean() {
        return """
                package com.example.demo;
                                
                import org.springframework.boot.SpringApplication;
                import org.springframework.boot.autoconfigure.SpringBootApplication;
                import org.springframework.context.annotation.Bean;
                                
                import java.nio.charset.StandardCharsets;
                import java.util.function.Function;
                                
                @SpringBootApplication
                public class DemoApplication {
                                
                    @Bean
                    public Function<byte[], String> hello() {
                        return value -> value.length>0 ? String.format("Hello, %s", new String(value, StandardCharsets.UTF_8)) : "Hello, World";
                    }
                                
                    public static void main(String[] args) {
                        SpringApplication.run(DemoApplication.class, args);
                    }
                                
                }
                """;
    }
}
