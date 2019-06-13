package repositories;

import domain.entities.Word;
import domain.enums.AddressingMode;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Repository
public class DexRepository {
    private static Connection connection;

    public DexRepository() {
        connectToDatabase();
    }

    private void connectToDatabase() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/DEX", "root", "parola");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnectedToDex() {
        return connection != null;
    }

    /**
     * @param word if is not a verb in the second person, won't process it and return the word
     * @param addressingMode should be FORMAL or INFORMAL
     * @return the appropriate word with the given addressing mode at same verbal mode if the given word is a verb in the second person;
     * otherwise return <null>
     * (returnează un verb la persoana a II-a și care are aproximativ același mod verbal)
     * Nu returnează verbe la modurile imperfect, perfect simplu sau mai mult ca perfect
     */
    @SuppressWarnings("Duplicates")
    public String getWordWithAddressingModeFromDex(final Word word, final AddressingMode addressingMode, final boolean isImperative) {
        if (connection == null) {
            return null;
        }

        try {
            // find the word in DEX
            final Statement statement1 = connection.createStatement();
            final ResultSet lexemes = statement1.executeQuery("select * from inflectedform where formNoAccent like \"" + word.getTextWithDiacritics().toLowerCase() + "\"");
            if (!lexemes.isBeforeFirst()) {
                return null; // word not found in DEX
            }
            while (lexemes.next()) {
                final int lexemeInflectionId = lexemes.getInt("inflectionId");

                // check if the word is a verb in the second person
                final Statement statement2 = connection.createStatement();
                final ResultSet lexemeInflection = statement2.executeQuery("select * from inflection where id = " + lexemeInflectionId);
                if (!lexemeInflection.next()) {
                    // corrupted/incomplete database
                    continue;
                }
                final String lexemeInflectionDescription = lexemeInflection.getString("description").toLowerCase();
                if (!lexemeInflectionDescription.contains("verb") || !lexemeInflectionDescription.contains(" ii-a")) {
                    // the word is NOT a verb in the second person
                    continue;
                }
                final boolean lexemeIsImperative = lexemeInflectionDescription.contains("imperativ");
                if (isImperative != lexemeIsImperative) {
                    continue;
                }
                // the lexeme is ok
                final String lexemeVerbalMode = lexemeInflectionDescription.split(", ")[1];
//                final String lexeme = lexemes.getString("formNoAccent");
//                if (lexeme.equals(word.getTextWithDiacritics())) {
//                    return lexeme;
//                }

                // get all conjugations of the word
                final int lexemeId = lexemes.getInt("lexemeId");
                final Statement statement3 = connection.createStatement();
                final ResultSet conjugations = statement3.executeQuery("select * from inflectedform where lexemeId = " + lexemeId);
                while (conjugations.next()) {
                    final int conjugationInflectionId = conjugations.getInt("inflectionId");
                    final Statement statement4 = connection.createStatement();
                    final ResultSet conjugationInflection = statement4.executeQuery("select * from inflection where id = " + conjugationInflectionId);
                    if (!conjugationInflection.next()) {
                        // corrupted/incomplete database
                        continue;
                    }

                    // check if the conjugate word is a verb in the second person at same verbal mode
                    final String conjugationInflectionDescription = conjugationInflection.getString("description").toLowerCase();
                    if (!conjugationInflectionDescription.contains("verb") || !conjugationInflectionDescription.contains(" ii-a")) {
                        // the conjugate word is NOT a verb in the second person => check the next one
                        continue;
                    }
                    if (conjugationInflectionDescription.contains("perfect")) {
                        // ignore verbs at the tenses: imperfect, perfect simplu, mult ca perfect
                        continue;
                    }
                    final boolean conjugationIsImperative = conjugationInflectionDescription.contains("imperativ");
                    if (isImperative != conjugationIsImperative) {
                        // the conjugate word is NOT at same verbal mode => check the next one
                        continue;
                    }

                    // check if the word is recommended
                    final boolean conjugationIsRecommended = conjugations.getInt("recommended") == 1;
                    if (!conjugationIsRecommended) {
                        continue;
                    }

                    // check verbal mode
                    final String conjugateVerbalMode = conjugationInflectionDescription.split(", ")[1];
                    if (!lexemeVerbalMode.equals(conjugateVerbalMode)) {
                        continue;
                    }

                    // check addressing mode of the conjugate word
                    switch (addressingMode) {
                        case FORMAL: {
                            if (conjugationInflectionDescription.contains("plural")) {
                                return conjugations.getString("formNoAccent");
                            } // else continue
                        }
                        case INFORMAL: {
                            if (conjugationInflectionDescription.contains("singular")) {
                                return conjugations.getString("formNoAccent");
                            } // else continue
                        }
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        // not found any conjugate words verbs in the second person
        return null;
    }
}
