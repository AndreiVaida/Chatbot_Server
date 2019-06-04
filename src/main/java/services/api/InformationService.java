package services.api;

import domain.entities.LinguisticExpression;
import domain.entities.Message;
import domain.information.Information;

import java.util.Arrays;
import java.util.List;

public interface InformationService {
    LinguisticExpression addLinguisticExpression(final LinguisticExpression linguisticExpression);

    List<LinguisticExpression> getAllLinguisticExpressions();

    void deleteLinguisticExpression(final Long expressionId);

    /**
     * @param previousMessage is a directive, statement or acknowledgement (ex: „Care e numele tău ?” or „Spune-mi numele tău !”, „Eu sunt Andy.”, „Salut !”).
     *                        It must have set the fields: informationClass and informationFieldName. (ex: PersonalInformation and FirstName)
     *                        TODO REMOVE It may be null. If it's null, we try to detect automatically what type of information is in answer.
     * @param answer is a statement
     * @return the information object of the previousMessage if we it; otherwise return <null>
     */
    Information identifyInformation(Class informationClass, String informationFieldName, final Message answer);
}
