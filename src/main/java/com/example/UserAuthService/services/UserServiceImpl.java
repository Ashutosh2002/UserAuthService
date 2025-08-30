package com.example.UserAuthService.services;

import com.example.UserAuthService.exceptions.PasswordMismatchException;
import com.example.UserAuthService.exceptions.UserAlreadyExistException;
import com.example.UserAuthService.exceptions.UserNotFoundException;
import com.example.UserAuthService.models.Token;
import com.example.UserAuthService.models.User;
import com.example.UserAuthService.repositories.TokenRepository;
import com.example.UserAuthService.repositories.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           TokenRepository tokenRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User signup(String name, String email, String password) {

        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistException("User with email " + email + " already exists.");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }

    @Override
    public Token login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User with email " + email + " does not exist.");
        }

        User user = userOptional.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new PasswordMismatchException("Incorrect password.");
        }

        Token token = new Token();
        token.setUser(user);
        token.setTokenValue(RandomStringUtils.randomAlphanumeric(128));

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 30);
        Date expiryTme = calendar.getTime();

        token.setExpiryAt(expiryTme);
        return tokenRepository.save(token);
    }

    @Override
    public void logout(Token token) {

    }

    @Override
    public User validateToken(String tokenValue) {
//        steps to validate token
//        1. check if token exists
//        2. check if token is not deleted
//        3. check if token is not expired
        Optional<Token> tokenOptional = tokenRepository.findByTokenValueAndDeletedAndExpiryAtAfter(tokenValue,false,new Date());
        if (tokenOptional.isEmpty()) {
            return null;
        }

        Token token = tokenOptional.get();
        return token.getUser();
    }
}
