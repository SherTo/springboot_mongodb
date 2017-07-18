package net.ebh.exam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ExamApp {

    public static void main(String[] args) {
        SpringApplication.run(ExamApp.class, args);
    }
}
