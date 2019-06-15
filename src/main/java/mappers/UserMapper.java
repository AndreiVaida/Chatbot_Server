package mappers;

import domain.entities.SimpleDate;
import domain.entities.User;
import domain.information.PersonalInformation;
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
        final PersonalInformation personalInformation = new PersonalInformation();
        user.setPersonalInformation(personalInformation);
        user.getPersonalInformation().setFirstName(requestUserRegisterDto.getFirstName());
        return user;
    }

    public static UserDto userToUserDto(final User user) {
        final UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        if (user.getPersonalInformation() != null) {
            userDto.setFirstName(user.getPersonalInformation().getFirstName());
            userDto.setSurname(user.getPersonalInformation().getSurname());
            userDto.setBirthDay(simpleDateToSimpleDateDto(user.getPersonalInformation().getBirthDay()));
        }
        return userDto;
    }

    private static SimpleDate simpleDateDtoToSimpleDate(final SimpleDateDto simpleDateDto) {
        if (simpleDateDto == null) {
            return null;
        }
        return new SimpleDate(simpleDateDto.getYear(), simpleDateDto.getMonth(), simpleDateDto.getDay());
    }

    static SimpleDateDto simpleDateToSimpleDateDto(final SimpleDate simpleDate) {
        if (simpleDate == null) {
            return null;
        }
        return new SimpleDateDto(simpleDate.getYear(), simpleDate.getMonth(), simpleDate.getDay());
    }

    public static JSONObject userDtoToJson(final UserDto userDto) {
        final JSONObject userJson = new JSONObject();
        userJson.put("id", userDto.getId());
        userJson.put("email", userDto.getEmail());
        userJson.put("firstName", userDto.getFirstName());
        userJson.put("surname", userDto.getSurname());
        userJson.put("birthDay", simpleDateDtoToJson(userDto.getBirthDay()));
        return userJson;
    }

    private static JSONObject simpleDateDtoToJson(final SimpleDateDto simpleDateDto) {
        final JSONObject dateJson = new JSONObject();
        if (simpleDateDto == null) {
            return  dateJson;
        }
        dateJson.put("year", simpleDateDto.getYear());
        dateJson.put("month", simpleDateDto.getMonth());
        dateJson.put("day", simpleDateDto.getDay());
        return dateJson;
    }
}
