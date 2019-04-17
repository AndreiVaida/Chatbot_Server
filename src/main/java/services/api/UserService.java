package services.api;

import domain.entities.User;

import java.util.List;

public interface UserService {
    List<User> findAll();

    void addUser(final User user);

    User getUserById(final Long id);

    User findUserByEmail(final String email);
}
