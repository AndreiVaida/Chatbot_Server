package services.impl;

import domain.entities.User;
import dtos.RequestLoginDto;
import dtos.RequestUserRegisterDto;
import dtos.UserDto;
import mappers.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.UserRepository;
import services.api.UserService;

import javax.persistence.EntityNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void addUser(final RequestUserRegisterDto requestUserRegisterDto) {
        final User user = UserMapper.requestUserRegisterDtoToUser(requestUserRegisterDto);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserDto getUserById(final Long id) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
        return UserMapper.userToUserDto(user);
    }

    @Override
    public String login(final RequestLoginDto requestLogin) throws FailedLoginException {
        final User user = userRepository.findByEmail(requestLogin.getEmail());
        if (user == null) {
            throw new EntityNotFoundException("User not found.");
        }

        if (!user.getPassword().equals(requestLogin.getPassword())) {
            throw new FailedLoginException();
        }
        return user.getId().toString();
    }
}
