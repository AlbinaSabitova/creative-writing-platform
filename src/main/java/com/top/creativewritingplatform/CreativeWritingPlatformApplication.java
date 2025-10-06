package com.top.creativewritingplatform;

import com.top.creativewritingplatform.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.top.creativewritingplatform.repositories")
@EntityScan(basePackages = "com.top.creativewritingplatform.models")
public class CreativeWritingPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreativeWritingPlatformApplication.class, args);
    }

    @Bean
    CommandLineRunner run(UserService userService) {
        return args -> {
            userService.createAdminIfNotExists();
        };
    }
}
