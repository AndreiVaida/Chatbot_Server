package domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
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

    @ManyToMany
    @JoinTable(
            name = "CONCEPT_MESSAGES_MESSAGES",
            joinColumns = {@JoinColumn(name = "CONCEPT_MESSAGE_ID")},
            inverseJoinColumns = {@JoinColumn(name = "MESSAGE_ID")}
    )
    private Set<Message> equivalentMessages;
}
