package mappers;

import domain.entities.Address;
import domain.entities.SimpleDate;
import domain.entities.User;
import domain.information.PersonalInformation;
import dtos.AddressDto;
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
        userDto.setProfilePicture(user.getProfilePicture());
        userDto.setIsAdmin(user.getIsAdministrator());
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
        userJson.put("isAdmin", userDto.getIsAdmin());
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

    static AddressDto addressToAddressDto(final Address address) {
        if (address == null) {
            return null;
        }

        final AddressDto addressDto = new AddressDto();
        addressDto.setPlanet(address.getPlanet());
        addressDto.setContinent(address.getContinent());
        addressDto.setCountry(address.getCountry());
        addressDto.setCounty(address.getCounty());
        addressDto.setLocalityType(address.getLocalityType());
        addressDto.setLocality(address.getLocality());
        addressDto.setNeighborhood(address.getNeighborhood());
        addressDto.setStreet(address.getStreet());
        addressDto.setStreetNumber(address.getStreetNumber());
        addressDto.setFloor(address.getFloor());
        addressDto.setApartmentNumber(address.getApartmentNumber());
        return addressDto;
    }
}
