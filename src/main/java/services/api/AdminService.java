package services.api;

import domain.entities.LinguisticExpression;
import domain.entities.Sentence;
import dtos.AddedDataStatus;
import dtos.MessageDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AdminService {
    List<Sentence> getAllSentences();

    Sentence saveSentence(final Sentence sentence);

    List<LinguisticExpression> getAllLinguisticExpressions();

    LinguisticExpression saveLinguisticExpression(final LinguisticExpression linguisticExpression);

    AddedDataStatus addMessagesFromFile(final MultipartFile fileWithMessags) throws IOException;

    AddedDataStatus addMessageDtos(final List<MessageDto> messageDtos);

    AddedDataStatus addMessages(final List<String> messages);

    /**
     * The csv file from Google Forms.
     * @param csvString the content of the csv file as string. Its structure is: "TimeStamp","Message"
     * @return number of conversations added (we will not add existing conversation)
     */
    AddedDataStatus addMessagesFromCsvString(final String csvString);

    /**
     * WARNING: add all sentences, it does not verify if a sentence already exists
     * A JSON sentence have:
     *      texts: String[]
     *      speechType: SpeechType
     *      informationClassDto: InformationClassDto
     *      informationFieldNamePath: String
     */
    AddedDataStatus addSentencesFromJsonFile(final MultipartFile sentencesJsonFile) throws IOException;

    AddedDataStatus addLinguisticExpressionsFromJsonFile(final MultipartFile linguisticExpressionsJsonFile) throws IOException;
}
