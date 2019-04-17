package domain.entities;

import domain.enums.SentenceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Lombok
@NoArgsConstructor
@AllArgsConstructor
@Data
// Hibernate
@Entity
@Table(name = "SENTENCES")
public class Sentence {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "SENTENCES_WORDS",
            joinColumns = {@JoinColumn(name = "SENTENCE_ID")},
            inverseJoinColumns = {@JoinColumn(name = "WORD_ID")}
    )
    private List<Word> words = new ArrayList<>();

    @Column
    private SentenceType sentenceType;

    @ElementCollection
    @CollectionTable(name="SENTENCE_SYNONYMS",
            joinColumns=@JoinColumn(name="SYNONYM_ID"))
    private Map<Sentence, Integer> synonyms = new HashMap<>(); // <synonym, frequency>

    @ElementCollection
    @CollectionTable(name="SENTENCE_RESPONSES",
            joinColumns=@JoinColumn(name="SYNONYM_ID"))
    private Map<Sentence, Integer> responses = new HashMap<>(); // <response, frequency>
}
