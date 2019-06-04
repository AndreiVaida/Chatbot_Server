package services.api;

import domain.entities.LinguisticExpression;

import java.util.Arrays;
import java.util.List;

public interface InformationService {
    LinguisticExpression addLinguisticExpression(final LinguisticExpression linguisticExpression);

    List<LinguisticExpression> getAllLinguisticExpressions();

    void deleteLinguisticExpression(final Long expressionId);
}
