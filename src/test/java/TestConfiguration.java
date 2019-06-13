import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {"domain"})
@ComponentScan(basePackages = {"repositories"})
@EnableJpaRepositories(basePackages = {"repositories"})
public class TestConfiguration {
}
