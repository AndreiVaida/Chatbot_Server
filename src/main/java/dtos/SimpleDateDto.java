package dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SimpleDateDto {
    private Integer year = null;
    private Integer month = null;
    private Integer day = null;

    @Override
    public String toString() {
        return "SimpleDate{" +
                "year=" + year +
                ", month=" + month +
                ", day=" + day +
                '}';
    }
}
