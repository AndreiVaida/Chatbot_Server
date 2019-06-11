package services.impl;

import app.Main;
import domain.entities.CsvConversationTimestamp;
import domain.entities.ExpressionItem;
import domain.entities.LinguisticExpression;
import domain.entities.Sentence;
import domain.entities.Word;
import domain.enums.ItemClass;
import domain.enums.MessageSource;
import domain.enums.SpeechType;
import dtos.AddedDataStatus;
import dtos.ExpressionItemDto;
import dtos.InformationClassDto;
import dtos.LinguisticExpressionDto;
import dtos.MessageDto;
import dtos.WordDto;
import mappers.InformationMapper;
import mappers.SentenceMapper;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import repositories.CsvConversationTimestampRepository;
import repositories.LinguisticExpressionRepository;
import repositories.SentenceRepository;
import repositories.WordRepository;
import services.api.AdminService;
import services.api.ChatService;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static services.impl.ChatbotServiceImpl.splitInWords;

@Service
public class AdminServiceImpl implements AdminService {
    private final SentenceRepository sentenceRepository;
    private final WordRepository wordRepository;
    private final LinguisticExpressionRepository linguisticExpressionRepository;
    private final ChatService chatService;
    private final CsvConversationTimestampRepository csvConversationTimestampRepository;

    public AdminServiceImpl(SentenceRepository sentenceRepository, WordRepository wordRepository, LinguisticExpressionRepository linguisticExpressionRepository, ChatService chatService, CsvConversationTimestampRepository csvConversationTimestampRepository) {
        this.sentenceRepository = sentenceRepository;
        this.wordRepository = wordRepository;
        this.linguisticExpressionRepository = linguisticExpressionRepository;
        this.chatService = chatService;
        this.csvConversationTimestampRepository = csvConversationTimestampRepository;
    }

    @Override
    @Transactional
    public List<Sentence> getAllSentences() {
        return sentenceRepository.findAll();
    }

    @Override
    @Transactional
    public Sentence saveSentence(final Sentence sentence) {
        for (int i = 0; i < sentence.getWords().size(); i++) {
            final Word word = sentence.getWords().get(i);
            final List<Word> existingWords = wordRepository.getByTextIgnoreCase(word.getText());
            if (existingWords == null || existingWords.isEmpty()) {
                wordRepository.save(word);
            }
            else {
                sentence.getWords().set(i, existingWords.get(0));
            }
        }
        return sentenceRepository.save(sentence);
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
    public AddedDataStatus addMessagesFromFile(final MultipartFile fileWithMessags) throws IOException {
        int numberOfMessages = 0;
        int numberOfAddedMessages = 0;
        final JSONParser jsonParser = new JSONParser(fileWithMessags.getInputStream());
        try {
            final JSONArray jsonMessages = (JSONArray) jsonParser.parse();
            for (Object messageObject : jsonMessages) {
                final JSONObject messageJson = (JSONObject) messageObject;
                final Long fromUserId = (Long) messageJson.get("fromUserId");
                final Long toUserId = (Long) messageJson.get("toUserId");
                final String text = (String) messageJson.get("text");
                chatService.addMessageAndLearn(text, fromUserId, toUserId, MessageSource.USER_USER_CONVERSATION);
                numberOfMessages++;
                numberOfAddedMessages++;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return new AddedDataStatus(numberOfMessages, numberOfAddedMessages);
    }

    @Override
    @Transactional
    public AddedDataStatus addMessageDtos(final List<MessageDto> messageDtos) {
        int numberOfMessages = 0;
        int numberOfAddedMessages = 0;
        for (MessageDto messageDto : messageDtos) {
            chatService.addMessageAndLearn(messageDto.getMessage(), messageDto.getFromUserId(), messageDto.getToUserId(), MessageSource.USER_USER_CONVERSATION);
            numberOfMessages++;
            numberOfAddedMessages++;
        }
        return new AddedDataStatus(numberOfMessages, numberOfAddedMessages);
    }

    @Override
    @Transactional
    public AddedDataStatus addMessages(List<String> messages) {
        Long user1Id = Main.USER_FOR_LEARNING_1_ID;
        Long user2Id = Main.USER_FOR_LEARNING_2_ID;

        int numberOfMessages = 0;
        int numberOfAddedMessages = 0;
        for (String message : messages) {
            numberOfMessages++;
            if (message.isEmpty()) {
                continue;
            }
            chatService.addMessageAndLearn(message, user1Id, user2Id, MessageSource.USER_USER_CONVERSATION);
            numberOfAddedMessages++;
            final Long auxId = user1Id;
            user1Id = user2Id;
            user2Id = auxId;
        }
        return new AddedDataStatus(numberOfMessages, numberOfAddedMessages);
    }

    @Override
    @Transactional
    public AddedDataStatus addMessagesFromCsvString(final String csvString) {
        final Map<LocalDateTime, List<String>> conversations = getConversationsFromCsvString(csvString);

        int numberOfConversations = 0;
        int numberOfAddedConversations = 0;
        for (LocalDateTime timestamp : conversations.keySet()) {
            numberOfConversations++;
            if (!csvConversationTimestampRepository.existsByTimestamp(timestamp)) {
                addMessages(conversations.get(timestamp));
                csvConversationTimestampRepository.save(new CsvConversationTimestamp(timestamp));
                numberOfAddedConversations++;
            }
        }

        return new AddedDataStatus(numberOfConversations, numberOfAddedConversations);
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
        // check if all the words exists and, if a words exists in DB, update the word from the given sentence with the word from DB
        for (int i = 0; i < sentence.getWords().size(); i++) {
            final Word word = sentence.getWords().get(i);
            final List<Word> existingWords = wordRepository.getByTextWithoutDiacriticsIgnoreCase(word.getTextWithoutDiacritics());
            final Word existingWord = getByTextIgnoreCase(existingWords, word);
            if (existingWord == null) {
                return false;
            }
            sentence.getWords().set(i, existingWord);
        }
        // all the words exists
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
            if (line.replaceAll("\\s+","").isEmpty()) {
                continue;
            }
            if (line.startsWith("\"")) {
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
                }
                else {
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
