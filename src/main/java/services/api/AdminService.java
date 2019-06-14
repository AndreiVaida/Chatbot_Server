package services.api;

import domain.entities.LinguisticExpression;
import domain.entities.Sentence;
import domain.entities.SentenceDetectionParameters;
import domain.enums.ChatbotRequestType;
import dtos.admin.AddedDataStatus;
import dtos.MessageDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public interface AdminService {
    long getNumberOfSentences();

    List<Sentence> getAllSentences();

    List<Sentence> getSentences(final Integer pageNumber, final Integer itemsPerPage);

    List<Sentence> findSentencesByWords(final String wordsAsString);

    List<Sentence> getSentencesById(final List<Long> sentencesId);

    Sentence saveSentence(final Sentence sentence);

    List<SentenceDetectionParameters> getSentenceDetectionParameters();

    void setSentenceDetectionParameters(final List<SentenceDetectionParameters> sentenceDetectionParameters);

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

    ChatbotRequestType getChatbotRequestType();

    void setChatbotRequestType(final ChatbotRequestType chatbotRequestType);

    AddedDataStatus downloadConversationsFromWebsite();

    AddedDataStatus loadFileConversationsFromWebsite();
}
