package services.api;

import dto.RequestLoginDto;
import dto.UserDto;
import dto.RequestUserRegisterDto;

import javax.security.auth.login.FailedLoginException;

public interface UserService {
    void addUser(final RequestUserRegisterDto requestUserRegisterDto);

    UserDto getUserById(final Long id);

    // TODO: replace with Spring Security
    String login(RequestLoginDto requestLogin) throws FailedLoginException;
}
