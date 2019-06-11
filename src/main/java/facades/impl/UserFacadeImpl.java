package facades.impl;

import dtos.RequestUserRegisterDto;
import dtos.UserDto;
import facades.api.UserFacade;
import mappers.UserMapper;
import org.springframework.stereotype.Service;
import services.api.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserFacadeImpl implements UserFacade {
    private final UserService userService;

    public UserFacadeImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public List<UserDto> findAll() {
        return userService.findAll().stream().map(UserMapper::userToUserDto).collect(Collectors.toList());
    }

    @Override
    public void addUser(final RequestUserRegisterDto requestUserRegisterDto) {
        userService.addUser(UserMapper.requestUserRegisterDtoToUser(requestUserRegisterDto));
    }

    @Override
    public UserDto getUserById(final Long id) {
        return UserMapper.userToUserDto(userService.getUserById(id));
    }

    @Override
    public UserDto findUserByEmail(final String email) {
        return UserMapper.userToUserDto(userService.findUserByEmail(email));
    }
}
