package services.impl;

import domain.entities.User;
import domain.information.Information;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import repositories.PersonalInformationRepository;
import repositories.UserRepository;
import services.api.UserService;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static app.Main.CHATBOT_ID;


@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PersonalInformationRepository personalInformationRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PersonalInformationRepository personalInformationRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.personalInformationRepository = personalInformationRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public void addUser(final User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EntityExistsException("There already exists an account with this e-mail.");
        }

        final String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        personalInformationRepository.save(user.getPersonalInformation());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public User getUserById(Long id) {
        if (id == null || id == 0) {
            id = CHATBOT_ID;
        }
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found."));
    }

    @Override
    @Transactional
    public User findUserByEmail(final String email) {
        final User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new EntityNotFoundException("User not found.");
        }
        return user;
    }

    @Override
    public void updateUser(final User user) {
        userRepository.save(user);
    }

//    @Override
//    public void updateUser(final List<Information> informationList, final User user) {
//        for (Information information : informationList) {
//            // iterate getters of the information object
//            for (Method getterOfInformation : information.getClass().getMethods()) {
//                if (getterOfInformation.getName().startsWith("get") && getterOfInformation.getParameterTypes().length == 0) {
//                    try {
//                        final Object info = getterOfInformation.invoke(information);
//                        if (info != null) {
//                            final Method getterOfUser = user.getClass().getMethod("get" + information.getClass().getSimpleName());
//                            final Method setterOfInformation = information.getClass().getMethod(getterOfInformation.getName().replace("get", "set"));
//                            final Information userInformation = (Information) getterOfUser.invoke(user); // ex: user.getPersonalInformation()
//                            setterOfInformation.invoke(userInformation, info);
//                        }
//
//                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }
}
