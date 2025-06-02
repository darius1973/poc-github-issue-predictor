package com.dariusnica.githubpredictor;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GithubIssuePredictorApp {

    public static void main(String[] args) {
        SpringApplication.run(GithubIssuePredictorApp.class, args);
    }
}
