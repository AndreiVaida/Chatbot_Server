package repositories;

import domain.entities.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m " +
            "WHERE (m.fromUser.id = :userId1 AND m.toUser.id = :userId2) OR (m.fromUser.id = :userId2 AND m.toUser.id = :userId1) " +
            "ORDER BY m.dateTime")
    List<Message> findAllByUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT m FROM Message m " +
            "WHERE (m.fromUser.id = :userId1 AND m.toUser.id = :userId2) OR (m.fromUser.id = :userId2 AND m.toUser.id = :userId1) " +
            "AND m.dateTime <= :maxDateTime " +
            "ORDER BY m.dateTime DESC")
    List<Message> findAllByUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2,
                                 @Param("maxDateTime") LocalDateTime maxDateTime, Pageable pageRequest);

    @Query("SELECT m FROM Message m " +
            "WHERE m.id <> :messageId " +
            "AND ((m.fromUser.id = :userId1 AND m.toUser.id = :userId2) OR (m.fromUser.id = :userId2 AND m.toUser.id = :userId1)) " +
            "ORDER BY m.dateTime DESC, m.id DESC")
    List<Message> getPreviousMessages(@Param("userId1") Long userId1, @Param("userId2") Long userId2,
                                      @Param("messageId") Long messageId, Pageable pageRequest);

//    @Query("SELECT m FROM Message m " +
//            "WHERE m.fromUser.id = :fromUserId AND m.toUser.id = :toUserId " +
//            "ORDER BY m.dateTime DESC, m.id DESC")
    List<Message> getAllByFromUser_IdAndToUser_IdOrderByDateTimeDesc(Long fromUserId, Long toUserId, Pageable pageRequest);
}
