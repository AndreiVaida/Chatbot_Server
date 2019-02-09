package domain.entities;

import com.sun.istack.internal.NotNull;
import domain.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;

// Lombok
@NoArgsConstructor
@AllArgsConstructor
@Data
// Hibernate
@Entity
@Table(name = "MESSAGES")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "FROM_USER")
    private User fromUser;

    @ManyToOne
    @JoinColumn(name = "TO_USER")
    private User toUser;

    @NotNull
    @Column
    private String message;

    @Column
    private MessageType messageType;

    @Column
    private LocalDateTime dateTime;
}
