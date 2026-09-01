package com.example.UserAuthService.services;

import com.example.UserAuthService.dtos.SendEmailDto;
import com.example.UserAuthService.exceptions.PasswordMismatchException;
import com.example.UserAuthService.exceptions.UserAlreadyExistException;
import com.example.UserAuthService.exceptions.UserNotFoundException;
import com.example.UserAuthService.models.Token;
import com.example.UserAuthService.models.User;
import com.example.UserAuthService.repositories.TokenRepository;
import com.example.UserAuthService.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecretKey SECRET_KEY;
    private final ObjectMapper objectMapper;
    private KafkaTemplate<String, String> kafkaTemplate;

//    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);

    private static final long EXPIRATION_TIME = 10 * 60 * 60 * 1000; // 10 hours

    public UserServiceImpl(UserRepository userRepository,
                           TokenRepository tokenRepository,
                           BCryptPasswordEncoder passwordEncoder,
                           SecretKey SECRET_KEY,
                            ObjectMapper objectMapper,
                           KafkaTemplate<String, String> kafkaTemplate) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.SECRET_KEY = SECRET_KEY;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;

    }

    @Override
    public User signup(String name, String email, String password) {

//        if (userRepository.findByEmail(email).isPresent()) {
//            throw new UserAlreadyExistException("User with email " + email + " already exists.");
//        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        user = userRepository.save(user);

        System.out.println("User registered successfully: " + email);
        SendEmailDto sendEmailDto = new SendEmailDto();
        sendEmailDto.setFrom("ashutoshswarup2002@gmail.com");
        sendEmailDto.setSubject("User Registration Test");
        sendEmailDto.setBody("Hello " + name + ",\n\nThank you for registering with our service. Your account has been successfully created.\n\nBest regards,\nThe Team");
        sendEmailDto.setTo(email);

        String sendEmailDtoString = "";
        try {
            sendEmailDtoString = objectMapper.writeValueAsString(sendEmailDto);
        } catch (Exception e) {
            System.out.println("Error serializing SendEmailDto: " + e.getMessage());
        }

        //        Producing a message to Kafka topic after successful user registration
        kafkaTemplate.send("sendEmail", sendEmailDtoString);


        return user;
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

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());

        String jsonString = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SECRET_KEY, io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();


//        This is for non JWT token generation and storage in DB, not relevant after JWT implementation, we can just return the JWT string

        Token token = new Token();
        token.setUser(user);
//        token.setTokenValue(RandomStringUtils.randomAlphanumeric(128));
        token.setTokenValue(jsonString);


//        Alternative way to set expiry date
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.DATE, 30);
//        Date expiryTme = calendar.getTime();

        token.setExpiryAt(expiryDate);
//        return tokenRepository.save(token);
        return token;
    }

    @Override
    public void logout(Token token) {

    }


    public User validateNonJwtTokenInDb(String tokenValue) {
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

    @Override
    public User validateToken(String tokenValue) {
        if (tokenValue == null || tokenValue.isEmpty()) {
            return null;
        }

        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(tokenValue)
                    .getBody();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("Token expired: " + e.getMessage());
            return null;
        } catch (io.jsonwebtoken.JwtException e) {
            System.out.println("Invalid token: " + e.getMessage());
            return null;
        }

        String email = claims.getSubject();
        if (email == null || email.isEmpty()) {
            System.out.println("Invalid token: email is missing");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty() || userOptional.get().isDeleted()) {
            System.out.println("User not found for email: " + email);
            return null;
        }

        return userOptional.get();
    }
}
