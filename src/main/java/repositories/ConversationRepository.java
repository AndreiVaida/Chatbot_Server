package repositories;

import domain.entities.ConceptMessage;
import domain.entities.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findAllByConceptMessageList(ConceptMessage conceptMessage);
}
