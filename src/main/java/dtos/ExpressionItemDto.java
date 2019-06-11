package dtos;

import domain.enums.ItemClass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ExpressionItemDto {
    private String text;
    private ItemClass itemClass;
}
