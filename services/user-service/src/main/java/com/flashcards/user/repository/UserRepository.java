package com.flashcards.user.repository;

import com.flashcards.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    // Checked against the lowercased username so names differing only in case collide.
    boolean existsByUsernameNormalized(String usernameNormalized);
}
