package controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

@Controller
public class AbstractController {

    @ExceptionHandler
    public ResponseEntity<?> handleException(final Exception e) {
        if (e instanceof EntityNotFoundException) {
            return new ResponseEntity<>(e, HttpStatus.NOT_FOUND);
        }
        if (e instanceof EntityExistsException) {
            return new ResponseEntity<>(e, HttpStatus.CONFLICT);
        }
        e.printStackTrace();
        return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
