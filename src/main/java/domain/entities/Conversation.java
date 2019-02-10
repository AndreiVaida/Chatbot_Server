package domain.entities;

import domain.enums.AgeGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.List;

// Lombok
@NoArgsConstructor
@AllArgsConstructor
@Data
// Hibernate
@Entity
@Table(name = "CONVERSATION")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "CONVERSATION_SUBJECT")
    private ConversationSubject conversationSubject;

    @Column(name = "AGE_GROUP")
    private AgeGroup ageGroup;

    @ManyToMany
    @JoinTable(
            name = "CONVERSATION_CONCEPT_MESSAGE",
            joinColumns = {@JoinColumn(name = "CONVERSATION_ID")},
            inverseJoinColumns = {@JoinColumn(name = "CONCEPT_MESSAGE_ID")}
    )
    private List<ConceptMessage> conceptMessageList;
}
