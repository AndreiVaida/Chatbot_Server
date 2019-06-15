package controllers;

import domain.enums.ChatbotRequestType;
import dtos.admin.AddedDataStatus;
import dtos.admin.LinguisticExpressionDto;
import dtos.MessageDto;
import dtos.admin.SentenceDetectionParametersDto;
import dtos.admin.SentenceDto;
import facades.api.AdminFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("admin")
public class AdminController extends AbstractController {
    private final AdminFacade adminFacade;

    @Autowired
    public AdminController(AdminFacade adminFacade) {
        this.adminFacade = adminFacade;
    }

    // CHATBOT REQUEST TYPE
    @GetMapping("/chatbotRequestType")
    public ResponseEntity<ChatbotRequestType> getChatbotRequestType() {
        return new ResponseEntity<>(adminFacade.getChatbotRequestType(), HttpStatus.OK);
    }

    @PostMapping("/chatbotRequestType")
    public ResponseEntity<?> setChatbotRequestType(@RequestParam ChatbotRequestType chatbotRequestType) {
        adminFacade.setChatbotRequestType(chatbotRequestType);
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    // SENTENCE
    @GetMapping("/sentence/all")
    public ResponseEntity<List<SentenceDto>> getAllSentences(@RequestParam(defaultValue = "0") final Integer pageNumber,
                                                             @RequestParam(defaultValue = "100") final Integer itemsPerPage) {
        final List<SentenceDto> sentencesDto = adminFacade.getSentences(pageNumber, itemsPerPage);
        return new ResponseEntity<>(sentencesDto, HttpStatus.OK);
    }

    @GetMapping("/sentence")
    public ResponseEntity<List<SentenceDto>> getSentencesById(@RequestParam final List<Long> sentencesId) {
        final List<SentenceDto> sentencesDto = adminFacade.getSentencesById(sentencesId);
        return new ResponseEntity<>(sentencesDto, HttpStatus.OK);
    }

    @GetMapping("/sentence/find")
    public ResponseEntity<List<SentenceDto>> findSentencesByWords(@RequestParam final String wordsAsString) {
        final List<SentenceDto> sentencesDto = adminFacade.findSentencesByWords(wordsAsString);
        return new ResponseEntity<>(sentencesDto, HttpStatus.OK);
    }

    @GetMapping("/sentence/nr")
    public ResponseEntity<Long> getNumberOfSentences() {
        final Long nrOfSentences = adminFacade.getNumberOfSentences();
        return new ResponseEntity<>(nrOfSentences, HttpStatus.OK);
    }

    @PostMapping("/sentence")
    public ResponseEntity<SentenceDto> saveSentence(@RequestBody SentenceDto sentenceDto) {
        sentenceDto = adminFacade.saveSentence(sentenceDto);
        return new ResponseEntity<>(sentenceDto, HttpStatus.OK);
    }

    @PostMapping("/sentence/synonym/{sentenceId}")
    public ResponseEntity<SentenceDto> updateSentenceSynonymFrequency(@PathVariable final Long sentenceId, @RequestParam final Long synonymId,@RequestParam final Integer newFrequency) {
        final SentenceDto sentenceDto = adminFacade.updateSentenceSynonymFrequency(sentenceId, synonymId, newFrequency);
        return new ResponseEntity<>(sentenceDto, HttpStatus.OK);
    }

    @PostMapping("/sentence/response/{sentenceId}")
    public ResponseEntity<SentenceDto> updateSentenceResponseFrequency(@PathVariable final Long sentenceId, @RequestParam final Long responseId,@RequestParam final Integer newFrequency) {
        final SentenceDto sentenceDto = adminFacade.updateSentenceResponseFrequency(sentenceId, responseId, newFrequency);
        return new ResponseEntity<>(sentenceDto, HttpStatus.OK);
    }

    @GetMapping("/sentence/parameters")
    public ResponseEntity<List<SentenceDetectionParametersDto>> getSentenceDetectionParameters() {
        final List<SentenceDetectionParametersDto> sentenceDetectionParametersDto = adminFacade.getSentenceDetectionParameters();
        return new ResponseEntity<>(sentenceDetectionParametersDto, HttpStatus.OK);
    }

    @PutMapping("/sentence/parameters")
    public ResponseEntity<?> setSentenceDetectionParameters(@RequestBody List<SentenceDetectionParametersDto> sentenceDetectionParametersDto) {
        adminFacade.setSentenceDetectionParameters(sentenceDetectionParametersDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // LINGUISTIC EXPRESSION
    @GetMapping("/linguisticExpression")
    public ResponseEntity<List<LinguisticExpressionDto>> getAllLinguisticExpressions() {
        final List<LinguisticExpressionDto> linguisticExpressionDtos = adminFacade.getAllLinguisticExpressions();
        return new ResponseEntity<>(linguisticExpressionDtos, HttpStatus.OK);
    }

    @PostMapping("/linguisticExpression")
    public ResponseEntity<LinguisticExpressionDto> saveLinguisticExpression(@RequestBody LinguisticExpressionDto linguisticExpressionDto) {
        linguisticExpressionDto = adminFacade.saveLinguisticExpression(linguisticExpressionDto);
        return new ResponseEntity<>(linguisticExpressionDto, HttpStatus.OK);
    }

    @DeleteMapping("/linguisticExpression/{linguisticExpressionId}")
    public ResponseEntity<?> deleteLinguisticExpression(@PathVariable final Long linguisticExpressionId) {
        adminFacade.deleteLinguisticExpression(linguisticExpressionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // MESSAGE
    @PostMapping("/messageDto")
    public ResponseEntity<AddedDataStatus> addMessagesDto(@RequestBody List<MessageDto> messageDtos) {
        final AddedDataStatus addedDataStatus = adminFacade.addMessageDtos(messageDtos);
        return new ResponseEntity<>(addedDataStatus, HttpStatus.OK);
    }

    @PostMapping("/message")
    public ResponseEntity<AddedDataStatus> addMessages(@RequestBody List<String> messages) {
        final AddedDataStatus addedDataStatus = adminFacade.addMessages(messages);
        return new ResponseEntity<>(addedDataStatus, HttpStatus.OK);
    }

    /**
     * The csv file from Google Forms.
     * @param csvString the content of the csv file as string. Its structure is: "TimeStamp","Message"
     */
    @PostMapping("/messageCsv")
    public ResponseEntity<AddedDataStatus> addMessagesFromCsvString(@RequestBody String csvString) {
        final AddedDataStatus addedDataStatus = adminFacade.addMessagesFromCsvString(csvString);
        return new ResponseEntity<>(addedDataStatus, HttpStatus.OK);
    }

    // FILE UPLOAD
    /**
     * @param file = a file with Conversations (CSV), QuestionsAndAnswers (CSV), LinguisticExpressions (JSON) or Sentences (JSON)
     * @param fileContentType: "questionsAndAnswers", "sentences", "linguisticExpressions"
     * @return the number of added data (only new data is added)
     */
    @PostMapping("/fileWithData/{fileContentType}")
    public ResponseEntity<AddedDataStatus> addDataFromFile(@RequestParam("file") MultipartFile file, @PathVariable String fileContentType) throws IOException {
        switch (fileContentType) {
            case "conversations": {
                final AddedDataStatus addedDataStatus = adminFacade.addMessagesFromFile(file);
                return new ResponseEntity<>(addedDataStatus, HttpStatus.OK);
            }
            case "questionsAndAnswers": {
//                final AddedDataStatus addedDataStatus = adminFacade.addQuestionsAndAnswersFromCsvFile(file);
//                return new ResponseEntity<>(addedDataStatus, HttpStatus.OK);
                return new ResponseEntity<>(new AddedDataStatus(0,0), HttpStatus.ACCEPTED);
            }
            case "sentences": {
                final AddedDataStatus addedDataStatus = adminFacade.addSentencesFromJsonFile(file);
                return new ResponseEntity<>(addedDataStatus, HttpStatus.OK);
            }
            case "linguisticExpressions": {
                final AddedDataStatus addedDataStatus = adminFacade.addLinguisticExpressionsFromJsonFile(file);
                return new ResponseEntity<>(addedDataStatus, HttpStatus.OK);
            }
            default: {
                return new ResponseEntity<>(new AddedDataStatus(0,0), HttpStatus.BAD_REQUEST);
            }
        }
    }

    // Get conversations from internet forums (Triburile)
    @GetMapping("/website")
    public ResponseEntity<AddedDataStatus> addConversationsFromWebsite() {
        final AddedDataStatus addedDataStatus = adminFacade.addConversationsFromWebsite();
        return new ResponseEntity<>(addedDataStatus, HttpStatus.OK);
    }
}
