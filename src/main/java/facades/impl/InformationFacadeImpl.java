package facades.impl;

import dtos.LinguisticExpressionDto;
import facades.api.InformationFacade;
import mappers.InformationMapper;
import org.springframework.stereotype.Component;
import services.api.InformationService;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InformationFacadeImpl implements InformationFacade {
    private final InformationService informationService;

    public InformationFacadeImpl(InformationService informationService) {
        this.informationService = informationService;
    }

    @Override
    @Transactional
    public void addLinguisticExpression(LinguisticExpressionDto linguisticExpressionDto) {
        informationService.addLinguisticExpression(InformationMapper.linguisticExpressionDtoToLinguisticExpression(linguisticExpressionDto));
    }

    @Override
    public List<LinguisticExpressionDto> getAllLinguisticExpressions() {
        return informationService.getAllLinguisticExpressions().stream()
                .map(InformationMapper::linguisticExpressionToLinguisticExpressionDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteLinguisticExpression(final Long expressionId) {
        informationService.deleteLinguisticExpression(expressionId);
    }
}
