package com.flashcards.user.repository;

import com.flashcards.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Checked against the lowercased username so names differing only in case collide.
    boolean existsByUsernameNormalized(String usernameNormalized);

    Optional<User> findByUsernameNormalized(String usernameNormalized);
}
