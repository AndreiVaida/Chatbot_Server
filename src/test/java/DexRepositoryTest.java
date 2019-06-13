import domain.entities.Word;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import repositories.DexRepository;

import static domain.enums.AddressingMode.FORMAL;
import static domain.enums.AddressingMode.INFORMAL;

@DataJpaTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {TestConfiguration.class})
public class DexRepositoryTest {
    @Autowired
    private DexRepository dexRepository;

    @Test
    public void test1_aMânca() {
        Assert.assertTrue(dexRepository.isConnectedToDex());

        // not imperative
        String result = dexRepository.getWordWithAddressingModeFromDex(new Word("mănânci"), INFORMAL, false);
        Assert.assertEquals("mănânci", result);

        result = dexRepository.getWordWithAddressingModeFromDex(new Word("mănânci"), FORMAL, false);
        Assert.assertEquals("mâncați", result);

        result = dexRepository.getWordWithAddressingModeFromDex(new Word("mâncați"), INFORMAL, false);
        Assert.assertEquals("mănânci", result);

        result = dexRepository.getWordWithAddressingModeFromDex(new Word("mâncați"), FORMAL, false);
        Assert.assertEquals("mâncați", result);

        // imperative
        result = dexRepository.getWordWithAddressingModeFromDex(new Word("mănâncă"), INFORMAL, true);
        Assert.assertEquals("mănâncă", result);

        result = dexRepository.getWordWithAddressingModeFromDex(new Word("mănâncă"), FORMAL, true);
        Assert.assertEquals("mâncați", result);

        result = dexRepository.getWordWithAddressingModeFromDex(new Word("mâncați"), INFORMAL, true);
        Assert.assertEquals("mănâncă", result);

        result = dexRepository.getWordWithAddressingModeFromDex(new Word("mâncați"), FORMAL, true);
        Assert.assertEquals("mâncați", result);
    }

    @Test
    public void test1_aFi() {
        Assert.assertTrue(dexRepository.isConnectedToDex());

        // not imperative
        String result = dexRepository.getWordWithAddressingModeFromDex(new Word("ești"), INFORMAL, false);
        Assert.assertEquals("ești", result);

        result = dexRepository.getWordWithAddressingModeFromDex(new Word("ești"), FORMAL, false);
        Assert.assertEquals("sunteți", result);

        result = dexRepository.getWordWithAddressingModeFromDex(new Word("sunteți"), INFORMAL, false);
        Assert.assertEquals("ești", result);

        result = dexRepository.getWordWithAddressingModeFromDex(new Word("sunteți"), FORMAL, false);
        Assert.assertEquals("sunteți", result);

        // imperative
        result = dexRepository.getWordWithAddressingModeFromDex(new Word("fii"), INFORMAL, true);
        Assert.assertEquals("fii", result);

        result = dexRepository.getWordWithAddressingModeFromDex(new Word("fii"), FORMAL, true);
        Assert.assertEquals("fiți", result);

        result = dexRepository.getWordWithAddressingModeFromDex(new Word("fiți"), INFORMAL, true);
        Assert.assertEquals("fii", result);

        result = dexRepository.getWordWithAddressingModeFromDex(new Word("fiți"), FORMAL, true);
        Assert.assertEquals("fiți", result);
    }

    @Test
    public void test1_aCânta() {
        Assert.assertTrue(dexRepository.isConnectedToDex());

        // not imperative
        String result = dexRepository.getWordWithAddressingModeFromDex(new Word("cânți"), INFORMAL, false);
        Assert.assertEquals("cânți", result);

        result = dexRepository.getWordWithAddressingModeFromDex(new Word("cânți"), FORMAL, false);
        Assert.assertEquals("cântați", result);

        result = dexRepository.getWordWithAddressingModeFromDex(new Word("cântați"), INFORMAL, false);
        Assert.assertEquals("cânți", result);

        result = dexRepository.getWordWithAddressingModeFromDex(new Word("cântați"), FORMAL, false);
        Assert.assertEquals("cântați", result);

        // imperative
        result = dexRepository.getWordWithAddressingModeFromDex(new Word("cântă"), INFORMAL, true);
        Assert.assertEquals("cântă", result);

        result = dexRepository.getWordWithAddressingModeFromDex(new Word("cântă"), FORMAL, true);
        Assert.assertEquals("cântați", result);

        result = dexRepository.getWordWithAddressingModeFromDex(new Word("cântați"), INFORMAL, true);
        Assert.assertEquals("cântă", result);

        result = dexRepository.getWordWithAddressingModeFromDex(new Word("cântați"), FORMAL, true);
        Assert.assertEquals("cântați", result);
    }
}
