package services.api;

import domain.entities.LinguisticExpression;
import domain.entities.RejectingExpression;
import domain.entities.Sentence;
import domain.entities.SentenceDetectionParameters;
import domain.enums.ChatbotRequestType;
import dtos.MessageDto;
import dtos.admin.AddedDataStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AdminService {
    // Sentence
    long getNumberOfSentences();

    List<Sentence> getAllSentences();

    List<Sentence> getSentences(final Integer pageNumber, final Integer itemsPerPage);

    List<Sentence> findSentencesByWords(final String wordsAsString);

    List<Sentence> getSentencesById(final List<Long> sentencesId);

    Sentence saveSentence(final Sentence sentence);

    /**
     * Sets the sentence with <synonymId> as a synonym for the sentence with <sentenceId> with the frequency <newFrequency>
     * @param sentenceId must exists in DB
     * @param synonymId must exists in DB, is not necessary to be a synonym of the given sentence
     * @param newFrequency should be >= 0
     */
    Sentence updateSentenceSynonymFrequency(final Long sentenceId, final Long synonymId, final Integer newFrequency);

    /**
     * Sets the sentence with <responseId> as a response for the sentence with <sentenceId> with the frequency <newFrequency>
     * @param sentenceId must exists in DB
     * @param responseId must exists in DB, is not necessary to be a response of the given sentence
     * @param newFrequency should be >= 0
     */
    Sentence updateSentenceResponseFrequency(final Long sentenceId, final Long responseId, final Integer newFrequency);

    List<SentenceDetectionParameters> getSentenceDetectionParameters();

    void setSentenceDetectionParameters(final List<SentenceDetectionParameters> sentenceDetectionParameters);

    // LinguisticExpression
    List<LinguisticExpression> getAllLinguisticExpressions();

    LinguisticExpression saveLinguisticExpression(final LinguisticExpression linguisticExpression);

    void deleteLinguisticExpression(final Long linguisticExpressionId);

    // RejectingExpression
    List<RejectingExpression> getAllRejectingExpressions();

    RejectingExpression saveRejectingExpression(final RejectingExpression rejectingExpression);

    void deleteRejectingExpression(final Long rejectingExpressionId);

    // File/data upload
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
     * The csv file from Google Forms.
     * @param csvFile Its structure is: "timestamp","questions","Ce faci ?","Care e melodia ta preferată ?","Care e filmul tău preferat ?","Cum e la școală ?","Cum e la facultate ?","Lucrezi ?","Cât de deștept ești ?","Ce ai făcut în weekend ?","Care e mâncarea ta preferată ?","Cum a fost la film ?","Ție ce îți place să asculți ?"
     * @param csvFile
     * @return number of questions&answers groups added (we will added existing timestamp)
     */
    AddedDataStatus addAnswersAndQuestionsFromCsvFile(final MultipartFile csvFile) throws IOException;

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

    AddedDataStatus addRejectingExpressionsFromJsonFile(final MultipartFile rejectingExpressionsJsonFile) throws IOException;

    ChatbotRequestType getChatbotRequestType();

    void setChatbotRequestType(final ChatbotRequestType chatbotRequestType);

    AddedDataStatus downloadConversationsFromWebsite();

    AddedDataStatus loadFileConversationsFromWebsite();

    void setSynonyms(final String word1, final String word2);

    AddedDataStatus getDataLoadingStatus();
}
