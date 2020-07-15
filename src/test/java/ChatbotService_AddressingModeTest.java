import app.Main;
import domain.entities.Sentence;
import domain.entities.Word;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import repositories.DexRepository;
import repositories.LinguisticExpressionRepository;
import repositories.MessageRepository;
import repositories.SentenceDetectionParametersRepository;
import repositories.SentenceRepository;
import repositories.WordRepository;
import services.api.ChatbotService;
import services.api.MessageService;
import services.impl.ChatbotServiceImpl;
import services.impl.MessageServiceImpl;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static domain.enums.AddressingMode.FORMAL;
import static domain.enums.AddressingMode.FORMAL_AND_INFORMAL;
import static domain.enums.AddressingMode.INFORMAL;

@DataJpaTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class ChatbotService_AddressingModeTest {
    @Autowired
    private WordRepository wordRepository;
    @Autowired
    private SentenceRepository sentenceRepository;
    @Autowired
    private LinguisticExpressionRepository linguisticExpressionRepository;
    @Autowired
    private SentenceDetectionParametersRepository sentenceDetectionParametersRepository;
    @Autowired
    private DexRepository dexRepository;
    @Autowired
    private MessageRepository messageRepository;
    private ChatbotService chatbotService;

    private Word wordSalut;
    private Word wordBună;
    private Word wordZiua;
    private Word wordTu;
    private Word wordDumneavoastră;
    private Word wordCum;
    private Word wordEști;
    private Word wordSunteți;
    private Word wordNormal;
    private Word questionMark;
    private Word wordÎți;
    private Word wordVă;
    private Word wordPlace;
    private Word wordȚi_ar;
    private Word wordV_ar;

    @Before
    public void initialize() {
        final MessageService messageService = new MessageServiceImpl(messageRepository);
        chatbotService = new ChatbotServiceImpl(messageService, sentenceRepository, wordRepository, sentenceDetectionParametersRepository, dexRepository, linguisticExpressionRepository);

        // add "salut", "bună" and "ziua" and set synonyms: "salut" ~= "bună"
        wordSalut = new Word("salut");
        wordSalut.setAddressingMode(INFORMAL);
        wordRepository.save(wordSalut);
        wordBună = new Word("bună");
        wordBună.setAddressingMode(FORMAL_AND_INFORMAL);
        wordRepository.save(wordBună);
        wordZiua = new Word("ziua");
        wordRepository.save(wordZiua);
        wordSalut.addSynonym(wordBună);
        wordRepository.save(wordSalut);
        wordBună.addSynonym(wordSalut);
        wordRepository.save(wordBună);
        // add "tu" and "dumneavostră" and set synonyms: "tu" ~= "dumneavostră"
        wordTu = new Word("tu");
        wordTu.setAddressingMode(INFORMAL);
        wordRepository.save(wordTu);
        wordDumneavoastră = new Word("dumneavoastră");
        wordDumneavoastră.setAddressingMode(FORMAL);
        wordRepository.save(wordDumneavoastră);
        wordTu.addSynonym(wordDumneavoastră);
        wordRepository.save(wordTu);
        wordDumneavoastră.addSynonym(wordTu);
        wordRepository.save(wordDumneavoastră);
        // add "cum", "ești", "sunteți"
        wordCum = new Word("cum");
        wordRepository.save(wordCum);
        wordEști = new Word("ești");
        wordRepository.save(wordEști);
        wordSunteți = new Word("sunteți");
        wordRepository.save(wordSunteți);
        // add "normal" and "?"
        wordNormal = new Word("normal");
        wordRepository.save(wordNormal);
        questionMark = new Word("?");
        wordRepository.save(questionMark);
        // add „îți”, „vă”, „place”, „ți-ar”, „v-ar”
        wordÎți = new Word("îți");
        wordRepository.save(wordÎți);
        wordVă = new Word("vă");
        wordRepository.save(wordVă);
        wordPlace = new Word("place");
        wordRepository.save(wordPlace);
        wordȚi_ar = new Word("ți-ar");
        wordRepository.save(wordȚi_ar);
        wordV_ar = new Word("v-ar");
        wordRepository.save(wordV_ar);

        Main.addDefaultSentenceDetectionParametersInDb(sentenceDetectionParametersRepository);
    }

    @Test
    public void testGreetings() {
        // create INFORMAL input
        List<Word> words = new ArrayList<>();
        words.add(wordSalut);
        Sentence sentence = new Sentence(words);

        // input: INFORMAL, output: FORMAL
        String text = chatbotService.translateSentenceToText(sentence, FORMAL);
        if (LocalTime.now().isBefore(ChatbotService.endOfMorning)) {
            Assert.assertEquals("bună dimineața", text);
        } else if (LocalTime.now().isAfter(ChatbotService.startOfEvening)) {
            Assert.assertEquals("bună seara", text);
        }
        else {
            Assert.assertTrue(text.equals("bună") || text.equals("bună ziua"));
        }

        // input: INFORMAL, output: INFORMAL
        text = chatbotService.translateSentenceToText(sentence, INFORMAL);
        if (LocalTime.now().isBefore(ChatbotService.endOfMorning)) {
            Assert.assertTrue(text.equals("neața") || text.equals(" bună dimi"));
        }
        else {
            Assert.assertTrue(text.equals("salut") || text.equals("bună") || text.equals("servus") || text.equals("salutare") || text.equals("hey"));
        }

        // create FORMAL input
        words = new ArrayList<>();
        words.add(wordBună);
        words.add(wordZiua);
        sentence = new Sentence(words);

        // input: FORMAL, output: FORMAL
        text = chatbotService.translateSentenceToText(sentence, FORMAL);
        if (LocalTime.now().isBefore(ChatbotService.endOfMorning)) {
            Assert.assertEquals("bună dimineața", text);
        } else if (LocalTime.now().isAfter(ChatbotService.startOfEvening)) {
            Assert.assertEquals("bună seara", text);
        }
        else {
            Assert.assertTrue(text.equals("bună") || text.equals("bună ziua"));
        }

        // input: FORMAL, output: INFORMAL
        text = chatbotService.translateSentenceToText(sentence, INFORMAL);
        if (LocalTime.now().isBefore(ChatbotService.endOfMorning)) {
            Assert.assertEquals("neața", text);
        }
        else {
            Assert.assertTrue(text.equals("salut") || text.equals("bună") || text.equals("servus") || text.equals("salutare") || text.equals("hey"));
        }
    }

    @Test
    public void testHowAreYou() {
        // create INFORMAL input
        List<Word> words = new ArrayList<>();
        words.add(wordTu);
        words.add(wordCum);
        words.add(wordEști);
        Sentence sentence = new Sentence(words);

        // input: INFORMAL, output: INFORMAL
        String text = chatbotService.translateSentenceToText(sentence, INFORMAL);
        Assert.assertEquals("tu cum ești", text);

        // input: INFORMAL, output: FORMAL
        text = chatbotService.translateSentenceToText(sentence, FORMAL);
        Assert.assertEquals("dumneavoastră cum sunteți", text);

        // create FORMAL input
        words = new ArrayList<>();
        words.add(wordDumneavoastră);
        words.add(wordCum);
        words.add(wordSunteți);
        sentence = new Sentence(words);

        // input: FORMAL, output: INFORMAL
        text = chatbotService.translateSentenceToText(sentence, INFORMAL);
        Assert.assertEquals("tu cum ești", text);

        // input: FORMAL, output: FORMAL
        text = chatbotService.translateSentenceToText(sentence, FORMAL);
        Assert.assertEquals("dumneavoastră cum sunteți", text);
    }

    @Test
    public void testEștiNormal() {
        // create INFORMAL input
        List<Word> words = new ArrayList<>();
        words.add(wordEști);
        words.add(wordNormal);
        words.add(questionMark);
        Sentence sentence = new Sentence(words);

        // input: INFORMAL, output: INFORMAL
        String text = chatbotService.translateSentenceToText(sentence, INFORMAL);
        Assert.assertEquals("ești normal?", text);

        // input: INFORMAL, output: FORMAL
        text = chatbotService.translateSentenceToText(sentence, FORMAL);
        Assert.assertEquals("sunteți normal?", text);

        // create FORMAL input
        words = new ArrayList<>();
        words.add(wordSunteți);
        words.add(wordNormal);
        words.add(questionMark);
        sentence = new Sentence(words);

        // input: FORMAL, output: INFORMAL
        text = chatbotService.translateSentenceToText(sentence, INFORMAL);
        Assert.assertEquals("ești normal?", text);

        // input: FORMAL, output: FORMAL
        text = chatbotService.translateSentenceToText(sentence, FORMAL);
        Assert.assertEquals("sunteți normal?", text);
    }

    @Test
    public void testPronouns() {
        // îți / vă
        // create INFORMAL input
        List<Word> words = new ArrayList<>();
        words.add(wordÎți);
        words.add(wordPlace);
        Sentence sentence = new Sentence(words);

        // input: INFORMAL, output: INFORMAL
        String text = chatbotService.translateSentenceToText(sentence, INFORMAL);
        Assert.assertEquals("îți place", text);

        // input: INFORMAL, output: FORMAL
        text = chatbotService.translateSentenceToText(sentence, FORMAL);
        Assert.assertEquals("vă place", text);

        // create FORMAL input
        words = new ArrayList<>();
        words.add(wordVă);
        words.add(wordPlace);
        sentence = new Sentence(words);

        // input: FORMAL, output: INFORMAL
        text = chatbotService.translateSentenceToText(sentence, INFORMAL);
        Assert.assertEquals("îți place", text);

        // input: FORMAL, output: FORMAL
        text = chatbotService.translateSentenceToText(sentence, FORMAL);
        Assert.assertEquals("vă place", text);

        // ți-ar / v-ar
        // create INFORMAL input
        words = new ArrayList<>();
        words.add(wordȚi_ar);
        words.add(wordPlace);
        sentence = new Sentence(words);

        // input: INFORMAL, output: INFORMAL
        text = chatbotService.translateSentenceToText(sentence, INFORMAL);
        Assert.assertEquals("ți-ar place", text);

        // input: INFORMAL, output: FORMAL
        text = chatbotService.translateSentenceToText(sentence, FORMAL);
        Assert.assertEquals("v-ar place", text);

        // create FORMAL input
        words = new ArrayList<>();
        words.add(wordV_ar);
        words.add(wordPlace);
        sentence = new Sentence(words);

        // input: FORMAL, output: INFORMAL
        text = chatbotService.translateSentenceToText(sentence, INFORMAL);
        Assert.assertEquals("ți-ar place", text);

        // input: FORMAL, output: FORMAL
        text = chatbotService.translateSentenceToText(sentence, FORMAL);
        Assert.assertEquals("v-ar place", text);
    }

}
