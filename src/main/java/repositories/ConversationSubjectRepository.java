package repositories;

import domain.entities.ConversationSubject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationSubjectRepository extends JpaRepository<ConversationSubject, Long> {
}
