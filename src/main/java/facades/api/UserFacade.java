package facades.api;

import dtos.RequestUserRegisterDto;
import dtos.UserDto;
import dtos.informationDtos.InformationClassDto;
import dtos.informationDtos.InformationDto;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface UserFacade {
    List<UserDto> findAll();

    void addUser(final RequestUserRegisterDto requestUserRegisterDto);

    UserDto getUserById(final Long id);

    UserDto findUserByEmail(final String email);

    InformationDto getInformationByClass(final Long userId, final InformationClassDto informationClassDto) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException;

    void deleteInformationByInformationFieldNamePath(final Long userId, final String informationFieldNamePath) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException;
}
