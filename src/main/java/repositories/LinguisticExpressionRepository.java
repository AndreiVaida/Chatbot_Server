package repositories;


import domain.entities.LinguisticExpression;
import domain.enums.SpeechType;
import domain.information.Information;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.Field;
import java.util.List;

public interface LinguisticExpressionRepository extends JpaRepository<LinguisticExpression, Long> {
    List<LinguisticExpression> findAllBySpeechType(final SpeechType speechType);

    List<LinguisticExpression> findAllByInformationClassAndInformationFieldNameAndSpeechType(final Class<Information> informationClass,
                                                                                         final String informationFieldName,
                                                                                         final SpeechType speechType);
}
