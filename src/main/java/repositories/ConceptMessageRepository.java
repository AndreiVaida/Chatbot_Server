package repositories;

import domain.entities.ConceptMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConceptMessageRepository extends JpaRepository<ConceptMessage, Long> {
}
