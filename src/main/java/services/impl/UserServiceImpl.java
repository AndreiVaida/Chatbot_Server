package services.impl;

import domain.entities.User;
import domain.information.Information;
import dtos.informationDtos.InformationClassDto;
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
import java.lang.reflect.Field;
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
    @Transactional
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
        final User user = userRepository.getByEmail(email);
        if (user == null) {
            throw new EntityNotFoundException("User not found.");
        }
        return user;
    }

    @Override
    public void updateUser(final User user) {
        userRepository.save(user);
    }

    @Override
    @Transactional
    public Information getInformationByClass(final Long userId, final Class<Information> informationClass) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final User user = getUserById(userId);

        final Method getterOfUser = user.getClass().getMethod("get" + informationClass.getSimpleName());
        return (Information) getterOfUser.invoke(user);
    }

    @Override
    @Transactional
    public void deleteInformationByInformationFieldNamePath(final Long userId, final String informationFieldNamePath) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException {
        final User user = getUserById(userId);

        final String[] informationPath = informationFieldNamePath.split("\\.");
        final Method getterOfUser = user.getClass().getMethod("get" + informationPath[0]);
        final Information information = (Information) getterOfUser.invoke(user);

        final Field prop = information.getClass().getDeclaredField(informationPath[1]);
        prop.setAccessible(true);
        prop.set(information, null);

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteInformationByInformationClass(final Long userId, final Class<Information> informationClass) throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IntrospectionException {
        final User user = getUserById(userId);

        // get Information of user
        final Method getterOfUser = user.getClass().getMethod("get" + informationClass.getSimpleName());
        final Information information = (Information) getterOfUser.invoke(user);

        // iterate Information fields and set them null
        final BeanInfo beanInformation = Introspector.getBeanInfo(information.getClass(), Object.class);
        final PropertyDescriptor[] propertyDescriptors = beanInformation.getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (propertyDescriptor.getName().equals("id") || propertyDescriptor.getName().equals("email") || propertyDescriptor.getName().equals("firstName")
                    || propertyDescriptor.getName().equals("password") || propertyDescriptor.getName().endsWith("ImportanceOrder")) {
                continue;
            }
            final String informationFieldName = propertyDescriptor.getName();
            final Field informationField = information.getClass().getDeclaredField(informationFieldName);
            informationField.setAccessible(true);
            informationField.set(information, null);
        }

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
