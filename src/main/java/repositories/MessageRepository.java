package repositories;

import domain.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
//    List<Message> findAllByFromUser_IdAndToUser_IdOrToUser_IdAndFromUser_IdOrderByDateTime(Long userId1, Long userId2,
//                                                                                           Long userId1_2, Long userId2_2);
}
