package com.flashcards.user.service;

import com.flashcards.user.dto.request.SignupRequest;
import com.flashcards.user.dto.response.UserResponse;
import com.flashcards.user.exception.UsernameTakenException;
import com.flashcards.user.model.User;
import com.flashcards.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserResponse signup(SignupRequest request) {
        // Locale.ROOT keeps lowercasing the same on every machine, regardless of
        // the server's language settings.
        String normalized = request.getUsername().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsernameNormalized(normalized)) {
            throw new UsernameTakenException();
        }

        User user = new User(request.getUsername(), normalized,
                passwordEncoder.encode(request.getPassword()));

        try {
            User saved = userRepository.saveAndFlush(user);
            return new UserResponse(saved.getId(), saved.getUsername());
        } catch (DataIntegrityViolationException e) {
            // Two signups for the same name at the same moment can both pass the
            // check above; the database's unique constraint rejects the second one.
            throw new UsernameTakenException();
        }
    }
}
