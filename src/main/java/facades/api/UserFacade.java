package facades.api;

import dtos.RequestUserRegisterDto;
import dtos.UserDto;

import java.util.List;

public interface UserFacade {
    List<UserDto> findAll();

    void addUser(final RequestUserRegisterDto requestUserRegisterDto);

    UserDto getUserById(final Long id);

    UserDto findUserByEmail(final String email);
}
