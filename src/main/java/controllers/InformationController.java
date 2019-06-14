package controllers;

import dtos.admin.LinguisticExpressionDto;
import facades.api.InformationFacade;
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
public class InformationController extends AbstractController {
    private final InformationFacade informationFacade;

    public InformationController(InformationFacade informationFacade) {
        this.informationFacade = informationFacade;
    }

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody final LinguisticExpressionDto linguisticExpressionDto) {
        informationFacade.addLinguisticExpression(linguisticExpressionDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<LinguisticExpressionDto>> getAllLinguisticExpressions() {
        final List<LinguisticExpressionDto> linguisticExpressionDtos = informationFacade.getAllLinguisticExpressions();
        return new ResponseEntity<>(linguisticExpressionDtos, HttpStatus.OK);
    }

    @DeleteMapping("/{expressionId}")
    public ResponseEntity<?> deleteLinguisticExpression(@PathVariable final Long expressionId) {
        informationFacade.deleteLinguisticExpression(expressionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
