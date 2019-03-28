package domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


// Lombok
@NoArgsConstructor
@AllArgsConstructor
@Data
// Hibernate
@Entity
@Table(name = "CONCEPT_MESSAGES")
public class ConceptMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "CONCEPT_MESSAGES_MESSAGES",
            joinColumns = {@JoinColumn(name = "CONCEPT_MESSAGE_ID")},
            inverseJoinColumns = {@JoinColumn(name = "MESSAGE_ID")}
    )
    private Set<Message> equivalentMessages = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "CONCEPT_MESSAGES_RESPONSES",
            joinColumns = {@JoinColumn(name = "CONCEPT_MESSAGE_ID")},
            inverseJoinColumns = {@JoinColumn(name = "RESPONSE_ID")}
    )
    private Set<ConceptMessage> responses = new HashSet<>();

    @Override
    public String toString() {
        return "ConceptMessage{" +
                "id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConceptMessage)) return false;
        ConceptMessage that = (ConceptMessage) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
