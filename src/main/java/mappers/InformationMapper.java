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
import dtos.InformationClass;
import dtos.LinguisticExpressionDto;

import java.util.List;
import java.util.stream.Collectors;

import static dtos.InformationClass.FACULTY_INFORMATION;
import static dtos.InformationClass.FREE_TIME_INFORMATION;
import static dtos.InformationClass.PERSONAL_INFORMATION;
import static dtos.InformationClass.RELATIONSHIPS_INFORMATION;
import static dtos.InformationClass.SCHOOL_INFORMATION;

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
        linguisticExpression.setInformationClass(informationClassDtoToClassOfInformation(linguisticExpressionDto.getInformationClass()));
        linguisticExpression.setInformationFieldName(linguisticExpressionDto.getInformationFieldName());
        return linguisticExpression;
    }

    private static Class informationClassDtoToClassOfInformation(final InformationClass informationClass) {
        switch (informationClass) {
            case PERSONAL_INFORMATION: return PersonalInformation.class;
            case RELATIONSHIPS_INFORMATION: return RelationshipsInformation.class;
            case SCHOOL_INFORMATION: return SchoolInformation.class;
            case FACULTY_INFORMATION: return FacultyInformation.class;
            case FREE_TIME_INFORMATION: return FreeTimeInformation.class;
            case GASTRONOMY_INFORMATION: return null;
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
        linguisticExpressionDto.setInformationClass(informationClassToClassOfInformationDto(linguisticExpression.getInformationClass()));
        linguisticExpressionDto.setInformationFieldName(linguisticExpression.getInformationFieldName());
        return linguisticExpressionDto;
    }

    private static InformationClass informationClassToClassOfInformationDto(final Class<Information> informationClass) {
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

    private static ExpressionItemDto expressionItemToExpressionItemDto(final ExpressionItem expressionItem) {
        final ExpressionItemDto expressionItemDto = new ExpressionItemDto();
        expressionItemDto.setText(expressionItem.getText());
        expressionItemDto.setItemClass(expressionItem.getItemClass());
        return expressionItemDto;
    }
}
