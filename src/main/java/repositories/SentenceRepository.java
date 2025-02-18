package repositories;

import domain.entities.Sentence;
import domain.entities.Word;
import domain.enums.SpeechType;
import domain.information.Information;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SentenceRepository extends JpaRepository<Sentence, Long> {
    Page<Sentence> findAll(Pageable pageRequest);
    List<Sentence> findAllBySpeechTypeAndInformationClassAndInformationFieldNamePath(SpeechType speechType, Class<Information> informationClass, String informationFieldNamePath);
    List<Sentence> findAllByWords(List<Word> words);
}
