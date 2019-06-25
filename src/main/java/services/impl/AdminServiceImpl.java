package services.impl;

import app.Main;
import com.opencsv.CSVReader;
import domain.entities.CsvConversationTimestamp;
import domain.entities.LinguisticExpression;
import domain.entities.Message;
import domain.entities.RejectingExpression;
import domain.entities.Sentence;
import domain.entities.SentenceDetectionParameters;
import domain.entities.User;
import domain.entities.Word;
import domain.enums.ChatbotRequestType;
import domain.enums.ItemClass;
import domain.enums.MessageSource;
import domain.enums.SpeechType;
import dtos.MessageDto;
import dtos.admin.AddedDataStatus;
import dtos.admin.ExpressionItemDto;
import dtos.admin.LinguisticExpressionDto;
import dtos.admin.WordDto;
import dtos.informationDtos.InformationClassDto;
import mappers.InformationMapper;
import mappers.SentenceMapper;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import repositories.CsvConversationTimestampRepository;
import repositories.LinguisticExpressionRepository;
import repositories.RejectingExpressionRepository;
import repositories.SentenceDetectionParametersRepository;
import repositories.SentenceRepository;
import repositories.WordRepository;
import services.api.AdminService;
import services.api.ChatService;
import services.api.ChatbotService;
import services.api.UserService;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static services.impl.ChatbotServiceImpl.splitInWords;

@Service
public class AdminServiceImpl implements AdminService {
    private final SentenceRepository sentenceRepository;
    private final WordRepository wordRepository;
    private final LinguisticExpressionRepository linguisticExpressionRepository;
    private final RejectingExpressionRepository rejectingExpressionRepository;
    private final ChatService chatService;
    private final ChatbotService chatbotService;
    private final UserService userService;
    private final SentenceDetectionParametersRepository sentenceDetectionParametersRepository;
    private final CsvConversationTimestampRepository csvConversationTimestampRepository;
    private final String fileWithConversations = "src/main/resources/conversations/ConversationsTriburile1.txt";

    public AdminServiceImpl(SentenceRepository sentenceRepository, WordRepository wordRepository, LinguisticExpressionRepository linguisticExpressionRepository, RejectingExpressionRepository rejectingExpressionRepository, ChatService chatService, ChatbotService chatbotService, UserService userService, SentenceDetectionParametersRepository sentenceDetectionParametersRepository, CsvConversationTimestampRepository csvConversationTimestampRepository) {
        this.sentenceRepository = sentenceRepository;
        this.wordRepository = wordRepository;
        this.linguisticExpressionRepository = linguisticExpressionRepository;
        this.rejectingExpressionRepository = rejectingExpressionRepository;
        this.chatService = chatService;
        this.chatbotService = chatbotService;
        this.userService = userService;
        this.sentenceDetectionParametersRepository = sentenceDetectionParametersRepository;
        this.csvConversationTimestampRepository = csvConversationTimestampRepository;
    }

    @Override
    public long getNumberOfSentences() {
        return sentenceRepository.count();
    }

    @Override
    public List<Sentence> getAllSentences() {
        return sentenceRepository.findAll();
    }

    @Override
    public Sentence saveSentence(final Sentence sentence) {
        for (int i = 0; i < sentence.getWords().size(); i++) {
            final Word word = sentence.getWords().get(i);
            final Word existingWord = wordRepository.getFirstByTextIgnoreCase(word.getText());
            if (existingWord == null) {
                wordRepository.save(word);
                wordRepository.flush();
            } else {
                sentence.getWords().set(i, existingWord);
            }
        }
        return sentenceRepository.save(sentence);
    }

    @Override
    public Sentence updateSentenceSynonymFrequency(final Long sentenceId, final Long synonymId, final Integer newFrequency) {
        final Sentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new EntityNotFoundException("Sentence not found: " + sentenceId));
        final Sentence synonym = sentenceRepository.findById(synonymId)
                .orElseThrow(() -> new EntityNotFoundException("Sentence not found: " + synonymId));

        sentence.getSynonyms().put(synonym, newFrequency);
        sentenceRepository.save(sentence);
        return sentence;
    }

    @Override
    public Sentence updateSentenceResponseFrequency(final Long sentenceId, final Long responseId, final Integer newFrequency) {
        final Sentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new EntityNotFoundException("Sentence not found: " + sentenceId));
        final Sentence response = sentenceRepository.findById(responseId)
                .orElseThrow(() -> new EntityNotFoundException("Sentence not found: " + responseId));

        sentence.getResponses().put(response, newFrequency);
        sentenceRepository.save(sentence);
        return sentence;
    }

    @Override
    public List<SentenceDetectionParameters> getSentenceDetectionParameters() {
        return sentenceDetectionParametersRepository.findAllByOrderBySentenceLength();
    }

    @Override
    public void setSentenceDetectionParameters(final List<SentenceDetectionParameters> sentenceDetectionParameters) {
        for (SentenceDetectionParameters newParameters : sentenceDetectionParameters) {
            final SentenceDetectionParameters parameters = sentenceDetectionParametersRepository.findBySentenceLength(newParameters.getSentenceLength());
            parameters.setMaxNrOfExtraWords(newParameters.getMaxNrOfExtraWords());
            parameters.setMaxNrOfUnmatchedWords(newParameters.getMaxNrOfUnmatchedWords());
            parameters.setWeight(newParameters.getWeight());
            sentenceDetectionParametersRepository.save(parameters);
        }
    }

    @Override
    @Transactional
    public List<LinguisticExpression> getAllLinguisticExpressions() {
        return linguisticExpressionRepository.findAll();
    }

    @Override
    @Transactional
    public LinguisticExpression saveLinguisticExpression(final LinguisticExpression linguisticExpression) {
        return linguisticExpressionRepository.save(linguisticExpression);
    }

    @Override
    @Transactional
    public void deleteLinguisticExpression(final Long linguisticExpressionId) {
        linguisticExpressionRepository.deleteById(linguisticExpressionId);
    }

    @Override
    @Transactional
    public List<RejectingExpression> getAllRejectingExpressions() {
        return rejectingExpressionRepository.findAll();
    }

    @Override
    @Transactional
    public RejectingExpression saveRejectingExpression(final RejectingExpression rejectingExpression) {
        return rejectingExpressionRepository.save(rejectingExpression);
    }

    @Override
    @Transactional
    public void deleteRejectingExpression(final Long rejectingExpressionId) {
        rejectingExpressionRepository.deleteById(rejectingExpressionId);
    }

    @Override
    @Transactional
    public AddedDataStatus addMessagesFromFile(final MultipartFile fileWithMessages) throws IOException {
        int numberOfMessages = 0;
        int numberOfAddedMessages = 0;
//        final JSONParser jsonParser = new JSONParser(fileWithMessages.getInputStream());
//        try {
//            final JSONArray jsonMessages = (JSONArray) jsonParser.parse();
//            for (Object messageObject : jsonMessages) {
//                final JSONObject messageJson = (JSONObject) messageObject;
//                final Long fromUserId = (Long) messageJson.get("fromUserId");
//                final Long toUserId = (Long) messageJson.get("toUserId");
//                final String text = (String) messageJson.get("text");
//                chatService.addMessageAndLearn(text, fromUserId, toUserId, MessageSource.USER_USER_CONVERSATION);
//                numberOfMessages++;
//                numberOfAddedMessages++;
//            }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }

        return new AddedDataStatus(numberOfMessages, numberOfAddedMessages);
    }

    @Override
    @Transactional
    public AddedDataStatus addMessageDtos(final List<MessageDto> messageDtos) {
        int numberOfMessages = 0;
        int numberOfAddedMessages = 0;
//        for (MessageDto messageDto : messageDtos) {
//            chatService.addMessageAndLearn(messageDto.getMessage(), messageDto.getFromUserId(), messageDto.getToUserId(), MessageSource.USER_USER_CONVERSATION);
//            numberOfMessages++;
//            numberOfAddedMessages++;
//        }
        return new AddedDataStatus(numberOfMessages, numberOfAddedMessages);
    }

    @Override
    public AddedDataStatus addMessages(List<String> messages) {
        User learningUser1 = userService.getUserById(Main.USER_FOR_LEARNING_1_ID); // todo
        User learningUser2 = userService.getUserById(Main.USER_FOR_LEARNING_2_ID);
        Message previousMessage = null;

        final int numberOfMessages = messages.size();
        int numberOfAddedMessages = 0;
        for (String message : messages) {
            if (message.isEmpty()) {
                continue;
            }
            previousMessage = chatService.addMessageAndLearn(message, learningUser1, learningUser2, previousMessage, MessageSource.USER_USER_CONVERSATION);
            numberOfAddedMessages++;
            final User auxUser = learningUser1;
            learningUser1 = learningUser2;
            learningUser2 = auxUser;
        }
        return new AddedDataStatus(numberOfMessages, numberOfAddedMessages);
    }

    @Override
    public AddedDataStatus addMessagesFromCsvString(final String csvString) {
        final Map<LocalDateTime, List<String>> conversations = getConversationsFromCsvString(csvString);

        int numberOfConversations = 0;
        int numberOfAddedConversations = 0;
        //AtomicInteger numberOfAddedConversations = new AtomicInteger();
        //final Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (LocalDateTime timestamp : conversations.keySet()) {
            numberOfConversations++;
            if (!csvConversationTimestampRepository.existsByTimestamp(timestamp)) {
                //executor.execute(() -> {
                addMessages(conversations.get(timestamp));
                csvConversationTimestampRepository.save(new CsvConversationTimestamp(timestamp));
                numberOfAddedConversations++;
                //numberOfAddedConversations.getAndIncrement();
                //});
            }
        }

        return new AddedDataStatus(numberOfConversations, numberOfAddedConversations);
    }

    @Override
    public AddedDataStatus addAnswersAndQuestionsFromCsvFile(final MultipartFile csvMultipartFile) throws IOException {
        final Map<String, List<String>> questionsAndAnswers = getQuestionsAndAnswersFromCsvString(csvMultipartFile); // <question,answers>
        int numberOfQuestions = questionsAndAnswers.size();
        int numberOfAnswers = 0;

        // add replies to questions
        for (String question : questionsAndAnswers.keySet()) {
            if (question.equals("Întrebări")) {
                continue;
            }
            final Sentence questionSentence = chatbotService.getExistingSentenceOrCreateANewOne(question);
            for (String answer : questionsAndAnswers.get(question)) {
                final Sentence responseSentence = chatbotService.getExistingSentenceOrCreateANewOne(answer);
                chatbotService.addResponseAndSynonym(questionSentence, responseSentence);
                numberOfAnswers++;
            }
        }

        // add questions (without replies)
        for (String question : questionsAndAnswers.get("Întrebări")) {
            chatbotService.getExistingSentenceOrCreateANewOne(question);
        }

        return new AddedDataStatus(numberOfQuestions, numberOfAnswers);
    }

    private Map<String, List<String>> getQuestionsAndAnswersFromCsvString(final MultipartFile csvMultipartFile) throws IOException {
        final File csvFile = new File(csvMultipartFile.getOriginalFilename());
        csvFile.createNewFile();
        final FileOutputStream fileOutputStream = new FileOutputStream(csvFile);
        fileOutputStream.write(csvMultipartFile.getBytes());
        fileOutputStream.close();

        final FileReader filereader = new FileReader(csvFile);
        final Map<String, List<String>> questionsAndAnswers = new HashMap<>();
        final CSVReader csvReader = new CSVReader(filereader);

        // read header (without timestamp)
        String[] record = csvReader.readNext();
        final String[] header = new String[record.length - 1];
        for (int i = 1; i < record.length; i++) {
            final String cell = record[i];
            header[i-1] = cell;
            questionsAndAnswers.put(cell, new ArrayList<>());
        }

        // read content (without timestamp)
        while ((record = csvReader.readNext()) != null) {
            for (int i = 1; i < record.length; i++) {
                final String cell = record[i];
                final String[] lines = cell.split("\\R");
                final List<String> responses = questionsAndAnswers.get(header[i-1]);
                for (String line : lines) {
                    if (line.trim().isEmpty() || line.contains("LGBTQ")) {
                        continue;
                    }
                    line = line.replaceAll("\"", "");
                    responses.add(line);
                }
            }
        }

        return questionsAndAnswers;
    }

    @Override
    public AddedDataStatus addSentencesFromJsonFile(final MultipartFile sentencesJsonFile) throws IOException {
        int numberOfSentences = 0;
        int numberOfAddedSentences = 0;
        final JSONParser jsonParser = new JSONParser(sentencesJsonFile.getInputStream());
        try {
            final List<Object> jsonSentences = (List<Object>) jsonParser.parse();
            for (Object sentenceObject : jsonSentences) {
                final Map<String, Object> sentenceJsonMap = (Map<String, Object>) sentenceObject;
                final List<Object> texts = (List<Object>) sentenceJsonMap.get("texts");
                final SpeechType speechType = SpeechType.valueOf((String) sentenceJsonMap.get("speechType"));
                final InformationClassDto informationClassDto = InformationClassDto.valueOf((String) sentenceJsonMap.get("informationClassDto"));
                final String informationFieldNamePath = (String) sentenceJsonMap.get("informationFieldNamePath");

                for (Object textObject : texts) {
                    final String[] words = splitInWords((String) textObject);
                    final Sentence sentence = SentenceMapper.sentenceJsonToSentence(words, speechType, informationClassDto, informationFieldNamePath);
                    if (!existsByWords(sentence)) {
                        saveSentence(sentence);
                        numberOfAddedSentences++;
                    }
                    numberOfSentences++;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new AddedDataStatus(numberOfSentences, numberOfAddedSentences);
    }

    private boolean existsByWords(final Sentence sentence) {
        // check if all the text exists and, if a text exists in DB, update the word from the given sentence with the word from DB
        for (int i = 0; i < sentence.getWords().size(); i++) {
            final Word word = sentence.getWords().get(i);
            final Word existingWord = wordRepository.getFirstByTextIgnoreCase(word.getText());
            if (existingWord == null) {
                return false;
            }
            sentence.getWords().set(i, existingWord);
        }
        // all the text exists
        final List<Sentence> sentences = sentenceRepository.findAllBySpeechTypeAndInformationClassAndInformationFieldNamePath(sentence.getSpeechType(), sentence.getInformationClass(), sentence.getInformationFieldNamePath());
        for (Sentence existingSentence : sentences) {
            if (sentence.getWords().equals(existingSentence.getWords())) {
                return true;
            }
        }
        return false;
    }

    private Word getByTextIgnoreCase(final List<Word> words, final Word wordToFind) {
        for (Word word : words) {
            if (wordToFind.getText().toLowerCase().equals(word.getText().toLowerCase())) {
                return word;
            }
        }
        return null;
    }

    private boolean existsByExpressionItems(final LinguisticExpression linguisticExpression) {
        final List<LinguisticExpression> linguisticExpressions = linguisticExpressionRepository.findAllByInformationClassAndInformationFieldNamePathAndSpeechType(linguisticExpression.getInformationClass(), linguisticExpression.getInformationFieldNamePath(), linguisticExpression.getSpeechType());
        for (LinguisticExpression existingLinguisticExpression : linguisticExpressions) {
            if (linguisticExpression.getItems().equals(existingLinguisticExpression.getItems())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public AddedDataStatus addLinguisticExpressionsFromJsonFile(MultipartFile linguisticExpressionsJsonFile) throws IOException {
        int numberOfLinguisticExpressions = 0;
        int numberOfAddedLinguisticExpressions = 0;
        final JSONParser jsonParser = new JSONParser(linguisticExpressionsJsonFile.getInputStream());
        try {
            final List<Object> jsonLinguisticExpression = (List<Object>) jsonParser.parse();
            for (Object linguisticExpressionObject : jsonLinguisticExpression) {
                final Map<String, Object> linguisticExpressionJsonMap = (Map<String, Object>) linguisticExpressionObject;
                final List<List<Object>> expressionItemsLists = (List<List<Object>>) linguisticExpressionJsonMap.get("expressionItems");
                final SpeechType speechType = SpeechType.valueOf((String) linguisticExpressionJsonMap.get("speechType"));
                final InformationClassDto informationClassDto = InformationClassDto.valueOf((String) linguisticExpressionJsonMap.get("informationClassDto"));
                final String informationFieldNamePath = (String) linguisticExpressionJsonMap.get("informationFieldNamePath");

                for (List<Object> expressionItemsObjects : expressionItemsLists) {
                    final List<ExpressionItemDto> expressionItemDtos = new ArrayList<>();
                    for (Object expressionItemObject : expressionItemsObjects) {
                        final Map<String, Object> expressionItemJsonMap = (Map<String, Object>) expressionItemObject;
                        final String text = (String) expressionItemJsonMap.get("text");
                        final ItemClass itemClass = ItemClass.valueOf((String) expressionItemJsonMap.get("itemClass"));
                        expressionItemDtos.add(new ExpressionItemDto(text, itemClass));
                    }
                    final LinguisticExpression linguisticExpression = InformationMapper.linguisticExpressionDtoToLinguisticExpression(new LinguisticExpressionDto(null, expressionItemDtos, speechType, informationClassDto, informationFieldNamePath));
                    if (!existsByExpressionItems(linguisticExpression)) {
                        saveLinguisticExpression(linguisticExpression);
                        numberOfAddedLinguisticExpressions++;
                    }
                    numberOfLinguisticExpressions++;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new AddedDataStatus(numberOfLinguisticExpressions, numberOfAddedLinguisticExpressions);
    }

    @Override
    public AddedDataStatus addRejectingExpressionsFromJsonFile(MultipartFile rejectingExpressionsJsonFile) throws IOException {
        int numberOfRejectingExpressions = 0;
        int numberOfAddedRejectingExpressions = 0;
        final JSONParser jsonParser = new JSONParser(rejectingExpressionsJsonFile.getInputStream());
        try {
            final List<Object> jsonRejectingExpressions = (List<Object>) jsonParser.parse();
            numberOfRejectingExpressions = jsonRejectingExpressions.size();
            for (Object rejectingExpressionObject : jsonRejectingExpressions) {
                final Map<String, Object> rejectingExpressionJsonMap = (Map<String, Object>) rejectingExpressionObject;
                final String text = (String) rejectingExpressionJsonMap.get("text");

                if (!rejectingExpressionRepository.existsByTextIgnoreCase(text)) {
                    rejectingExpressionRepository.save(new RejectingExpression(text));
                    numberOfAddedRejectingExpressions++;
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new AddedDataStatus(numberOfRejectingExpressions, numberOfAddedRejectingExpressions);
    }

    @Override
    public ChatbotRequestType getChatbotRequestType() {
        return chatService.getChatbotRequestType();
    }

    @Override
    public void setChatbotRequestType(final ChatbotRequestType chatbotRequestType) {
        chatService.setChatbotRequestType(chatbotRequestType);
    }

    @Override
    public AddedDataStatus loadFileConversationsFromWebsite() {
        final List<String> replies = new ArrayList<>();
        try {
            final File file = new File("src/main/resources/conversations/ConversationsTriburile1.txt");
            final BufferedReader reader = new BufferedReader(new FileReader(file));

            String reply;
            while ((reply = reader.readLine()) != null) {
                if (reply.trim().isEmpty()) {
                    continue;
                }
                replies.add(reply);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addMessages(replies);
    }

    @Override
    public List<Sentence> getSentences(final Integer pageNumber, final Integer itemsPerPage) {
        return sentenceRepository.findAll(PageRequest.of(pageNumber, itemsPerPage)).getContent();
    }

    @Override
    public List<Sentence> findSentencesByWords(final String wordsAsString) {
        final List<Word> words = new ArrayList<>();
        final String[] wordsArray = splitInWords(wordsAsString);
        for (String text : wordsArray) {
            final Word word = wordRepository.getFirstByTextIgnoreCase(Word.replaceDiacritics(text));
            if (word != null) {
                words.add(word);
            }
        }

        if (words.isEmpty()) {
            return new ArrayList<>();
        }

        SentenceDetectionParameters sentenceDetectionParameters = sentenceDetectionParametersRepository.findBySentenceLength(words.size());
        if (sentenceDetectionParameters == null) {
            sentenceDetectionParameters = sentenceDetectionParametersRepository.findTop1ByOrderBySentenceLengthDesc();
            sentenceDetectionParameters.setMaxNrOfExtraWords((int) (sentenceDetectionParameters.getMaxNrOfExtraWords() * 1.5));
        }
        return chatbotService.findSimilarSentencesByWords(words,
                sentenceDetectionParameters.getMaxNrOfExtraWords(),
                sentenceDetectionParameters.getMaxNrOfUnmatchedWords(),
                sentenceDetectionParameters.getWeight());
    }

    @Override
    public List<Sentence> getSentencesById(final List<Long> sentencesId) {
        final List<Sentence> sentences = new ArrayList<>();
        for (Long id : sentencesId) {
            final Sentence sentence = sentenceRepository.getOne(id);
            if (sentence != null) {
                sentences.add(sentence);
            }
        }
        return sentences;
    }

    @Override
    public AddedDataStatus downloadConversationsFromWebsite() {
        final List<String> replies = new ArrayList<>();

        for (int page = 1; page <= 229; page++) {
            try {
                final Document doc = Jsoup.connect("https://forum.triburile.ro/index.php?threads/fara-da-sau-nu.11609/page-" + page).get();
                final Elements blockquotes = doc.select("blockquote");
                for (Element blockquote : blockquotes) {
                    final String prettyPrintedBodyFragment = Jsoup.clean(blockquote.html(), "", Whitelist.none().addTags("br", "p"), new Document.OutputSettings().prettyPrint(true));
                    final String plainText = Jsoup.clean(prettyPrintedBodyFragment, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
                    replies.addAll(Arrays.stream(plainText.replaceAll("\\?\\s+", "?\n").split("\n"))
                            .map(reply -> reply.replaceAll("&nbsp", "").replaceAll(";", "").replaceAll("Click pentru a extinde\\.+", "")
                                    .replaceAll("^.*a spus: ↑    ", "").replaceAll("\"", "").replaceAll("`", "-")
                                    .replaceAll(":&gt", ""))
                            .filter(reply -> !reply.trim().isEmpty() && reply.length() < 255 && reply.matches(".*[a-zA-Z0-9]+.*"))
                            .collect(Collectors.toList()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return writeToFile(replies);
    }

    private AddedDataStatus writeToFile(final List<String> replies) {
        final int nrOfReplies = replies.size();
        int nrOfAddedReplies = 0;
        try {
            final BufferedWriter writer = new BufferedWriter(new FileWriter(fileWithConversations));
            for (String reply : replies) {
                writer.write(reply);
                writer.newLine();
                nrOfAddedReplies++;
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new AddedDataStatus(nrOfReplies, nrOfAddedReplies);
    }

    private List<WordDto> convertWordJsonArrayToWordDtoList(final JSONArray wordsJson) {
        final List<WordDto> wordDtos = new ArrayList<>();
        for (Object wordObject : wordsJson) {
            final JSONObject wordJson = (JSONObject) wordObject;
            final String text = (String) wordJson.get("text");
            final Object synonyms = wordJson.get("synonyms");
            final WordDto wordDto = new WordDto();
            wordDto.setText(text);
            wordDtos.add(wordDto);
        }
        return wordDtos;
    }

    private Map<LocalDateTime, List<String>> getConversationsFromCsvString(final String csvString) {
        final String[] lines = csvString.split("\\R");
        final Map<LocalDateTime, List<String>> conversations = new HashMap<>();
        List<String> conversation = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            final String line = lines[i];
            if (line.replaceAll("\\s+", "").isEmpty()) {
                continue;
            }
            if (line.startsWith("\"") && line.length() > 1) {
                // new conversation
                final String[] timestampAndFirstMessage = line.split("\",\"");
                final LocalDateTime timestamp = convertStringToLocalDateTime(timestampAndFirstMessage[0].substring(1));
                final String message = timestampAndFirstMessage[1];
                conversation = new ArrayList<>();
                conversation.add(message);
                conversations.put(timestamp, conversation);
            } else {
                // inside a conversation
                if (line.endsWith("\"")) {
                    // end of conversation
                    final String message = line.substring(0, line.length() - 1);
                    conversation.add(message);
                } else {
                    conversation.add(line);
                }
            }
        }
        return conversations;
    }

    private LocalDateTime convertStringToLocalDateTime(final String timestampeString) {
        final String[] sections = timestampeString.split(" ");
        final String[] dateSection = sections[0].split("/");
        final String[] timeSection = sections[1].split(":");
        return LocalDateTime.of(Integer.parseInt(dateSection[0]), Integer.parseInt(dateSection[1]), Integer.parseInt(dateSection[2]),
                Integer.parseInt(timeSection[0]), Integer.parseInt(timeSection[1]), Integer.parseInt(timeSection[2]));
    }
}
