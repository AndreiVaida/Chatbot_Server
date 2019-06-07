package controllers;

import dtos.LinguisticExpressionDto;
import dtos.MessageDto;
import dtos.SentenceDto;
import facades.api.AdminFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping("/messageDto")
    public ResponseEntity<Integer> addMessagesDto(@RequestBody List<MessageDto> messageDtos) {
        final Integer numberOfAddedMessages = adminFacade.addMessageDtos(messageDtos);
        return new ResponseEntity<>(numberOfAddedMessages, HttpStatus.OK);
    }

    @PostMapping("/message")
    public ResponseEntity<Integer> addMessages(@RequestBody List<String> messages) {
        final Integer numberOfAddedMessages = adminFacade.addMessages(messages);
        return new ResponseEntity<>(numberOfAddedMessages, HttpStatus.OK);
    }

    @PostMapping("/messageFile")
    public ResponseEntity<Integer> addMessagesFromFile(@RequestParam("file") MultipartFile file) throws IOException {
        final Integer numberOfAddedMessages = adminFacade.addMessagesFromFile(file);
        return new ResponseEntity<>(numberOfAddedMessages, HttpStatus.OK);
    }

    /**
     * The csv file from Google Forms.
     * @param csvString the content of the csv file as string. Its structure is: "TimeStamp","Message"
     */
    @PostMapping("/messageCsv")
    public ResponseEntity<Integer> addMessagesFromCsvString(@RequestBody String csvString) {
        final Integer numberOfAddedMessages = adminFacade.addMessagesFromCsvString(csvString);
        return new ResponseEntity<>(numberOfAddedMessages, HttpStatus.OK);
    }
}
