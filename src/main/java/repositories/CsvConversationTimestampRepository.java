package repositories;

import domain.entities.CsvConversationTimestamp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface CsvConversationTimestampRepository extends JpaRepository<CsvConversationTimestamp, Long> {
    boolean existsByTimestamp(LocalDateTime timestamp);
}
