package repositories;

import domain.entities.RejectingExpression;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RejectingExpressionRepository extends JpaRepository<RejectingExpression, Long> {
    boolean existsByTextIgnoreCase(String text);
}
