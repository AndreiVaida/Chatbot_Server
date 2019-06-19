package controllers;

import dtos.RequestUserRegisterDto;
import dtos.UserDto;
import dtos.informationDtos.InformationClassDto;
import dtos.informationDtos.InformationDto;
import facades.api.UserFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/users")
public class UserController extends AbstractController {
    private final UserFacade userFacade;

    @Autowired
    public UserController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody @Validated final RequestUserRegisterDto requestUserRegisterDto) {
        userFacade.addUser(requestUserRegisterDto);
        return new ResponseEntity<>(CREATED);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable final Long userId) {
        final UserDto userDto = userFacade.getUserById(userId);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @GetMapping("/{userId}/{informationClass}")
    public ResponseEntity<?> getInformationByClass(final @PathVariable Long userId, @PathVariable final InformationClassDto informationClass) {
        try {
            final InformationDto informationDto = userFacade.getInformationByClass(userId, informationClass);
            return new ResponseEntity<>(informationDto, HttpStatus.OK);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{userId}/{informationClass}")
    public ResponseEntity<?> deleteInformationByInformationFieldNamePath(final @PathVariable Long userId, @PathVariable final InformationClassDto informationClass) {
        try {
            userFacade.deleteInformationByInformationClass(userId, informationClass);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | IntrospectionException | InvocationTargetException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("{userId}/profilePicture")
    public ResponseEntity<?> createGraphicResource(@PathVariable Long userId, @RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        userFacade.updateProfilePicture(userId, file);
        return new ResponseEntity<>(CREATED);
    }
}
