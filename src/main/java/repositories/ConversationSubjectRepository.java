package repositories;

import domain.entities.ConversationSubject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationSubjectRepository extends JpaRepository<ConversationSubject, Long> {
}
