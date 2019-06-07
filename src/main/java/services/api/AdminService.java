package services.api;

import domain.entities.LinguisticExpression;
import domain.entities.Sentence;
import dtos.MessageDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AdminService {
    List<Sentence> getAllSentences();

    Sentence saveSentence(final Sentence sentence);

    List<LinguisticExpression> getAllLinguisticExpressions();

    LinguisticExpression saveLinguisticExpression(final LinguisticExpression linguisticExpression);

    Integer addMessagesFromFile(final MultipartFile fileWithMessags) throws IOException;

    Integer addMessages(final List<MessageDto> messageDtos);
}
