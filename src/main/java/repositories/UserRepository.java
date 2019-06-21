package repositories;

import domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User getByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByIsAdministrator(Boolean isAdministrator);
}
