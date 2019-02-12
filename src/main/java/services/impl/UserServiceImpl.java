package services.impl;

import domain.entities.User;
import dtos.RequestUserRegisterDto;
import dtos.UserDto;
import mappers.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import repositories.UserRepository;
import services.api.UserService;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    @Transactional
    public void addUser(final User user) {
        final String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void addUser(final RequestUserRegisterDto requestUserRegisterDto) {
        final User user = UserMapper.requestUserRegisterDtoToUser(requestUserRegisterDto);
        final String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
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
    @Transactional
    public UserDto findUserByEmail(final String email) {
        final User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new EntityNotFoundException("User not found.");
        }
        return UserMapper.userToUserDto(user);
    }
}
