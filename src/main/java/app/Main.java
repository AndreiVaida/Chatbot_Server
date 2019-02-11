package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"repositories"})
@EntityScan(basePackages = {"domain"})
@ComponentScan(basePackages = {"services", "controllers"})
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        System.out.println("Server is on.");
    }
}
