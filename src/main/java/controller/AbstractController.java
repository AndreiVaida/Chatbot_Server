package controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.persistence.EntityNotFoundException;
import javax.security.auth.login.FailedLoginException;

@Controller
public class AbstractController {

    @ExceptionHandler
    public ResponseEntity<?> handleException(final Exception e) {
        if (e instanceof EntityNotFoundException) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (e instanceof FailedLoginException) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
