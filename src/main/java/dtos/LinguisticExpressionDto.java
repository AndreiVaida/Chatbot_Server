package dtos;

import domain.enums.SpeechType;
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
    private InformationClass informationClass;
    private String informationFieldName;
}
