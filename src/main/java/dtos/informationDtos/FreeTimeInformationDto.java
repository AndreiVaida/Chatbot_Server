package dtos.informationDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FreeTimeInformationDto implements InformationDto {
    private List<String> hobbies = new ArrayList<>();
    // BOOKS
    private Boolean likeReading;
    private String favouriteBook;
    private String currentReadingBook;
    // VIDEO GAMES
    private Boolean likeVideoGames;
    private String favouriteVideoGame;
    private String currentPlayedGame;
    // BOARD GAMES
    private Boolean likeBoardGames;
    private String favouriteBoardGame;
    private String currentBoardGame;
}
