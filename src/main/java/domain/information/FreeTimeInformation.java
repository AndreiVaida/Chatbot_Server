package domain.information;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

// Lombok
@NoArgsConstructor
@AllArgsConstructor
@Data
// Hibernate
@Entity
@Table(name = "FREE_TIME_INFORMATION")
public class FreeTimeInformation implements Information {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // HOBBIES
    @Column(name = "HOBBIES")
    @ElementCollection
    private List<String> hobbies = new ArrayList<>();

    // BOOKS
    @Column(name = "LIKE_READING")
    private Boolean likeReading;

    @Column(name = "FAVOURITE_BOOK")
    private String favouriteBook;

    @Column(name = "CURRENT_READING_BOOK")
    private String currentReadingBook;

    // VIDEO GAMES
    @Column(name = "LIKE_VIDEO_GAMES")
    private Boolean likeVideoGames;

    @Column(name = "FAVOURITE_VIDEO_GAME")
    private String favouriteVideoGame;

    @Column(name = "CURRENT_PLAYED_VIDEO_GAME")
    private String currentPlayedGame;

    // BOARD GAMES
    @Column(name = "LIKE_BOARD_GAMES")
    private Boolean likeBoardGames;

    @Column(name = "FAVOURITE_BOARD_GAME")
    private String favouriteBoardGame;

    @Column(name = "CURRENT_PLAYED_BOARD_GAME")
    private String currentBoardGame;

    @Override
    public List<String> getFieldNamesInImportanceOrder() {
        final List<String> fieldNamesInImportanceOrder = new ArrayList<>();
        fieldNamesInImportanceOrder.add("hobbies");
        fieldNamesInImportanceOrder.add("likeReading");
        fieldNamesInImportanceOrder.add("favouriteBook");
        fieldNamesInImportanceOrder.add("currentReadingBook");
        fieldNamesInImportanceOrder.add("likeVideoGames");
        fieldNamesInImportanceOrder.add("favouriteVideoGame");
        fieldNamesInImportanceOrder.add("currentPlayedGame");
        fieldNamesInImportanceOrder.add("likeBoardGames");
        fieldNamesInImportanceOrder.add("favouriteBoardGame");
        fieldNamesInImportanceOrder.add("currentBoardGame");
        return fieldNamesInImportanceOrder;
    }
}
