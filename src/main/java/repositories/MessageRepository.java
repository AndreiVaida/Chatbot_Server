package repositories;

import domain.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m " +
            "WHERE (m.fromUser.id = :userId1 AND m.toUser.id = :userId2) OR (m.fromUser.id = :userId2 AND m.toUser.id = :userId1) " +
            "ORDER BY m.dateTime")
    List<Message> findAllByUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
