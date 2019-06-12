package repositories;

import domain.entities.Word;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WordRepository extends JpaRepository<Word, Long> {
    Word getFirstByTextIgnoreCase(String text);

    //List<Word> getByTextWithoutDiacriticsIgnoreCase(final String textWithoutDiacritics);
}
