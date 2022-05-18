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

    private String DemoApplicationWithFunctionBean(){
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
