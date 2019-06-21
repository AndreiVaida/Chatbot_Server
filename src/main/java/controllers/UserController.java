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
import services.api.UserPermissionService;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/users")
public class UserController extends AbstractController {
    private final UserFacade userFacade;
    private final UserPermissionService userPermissionService;

    @Autowired
    public UserController(UserFacade userFacade, UserPermissionService userPermissionService) {
        this.userFacade = userFacade;
        this.userPermissionService = userPermissionService;
    }

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody @Validated final RequestUserRegisterDto requestUserRegisterDto) {
        userFacade.addUser(requestUserRegisterDto);
        return new ResponseEntity<>(CREATED);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<?> updateUserFirstName(@PathVariable final Long userId, @RequestBody final String newFirstName) {
        if (!userPermissionService.hasUserAccess(userId)) {
            return new ResponseEntity<>(FORBIDDEN);
        }
        userFacade.updateUserFirstName(userId, newFirstName);
        return new ResponseEntity<>(OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable final Long userId) {
        if (!userPermissionService.hasUserAccess(userId) && !(userId == null || userId == 0)) {
            return new ResponseEntity<>(FORBIDDEN);
        }
        final UserDto userDto = userFacade.getUserById(userId);
        return new ResponseEntity<>(userDto, OK);
    }

    @GetMapping("/{userId}/{informationClass}")
    public ResponseEntity<?> getInformationByClass(final @PathVariable Long userId, @PathVariable final InformationClassDto informationClass) {
        if (!userPermissionService.hasUserAccess(userId)) {
            return new ResponseEntity<>(FORBIDDEN);
        }
        try {
            final InformationDto informationDto = userFacade.getInformationByClass(userId, informationClass);
            return new ResponseEntity<>(informationDto, OK);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{userId}/{informationClass}")
    public ResponseEntity<?> deleteInformationByClass(final @PathVariable Long userId, @PathVariable final InformationClassDto informationClass) {
        if (!userPermissionService.hasUserAccess(userId)) {
            return new ResponseEntity<>(FORBIDDEN);
        }
        try {
            userFacade.deleteInformationByInformationClass(userId, informationClass);
            return new ResponseEntity<>(OK);

        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | IntrospectionException | InvocationTargetException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("{userId}/profilePicture")
    public ResponseEntity<UserDto> uploadProfilePicture(@PathVariable Long userId, @RequestParam("file") MultipartFile file) throws IOException {
        if (!userPermissionService.hasUserAccess(userId)) {
            return new ResponseEntity<>(FORBIDDEN);
        }
        if (file.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        final UserDto userDto = userFacade.updateProfilePicture(userId, file);
        return new ResponseEntity<>(userDto, CREATED);
    }

    @DeleteMapping("{userId}/profilePicture")
    public ResponseEntity<UserDto> deleteProfilePicture(@PathVariable Long userId) throws IOException {
        if (!userPermissionService.hasUserAccess(userId)) {
            return new ResponseEntity<>(FORBIDDEN);
        }
        final UserDto userDto = userFacade.deleteProfilePicture(userId);
        return new ResponseEntity<>(userDto, OK);
    }
}
