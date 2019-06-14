package facades.api;

import dtos.admin.LinguisticExpressionDto;

import java.util.List;

public interface InformationFacade {
    void addLinguisticExpression(final LinguisticExpressionDto linguisticExpressionDto);

    List<LinguisticExpressionDto> getAllLinguisticExpressions();

    void deleteLinguisticExpression(final Long expressionId);
}
