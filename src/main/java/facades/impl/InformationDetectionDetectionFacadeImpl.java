package facades.impl;

import dtos.admin.LinguisticExpressionDto;
import facades.api.InformationDetectionFacade;
import mappers.InformationMapper;
import org.springframework.stereotype.Component;
import services.api.InformationDetectionService;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InformationDetectionDetectionFacadeImpl implements InformationDetectionFacade {
    private final InformationDetectionService informationDetectionService;

    public InformationDetectionDetectionFacadeImpl(InformationDetectionService informationDetectionService) {
        this.informationDetectionService = informationDetectionService;
    }

    @Override
    @Transactional
    public void addLinguisticExpression(LinguisticExpressionDto linguisticExpressionDto) {
        informationDetectionService.addLinguisticExpression(InformationMapper.linguisticExpressionDtoToLinguisticExpression(linguisticExpressionDto));
    }

    @Override
    public List<LinguisticExpressionDto> getAllLinguisticExpressions() {
        return informationDetectionService.getAllLinguisticExpressions().stream()
                .map(InformationMapper::linguisticExpressionToLinguisticExpressionDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteLinguisticExpression(final Long expressionId) {
        informationDetectionService.deleteLinguisticExpression(expressionId);
    }
}
