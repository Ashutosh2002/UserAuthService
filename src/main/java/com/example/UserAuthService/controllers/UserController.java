package com.example.UserAuthService.controllers;

import com.example.UserAuthService.dtos.*;
import com.example.UserAuthService.models.Token;
import com.example.UserAuthService.models.User;
import com.example.UserAuthService.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class UserController {

    UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }


    @PostMapping("/signup")
    public UserDto signup(@RequestBody SignUpRequestDto signUpRequestDto){
        User user = userService.signup(signUpRequestDto.getName(), signUpRequestDto.getEmail(), signUpRequestDto.getPassword());
        return UserDto.from(user);
    }

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto loginRequestDto){
        Token token = userService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        LoginResponseDto loginResponseDto = new LoginResponseDto();
        loginResponseDto.setTokenValue(token.getTokenValue());
        return loginResponseDto;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequestDto logoutRequestDto){
        return null;

    }

    @GetMapping("/validate/{tokenValue}")
    public ResponseEntity<Boolean> validateToken(@PathVariable("tokenValue") String token){
        User user = userService.validateToken(token);
        ResponseEntity<Boolean> responseEntity;

        if (user == null){
            responseEntity = new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);
        } else {
            responseEntity = new ResponseEntity<>(true, HttpStatus.OK);
        }
        return responseEntity;
    }

}
