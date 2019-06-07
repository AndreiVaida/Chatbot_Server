package facades.api;

import dtos.LinguisticExpressionDto;
import dtos.MessageDto;
import dtos.SentenceDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AdminFacade {
    List<SentenceDto> getAllSentences();

    SentenceDto saveSentence(final SentenceDto sentenceDto);

    List<LinguisticExpressionDto> getAllLinguisticExpressions();

    LinguisticExpressionDto saveLinguisticExpression(final LinguisticExpressionDto linguisticExpressionDto);

    Integer addMessagesFromFile(final MultipartFile fileWithMessages) throws IOException;

    Integer addMessages(final List<MessageDto> messageDtos);
}
