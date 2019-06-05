package repositories;


import domain.entities.LinguisticExpression;
import domain.enums.SpeechType;
import domain.information.Information;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LinguisticExpressionRepository extends JpaRepository<LinguisticExpression, Long> {
    List<LinguisticExpression> findAllBySpeechType(final SpeechType speechType);

    List<LinguisticExpression> findAllByInformationClassAndInformationFieldNamePathAndSpeechType(final Class<Information> informationClass,
                                                                                         final String informationFieldNamePath,
                                                                                         final SpeechType speechType);
}
