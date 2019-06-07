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

    Integer addMessageDtos(final List<MessageDto> messageDtos);

    Integer addMessages(final List<String> messages);

    /**
     * The csv file from Google Forms.
     * @param csvString the content of the csv file as string. Its structure is: "TimeStamp","Message"
     * @return number of conversations added (we will not add existing conversation)
     */
    Integer addMessagesFromCsvString(final String csvString);
}
