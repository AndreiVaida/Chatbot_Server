package domain.entities;

import domain.enums.SpeechType;
import domain.information.Information;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
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
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "SENTENCES_WORDS",
            joinColumns = {@JoinColumn(name = "SENTENCE_ID")},
            inverseJoinColumns = {@JoinColumn(name = "WORD_ID")}
    )
    @OrderColumn
    private List<Word> words = new ArrayList<>();

    @Column
    private SpeechType speechType;

    @Column
    private Class<Information> informationClass;

    @Column
    private String informationFieldNamePath;

    @ElementCollection
    @CollectionTable(name = "SENTENCE_SYNONYMS",
            joinColumns = @JoinColumn(name = "SENTENCE_ID"))
    private Map<Sentence, Integer> synonyms = new HashMap<>(); // <synonym, frequency>

    @ElementCollection
    @CollectionTable(name = "SENTENCE_RESPONSES",
            joinColumns = @JoinColumn(name = "SENTENCE_ID"))
    private Map<Sentence, Integer> responses = new HashMap<>(); // <response, frequency>

    public Sentence(List<Word> words, SpeechType speechType, Class informationClass, String informationFieldNamePath) {
        this.words = words;
        this.speechType = speechType;
        this.informationClass = informationClass;
        this.informationFieldNamePath = informationFieldNamePath;
    }

    /**
     * Adds the given sentence as a synonym to this one.
     * If this sentence already has the given synonym, then is increased its frequency.
     */
    public void addSynonym(final Sentence synonym) {
        Integer frequency = synonyms.get(synonym);
        if (frequency == null) {
            frequency = 0;
        }
        frequency++;
        synonyms.put(synonym, frequency);
    }

    /**
     * Adds the given sentence as a response to this one.
     * If this sentence already has the given response, then is increased its frequency.
     */
    public void addResponse(final Sentence response) {
        Integer frequency = responses.get(response);
        if (frequency == null) {
            frequency = 0;
        }
        frequency++;
        responses.put(response, frequency);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sentence)) return false;
        Sentence sentence = (Sentence) o;
        return Objects.equals(getWords(), sentence.getWords()) &&
                getSpeechType() == sentence.getSpeechType() &&
                Objects.equals(getInformationClass(), sentence.getInformationClass()) &&
                Objects.equals(getInformationFieldNamePath(), sentence.getInformationFieldNamePath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getWords(), getSpeechType(), getInformationClass(), getInformationFieldNamePath());
    }

    @Override
    public String toString() {
        return "Sentence{" +
                "id=" + id +
                ", words=" + words +
                ", speechType=" + speechType +
                ", informationClass=" + informationClass +
                ", informationFieldNamePath='" + informationFieldNamePath + '\'' +
                '}';
    }
}
