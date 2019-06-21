package services.api;

import domain.entities.User;
import domain.information.Information;
import org.springframework.web.multipart.MultipartFile;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface UserService {
    List<User> findAll();

    void addUser(final User user);

    User getUserById(final Long id);

    User findUserByEmail(final String email);

    void updateUserFirstName(final User user);

    Information getInformationByClass(final Long userId, final Class<Information> informationClass) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;

    /**
     * Delete the given field of information.
     * @param informationFieldNamePath contains 2 names delimit by a dot: InformationClassName.informationFieldName
     *                                 InformationClassName must start with uppercase letter
     *                                 informationFieldName must start with lowercase letter
     */
    void deleteInformationByInformationFieldNamePath(final Long userId, final String informationFieldNamePath) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, NoSuchFieldException;

    void deleteInformationByInformationClass(final Long userId, final Class<Information> informationClass) throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, IntrospectionException;

    User updateProfilePicture(final Long userId, final MultipartFile profilePicture) throws IOException;

    User deleteProfilePicture(final Long userId);

    void updateUserFirstName(final Long userId, final String newFirstName);
}
