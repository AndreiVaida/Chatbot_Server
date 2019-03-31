package repositories;

import domain.entities.ConceptMessage;
import domain.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConceptMessageRepository extends JpaRepository<ConceptMessage, Long> {
    List<ConceptMessage> findAllByEquivalentMessages(Message message);
}
