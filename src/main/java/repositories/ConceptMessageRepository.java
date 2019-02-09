package repositories;

import domain.entities.ConceptMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConceptMessageRepository extends JpaRepository<ConceptMessage, Long> {
}
