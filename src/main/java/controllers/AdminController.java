package controllers;

import dtos.AddedDataStatus;
import dtos.LinguisticExpressionDto;
import dtos.MessageDto;
import dtos.SentenceDto;
import facades.api.AdminFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    // SENTENCE
    @GetMapping("/sentence")
    public ResponseEntity<List<SentenceDto>> getAllSentences() {
        final List<SentenceDto> sentencesDto = adminFacade.getAllSentences();
        return new ResponseEntity<>(sentencesDto, HttpStatus.OK);
    }

    @PostMapping("/sentence")
    public ResponseEntity<SentenceDto> saveSentence(@RequestBody SentenceDto sentenceDto) {
        sentenceDto = adminFacade.saveSentence(sentenceDto);
        return new ResponseEntity<>(sentenceDto, HttpStatus.OK);
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
     * @param file = a file with Conversations (CSV), QuestionsAndAnswers (CSV), LinguisticExpressions (JSON) or Sentences (JSON)
     * @param fileContentType: "questionsAndAnswers", "sentences", "linguisticExpressions"
     * @return the number of added data (only new data is added)
     */
    @PostMapping("/fileWithData/{fileContentType}")
    public ResponseEntity<AddedDataStatus> addMessagesFromFile(@RequestParam("file") MultipartFile file, @PathVariable String fileContentType) throws IOException {
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

    /**
     * The csv file from Google Forms.
     * @param csvString the content of the csv file as string. Its structure is: "TimeStamp","Message"
     */
    @PostMapping("/messageCsv")
    public ResponseEntity<AddedDataStatus> addMessagesFromCsvString(@RequestBody String csvString) {
        final AddedDataStatus addedDataStatus = adminFacade.addMessagesFromCsvString(csvString);
        return new ResponseEntity<>(addedDataStatus, HttpStatus.OK);
    }
}
