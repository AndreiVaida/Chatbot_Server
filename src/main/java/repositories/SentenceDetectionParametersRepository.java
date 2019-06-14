package repositories;

import domain.entities.SentenceDetectionParameters;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SentenceDetectionParametersRepository extends JpaRepository<SentenceDetectionParameters, Integer> {
    List<SentenceDetectionParameters> findAllByOrderBySentenceLength();
    SentenceDetectionParameters findBySentenceLength(Integer sentenceLength);
    SentenceDetectionParameters findTop1ByOrderBySentenceLengthDesc();
}
