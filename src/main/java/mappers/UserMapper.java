package mappers;

import domain.entities.User;
import dtos.RequestUserRegisterDto;
import dtos.UserDto;
import org.json.simple.JSONObject;

public class UserMapper {
    private UserMapper() {}


    public static User requestUserRegisterDtoToUser(final RequestUserRegisterDto requestUserRegisterDto) {
        final User user = new User();
        user.setEmail(requestUserRegisterDto.getEmail());
        user.setPassword(requestUserRegisterDto.getPassword());
        user.setFirstName(requestUserRegisterDto.getFirstName());
        user.setSurname(requestUserRegisterDto.getSurname());
        user.setBirthDay(requestUserRegisterDto.getBirthDay());
        return user;
    }

    public static UserDto userToUserDto(final User user) {
        final UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setSurname(user.getSurname());
        userDto.setBirthDay(user.getBirthDay());
        return userDto;
    }

    public static JSONObject userDtoToJson(final UserDto userDto) {
        final JSONObject userJson = new JSONObject();
        userJson.put("id", userDto.getId());
        userJson.put("email", userDto.getEmail());
        userJson.put("firstName", userDto.getFirstName());
        userJson.put("surname", userDto.getSurname());
        userJson.put("birthDay", userDto.getBirthDay());
        return userJson;
    }
}
