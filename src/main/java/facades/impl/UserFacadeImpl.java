package facades.impl;

import domain.information.FacultyInformation;
import domain.information.FreeTimeInformation;
import domain.information.Information;
import domain.information.PersonalInformation;
import domain.information.RelationshipsInformation;
import domain.information.SchoolInformation;
import dtos.RequestUserRegisterDto;
import dtos.UserDto;
import dtos.informationDtos.InformationClassDto;
import dtos.informationDtos.InformationDto;
import facades.api.UserFacade;
import mappers.InformationMapper;
import mappers.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import services.api.UserService;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserFacadeImpl implements UserFacade {
    private final UserService userService;

    public UserFacadeImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public List<UserDto> findAll() {
        return userService.findAll().stream().map(UserMapper::userToUserDto).collect(Collectors.toList());
    }

    @Override
    public void addUser(final RequestUserRegisterDto requestUserRegisterDto) {
        userService.addUser(UserMapper.requestUserRegisterDtoToUser(requestUserRegisterDto));
    }

    @Override
    public UserDto getUserById(final Long id) {
        return UserMapper.userToUserDto(userService.getUserById(id));
    }

    @Override
    public UserDto findUserByEmail(final String email) {
        return UserMapper.userToUserDto(userService.findUserByEmail(email));
    }

    @Override
    public InformationDto getInformationByClass(final Long userId, final InformationClassDto informationClassDto) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final Class<Information> informationClass = InformationMapper.informationClassDtoToClassOfInformation(informationClassDto);
        final Information information = userService.getInformationByClass(userId, informationClass);
        if (information == null) {
            return null;
        }

        switch (informationClassDto) {
            case PERSONAL_INFORMATION: return InformationMapper.personalInformationToPersonalInformationDto((PersonalInformation) information);
            case RELATIONSHIPS_INFORMATION: return InformationMapper.relationshipInformationToRelationshipInformationDto((RelationshipsInformation) information);
            case SCHOOL_INFORMATION: return InformationMapper.schoolInformationToSchoolInformationDto((SchoolInformation) information);
            case FACULTY_INFORMATION: return InformationMapper.facultyInformationToFacultyInformationDto((FacultyInformation) information);
            case FREE_TIME_INFORMATION: return InformationMapper.freeTimeInformationToFreeTimeInformationDto((FreeTimeInformation) information);
            default: return null;
        }
    }

    @Override
    public void deleteInformationByInformationFieldNamePath(final Long userId, final String informationFieldNamePath) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, NoSuchFieldException {
        userService.deleteInformationByInformationFieldNamePath(userId, informationFieldNamePath);
    }

    @Override
    public void deleteInformationByInformationClass(final Long userId, final InformationClassDto informationClassDto) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, IntrospectionException, InvocationTargetException {
        final Class<Information> informationClass = InformationMapper.informationClassDtoToClassOfInformation(informationClassDto);
        userService.deleteInformationByInformationClass(userId, informationClass);
    }

    @Override
    public void updateProfilePicture(final Long userId, final MultipartFile profilePicture) throws IOException {
        userService.updateProfilePicture(userId, profilePicture);
    }
}
