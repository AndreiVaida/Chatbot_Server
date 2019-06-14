package mappers;

import domain.entities.ExpressionItem;
import domain.entities.LinguisticExpression;
import domain.information.FacultyInformation;
import domain.information.FreeTimeInformation;
import domain.information.Information;
import domain.information.PersonalInformation;
import domain.information.RelationshipsInformation;
import domain.information.SchoolInformation;
import dtos.ExpressionItemDto;
import dtos.informationDtos.FacultyInformationDto;
import dtos.informationDtos.FreeTimeInformationDto;
import dtos.informationDtos.InformationClassDto;
import dtos.LinguisticExpressionDto;
import dtos.informationDtos.InformationDto;
import dtos.informationDtos.PersonalInformationDto;
import dtos.informationDtos.RelationshipInformationDto;
import dtos.informationDtos.SchoolInformationDto;

import java.util.List;
import java.util.stream.Collectors;

import static dtos.informationDtos.InformationClassDto.FACULTY_INFORMATION;
import static dtos.informationDtos.InformationClassDto.FREE_TIME_INFORMATION;
import static dtos.informationDtos.InformationClassDto.PERSONAL_INFORMATION;
import static dtos.informationDtos.InformationClassDto.RELATIONSHIPS_INFORMATION;
import static dtos.informationDtos.InformationClassDto.SCHOOL_INFORMATION;

public class InformationMapper {
    private InformationMapper(){}

    /**
     * WARNING: the id is ignored
     */
    static public LinguisticExpression linguisticExpressionDtoToLinguisticExpression(final LinguisticExpressionDto linguisticExpressionDto) {
        final LinguisticExpression linguisticExpression = new LinguisticExpression();
        final List<ExpressionItem> expressionItems = linguisticExpressionDto.getExpressionItems().stream()
                .map(InformationMapper::expressionItemDtoToExpressionItem)
                .collect(Collectors.toList());

        linguisticExpression.setItems(expressionItems);
        linguisticExpression.setSpeechType(linguisticExpressionDto.getSpeechType());
        linguisticExpression.setInformationClass(informationClassDtoToClassOfInformation(linguisticExpressionDto.getInformationClassDto()));
        linguisticExpression.setInformationFieldNamePath(linguisticExpressionDto.getInformationFieldNamePath());
        return linguisticExpression;
    }

    public static Class informationClassDtoToClassOfInformation(final InformationClassDto informationClassDto) {
        switch (informationClassDto) {
            case PERSONAL_INFORMATION: return PersonalInformation.class;
            case RELATIONSHIPS_INFORMATION: return RelationshipsInformation.class;
            case SCHOOL_INFORMATION: return SchoolInformation.class;
            case FACULTY_INFORMATION: return FacultyInformation.class;
            case FREE_TIME_INFORMATION: return FreeTimeInformation.class;
            case GASTRONOMY_INFORMATION: return null;
        }
        return null;
    }

    public static InformationClassDto informationClassToClassOfInformationDto(final Class<Information> informationClass) {
        if (PersonalInformation.class.equals(informationClass)) {
            return PERSONAL_INFORMATION;
        } else if (RelationshipsInformation.class.equals(informationClass)) {
            return RELATIONSHIPS_INFORMATION;
        } else if (SchoolInformation.class.equals(informationClass)) {
            return SCHOOL_INFORMATION;
        } else if (FacultyInformation.class.equals(informationClass)) {
            return FACULTY_INFORMATION;
        } else if (FreeTimeInformation.class.equals(informationClass)) {
            return FREE_TIME_INFORMATION;
//            case GASTRONOMY_INFORMATION: return null;
        }
        return null;
    }

    private static ExpressionItem expressionItemDtoToExpressionItem(final ExpressionItemDto expressionItemDto) {
        final ExpressionItem expressionItem = new ExpressionItem();
        expressionItem.setText(expressionItemDto.getText());
        expressionItem.setItemClass(expressionItemDto.getItemClass());
        return expressionItem;
    }

    public static LinguisticExpressionDto linguisticExpressionToLinguisticExpressionDto(final LinguisticExpression linguisticExpression) {
        final LinguisticExpressionDto linguisticExpressionDto = new LinguisticExpressionDto();
        linguisticExpressionDto.setId(linguisticExpression.getId());
        final List<ExpressionItemDto> expressionItemDtos = linguisticExpression.getItems().stream()
                .map(InformationMapper::expressionItemToExpressionItemDto)
                .collect(Collectors.toList());

        linguisticExpressionDto.setExpressionItems(expressionItemDtos);
        linguisticExpressionDto.setSpeechType(linguisticExpression.getSpeechType());
        linguisticExpressionDto.setInformationClassDto(informationClassToClassOfInformationDto(linguisticExpression.getInformationClass()));
        linguisticExpressionDto.setInformationFieldNamePath(linguisticExpression.getInformationFieldNamePath());
        return linguisticExpressionDto;
    }

    private static ExpressionItemDto expressionItemToExpressionItemDto(final ExpressionItem expressionItem) {
        final ExpressionItemDto expressionItemDto = new ExpressionItemDto();
        expressionItemDto.setText(expressionItem.getText());
        expressionItemDto.setItemClass(expressionItem.getItemClass());
        return expressionItemDto;
    }

    public static PersonalInformationDto personalInformationToPersonalInformationDto(final PersonalInformation entity) {
        final PersonalInformationDto dto = new PersonalInformationDto();
        dto.setFirstName(entity.getFirstName());
        dto.setSurname(entity.getSurname());
        dto.setBirthDay(UserMapper.simpleDateToSimpleDateDto(entity.getBirthDay()));
        dto.setGender(entity.getGender());
        dto.setHomeAddress(entity.getHomeAddress());
        return dto;
    }

    public static RelationshipInformationDto relationshipInformationToRelationshipInformationDto(final RelationshipsInformation entity) {
        final RelationshipInformationDto dto = new RelationshipInformationDto();
        dto.setMotherPersonalInformation(personalInformationToPersonalInformationDto(entity.getMotherPersonalInformation()));
        dto.setFatherPersonalInformation(personalInformationToPersonalInformationDto(entity.getFatherPersonalInformation()));
        dto.setNumberOfBrothersAndSisters(entity.getNumberOfBrothersAndSisters());
        dto.setBrothersAndSistersPersonalInformation(entity.getBrothersAndSistersPersonalInformation().values().stream()
                .collect(Collectors.toMap(PersonalInformation::getFirstName, information ->
                        personalInformationToPersonalInformationDto(entity.getBrothersAndSistersPersonalInformation().get(information.getFirstName())))));
        dto.setNumberOfGrandparents(entity.getNumberOfGrandparents());
        dto.setGrandparentsPersonalInformation(entity.getGrandparentsPersonalInformation().values().stream()
                .collect(Collectors.toMap(PersonalInformation::getFirstName, information ->
                        personalInformationToPersonalInformationDto(entity.getGrandparentsPersonalInformation().get(information.getFirstName())))));
        dto.setWifeOrHusbandInformation(personalInformationToPersonalInformationDto(entity.getWifeOrHusbandInformation()));
        dto.setNumberOfKids(entity.getNumberOfKids());
        dto.setKidsPersonalInformation(entity.getKidsPersonalInformation().values().stream()
                .collect(Collectors.toMap(PersonalInformation::getFirstName, information ->
                        personalInformationToPersonalInformationDto(entity.getKidsPersonalInformation().get(information.getFirstName())))));
        return dto;
    }

    public static SchoolInformationDto schoolInformationToSchoolInformationDto(final SchoolInformation entity) {
        final SchoolInformationDto dto = new SchoolInformationDto();
        dto.setIsAtSchool(entity.getIsAtSchool());
        dto.setSchoolName(entity.getSchoolName());
        dto.setSchoolProfile(entity.getSchoolProfile());
        dto.setSchoolClass(entity.getSchoolClass());
        dto.setFavouriteCourse(entity.getFavouriteCourse());
        dto.setFavouriteProfessor(entity.getFavouriteProfessor());
        dto.setBestFriend(entity.getBestFriend());
        dto.setCoursesGrades(entity.getCoursesGrades());
        return dto;
    }

    public static FacultyInformationDto facultyInformationToFacultyInformationDto(final FacultyInformation entity) {
        final FacultyInformationDto dto = new FacultyInformationDto();
        dto.setIsAtFaculty(entity.getIsAtFaculty());
        dto.setFacultyName(entity.getFacultyName());
        dto.setFacultySpecialization(entity.getFacultySpecialization());
        dto.setFacultyYear(entity.getFacultyYear());
        dto.setFacultyGroup(entity.getFacultyGroup());
        dto.setFavouriteCourse(entity.getFavouriteCourse());
        dto.setFavouriteProfessor(entity.getFavouriteProfessor());
        dto.setBestFriend(entity.getBestFriend());
        dto.setCoursesGrades(entity.getCoursesGrades());
        return dto;
    }

    public static FreeTimeInformationDto freeTimeInformationToFreeTimeInformationDto(final FreeTimeInformation entity) {
        final FreeTimeInformationDto dto = new FreeTimeInformationDto();
        dto.setHobbies(entity.getHobbies());
        dto.setLikeReading(entity.getLikeReading());
        dto.setFavouriteBook(entity.getFavouriteBook());
        dto.setCurrentReadingBook(entity.getCurrentReadingBook());
        dto.setLikeVideoGames(entity.getLikeVideoGames());
        dto.setFavouriteVideoGame(entity.getFavouriteVideoGame());
        dto.setCurrentPlayedGame(entity.getCurrentPlayedGame());
        dto.setLikeBoardGames(entity.getLikeBoardGames());
        dto.setFavouriteBoardGame(entity.getFavouriteBoardGame());
        dto.setCurrentReadingBook(entity.getCurrentBoardGame());
        return dto;
    }
}
