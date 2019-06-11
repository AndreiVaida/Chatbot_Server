package services.api;

import domain.entities.LinguisticExpression;
import domain.entities.Message;
import domain.entities.User;
import domain.information.Information;

import java.util.Arrays;
import java.util.List;

public interface InformationService {
    LinguisticExpression addLinguisticExpression(final LinguisticExpression linguisticExpression);

    List<LinguisticExpression> getAllLinguisticExpressions();

    void deleteLinguisticExpression(final Long expressionId);

    /**
     * @param informationFieldNamePath is the name of the field of the effective information you want to identify.
     *                             This function identifies only 1 effective information, not an entire object Information or sub-object of Information.
     *                             Use "." (dot) to delimit hierarchy fields when you hant to identify an information which is in a sub-object of Information.
     *                             Example for PersonalInformation: "firstName", "birthDay", "address.street", "address.number". Just giving "address" will not work.
     *                             Example for RelationshipInformation: "motherPersonalInformation.firstName", "motherPersonalInformation.address.street".
     *
     *                             The hierarchy may contain maps. In this case, the field name element should contain at the end the key of the element you want to update, separate with #.
     *                             If after # follows ? it means that the field is a map and you want to add the information in map.
     *                             Example for PersonalInformation: "grades#math"
     *                             Example for RelationshipInformation: "kidsPersonalInformation#Matei.firstName", "brothersAndSistersPersonalInformation#?"
     * @param answer is a statement
     * @return the list with the effective identified information (ex: name(s), date, number etc.); otherwise return <null>
     */
    List<Object> identifyAndSetInformation(Class informationClass, String informationFieldNamePath, final Message answer, final User user) throws IllegalAccessException, InstantiationException;
}
