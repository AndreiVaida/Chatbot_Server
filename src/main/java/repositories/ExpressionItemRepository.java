package repositories;

import domain.entities.ExpressionItem;
import domain.enums.ItemClass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpressionItemRepository extends JpaRepository<ExpressionItem, Long> {
}
