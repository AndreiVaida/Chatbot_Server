package mappers;

import domain.entities.SimpleDate;
import domain.entities.User;
import dtos.RequestUserRegisterDto;
import dtos.SimpleDateDto;
import dtos.UserDto;
import org.json.simple.JSONObject;

public class UserMapper {
    private UserMapper() {}


    public static User requestUserRegisterDtoToUser(final RequestUserRegisterDto requestUserRegisterDto) {
        final User user = new User();
        user.setEmail(requestUserRegisterDto.getEmail());
        user.setPassword(requestUserRegisterDto.getPassword());
        user.getPersonalInformation().setFirstName(requestUserRegisterDto.getFirstName());
        user.getPersonalInformation().setSurname(requestUserRegisterDto.getSurname());
        user.getPersonalInformation().setBirthDay(simpleDateDtoToSimpleDate(requestUserRegisterDto.getBirthDay()));
        return user;
    }

    public static UserDto userToUserDto(final User user) {
        final UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getPersonalInformation().getFirstName());
        userDto.setSurname(user.getPersonalInformation().getSurname());
        userDto.setBirthDay(simpleDateToSimpleDateDto(user.getPersonalInformation().getBirthDay()));
        return userDto;
    }

    private static SimpleDate simpleDateDtoToSimpleDate(final SimpleDateDto simpleDateDto) {
        return new SimpleDate(simpleDateDto.getYear(), simpleDateDto.getMonth(), simpleDateDto.getDay());
    }

    private static SimpleDateDto simpleDateToSimpleDateDto(final SimpleDate simpleDate) {
        return new SimpleDateDto(simpleDate.getYear(), simpleDate.getMonth(), simpleDate.getDay());
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
