package repositories;

import domain.entities.Sentence;
import domain.entities.Word;
import domain.enums.SpeechType;
import domain.information.Information;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

public interface SentenceRepository extends JpaRepository<Sentence, Long> {
    List<Sentence> findAllBySpeechTypeAndInformationClassAndInformationFieldNamePath(SpeechType speechType, Class<Information> informationClass, String informationFieldNamePath);
    List<Sentence> findAllByWords(List<Word> words);
}
