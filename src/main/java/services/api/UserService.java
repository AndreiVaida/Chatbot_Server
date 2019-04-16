package services.api;

import domain.entities.User;
import dtos.RequestUserRegisterDto;
import dtos.UserDto;

import java.util.List;

public interface UserService {
    List<User> findAll();

    void addUser(final User user);

    void addUser(final RequestUserRegisterDto requestUserRegisterDto);

    UserDto getUserById(final Long id);

    UserDto findUserByEmail(final String email);
}
