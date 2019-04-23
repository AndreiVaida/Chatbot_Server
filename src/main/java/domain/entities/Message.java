package domain.entities;

import domain.enums.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CONCEPT_MESSAGE")
    private ConceptMessage conceptMessage;

    @Column
    private Boolean isUnknownMessage = false;

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", fromUser=" + fromUser +
                ", toUser=" + toUser +
                ", message='" + message + '\'' +
                ", messageType=" + messageType +
                ", dateTime=" + dateTime +
                ", conceptMessage=" + conceptMessage +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message message1 = (Message) o;
        return Objects.equals(getId(), message1.getId()) &&
                Objects.equals(getFromUser(), message1.getFromUser()) &&
                Objects.equals(getToUser(), message1.getToUser()) &&
                Objects.equals(getMessage(), message1.getMessage()) &&
                getMessageType() == message1.getMessageType() &&
                Objects.equals(getDateTime(), message1.getDateTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFromUser(), getToUser(), getMessage(), getMessageType(), getDateTime());
    }
}
