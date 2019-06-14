package dtos.admin;

import domain.enums.SpeechType;
import dtos.informationDtos.InformationClassDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LinguisticExpressionDto {
    private Long id;
    private List<ExpressionItemDto> expressionItems;
    private SpeechType speechType;
    private InformationClassDto informationClassDto;
    private String informationFieldNamePath;
}
