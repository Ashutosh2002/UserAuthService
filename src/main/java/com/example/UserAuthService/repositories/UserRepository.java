package com.example.UserAuthService.repositories;

import com.example.UserAuthService.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User save(User user);
    Optional<User> findByEmail(String email);
}
