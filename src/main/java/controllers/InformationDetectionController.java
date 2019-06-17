package controllers;

import dtos.admin.LinguisticExpressionDto;
import facades.api.InformationDetectionFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/expression")
public class InformationDetectionController extends AbstractController { // NOT USED ??
    private final InformationDetectionFacade informationDetectionFacade;

    public InformationDetectionController(InformationDetectionFacade informationDetectionFacade) {
        this.informationDetectionFacade = informationDetectionFacade;
    }

    @PostMapping
    public ResponseEntity<?> addLinguisticExpression(@RequestBody final LinguisticExpressionDto linguisticExpressionDto) {
        informationDetectionFacade.addLinguisticExpression(linguisticExpressionDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<LinguisticExpressionDto>> getAllLinguisticExpressions() {
        final List<LinguisticExpressionDto> linguisticExpressionDtos = informationDetectionFacade.getAllLinguisticExpressions();
        return new ResponseEntity<>(linguisticExpressionDtos, HttpStatus.OK);
    }

    @DeleteMapping("/{expressionId}")
    public ResponseEntity<?> deleteLinguisticExpression(@PathVariable final Long expressionId) {
        informationDetectionFacade.deleteLinguisticExpression(expressionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
