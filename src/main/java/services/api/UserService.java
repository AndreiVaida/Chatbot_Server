package services.api;

import dtos.RequestUserRegisterDto;
import dtos.UserDto;

public interface UserService {
    void addUser(final RequestUserRegisterDto requestUserRegisterDto);

    UserDto getUserById(final Long id);

    UserDto findUserByEmail(final String email);
}
