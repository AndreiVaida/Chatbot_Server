package services.api;

import dtos.RequestLoginDto;
import dtos.UserDto;
import dtos.RequestUserRegisterDto;

import javax.security.auth.login.FailedLoginException;

public interface UserService {
    void addUser(final RequestUserRegisterDto requestUserRegisterDto);

    UserDto getUserById(final Long id);

    // TODO: replace with Spring Security
    String login(RequestLoginDto requestLogin) throws FailedLoginException;
}
