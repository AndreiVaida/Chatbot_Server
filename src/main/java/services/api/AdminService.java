package services.api;

import domain.entities.LinguisticExpression;
import domain.entities.Sentence;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AdminService {
    List<Sentence> getAllSentences();

    Sentence saveSentence(final Sentence sentence);

    List<LinguisticExpression> getAllLinguisticExpressions();

    LinguisticExpression saveLinguisticExpression(final LinguisticExpression linguisticExpression);

    Integer addMessages(final MultipartFile fileWithMessags) throws IOException;
}
