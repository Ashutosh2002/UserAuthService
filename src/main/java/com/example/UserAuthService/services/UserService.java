package com.example.UserAuthService.services;

import com.example.UserAuthService.models.Token;
import com.example.UserAuthService.models.User;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


public interface UserService {

    User signup(String name, String email, String password);
    Token login(String email, String password);
    void logout(Token token);
    User validateToken(String tokenValue);
}
