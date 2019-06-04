package services.impl;

import domain.entities.ExpressionItem;
import domain.entities.LinguisticExpression;
import org.springframework.stereotype.Service;
import repositories.ExpressionItemRepository;
import repositories.LinguisticExpressionRepository;
import services.api.InformationService;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class InformationServiceImpl implements InformationService {
    private final LinguisticExpressionRepository linguisticExpressionRepository;
    private final ExpressionItemRepository expressionItemRepository;

    public InformationServiceImpl(LinguisticExpressionRepository linguisticExpressionRepository, ExpressionItemRepository expressionItemRepository) {
        this.linguisticExpressionRepository = linguisticExpressionRepository;
        this.expressionItemRepository = expressionItemRepository;
    }

    @Override
    @Transactional
    public LinguisticExpression addLinguisticExpression(final LinguisticExpression linguisticExpression) {
        for (ExpressionItem expressionItem : linguisticExpression.getItems()) {
            expressionItemRepository.save(expressionItem);
        }

        return linguisticExpressionRepository.save(linguisticExpression);
    }

    @Override
    @Transactional
    public List<LinguisticExpression> getAllLinguisticExpressions() {
        return linguisticExpressionRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteLinguisticExpression(final Long expressionId) {
        if (!linguisticExpressionRepository.existsById(expressionId)) {
            throw new EntityNotFoundException("Linguistic Expression not found.");
        }
        linguisticExpressionRepository.deleteById(expressionId);
    }
}
