package controllers;

import dtos.RequestUserRegisterDto;
import dtos.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import services.api.UserService;

@RestController
@RequestMapping("/users")
public class UserController extends AbstractController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> createAccount(@RequestBody @Validated final RequestUserRegisterDto requestUserRegisterDto) {
        userService.addUser(requestUserRegisterDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable final Long userId) {
        final UserDto userDto = userService.getUserById(userId);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }
}
