package facades.api;

import dtos.admin.LinguisticExpressionDto;

import java.util.List;

public interface InformationDetectionFacade {
    void addLinguisticExpression(final LinguisticExpressionDto linguisticExpressionDto);

    List<LinguisticExpressionDto> getAllLinguisticExpressions();

    void deleteLinguisticExpression(final Long expressionId);
}
