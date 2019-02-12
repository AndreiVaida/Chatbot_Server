package app;

import domain.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import services.api.UserService;

import java.time.LocalDate;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@EnableJpaRepositories(basePackages = {"repositories"})
@EntityScan(basePackages = {"domain"})
@ComponentScan(basePackages = {"services", "controllers", "configuration"})
public class Main {
    private final UserService userService;

    @Autowired
    public Main(UserService userService) {
        this.userService = userService;
        //populateDb();
    }

    private void populateDb() {
        final User andy = new User(1L, "andy@andy.andy", "parola", "Andy", "Bot", LocalDate.of(2016, 6, 26));
        userService.addUser(andy);
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        System.out.println("Server is on.");
    }

}
