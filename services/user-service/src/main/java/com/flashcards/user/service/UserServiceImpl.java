package com.flashcards.user.service;

import com.flashcards.user.dto.request.LoginRequest;
import com.flashcards.user.dto.request.SignupRequest;
import com.flashcards.user.dto.response.LoginResponse;
import com.flashcards.user.dto.response.UserResponse;
import com.flashcards.user.exception.InvalidCredentialsException;
import com.flashcards.user.exception.UsernameTakenException;
import com.flashcards.user.model.User;
import com.flashcards.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
public class UserServiceImpl implements UserService {

    // BCrypt only works on the first 72 bytes of a password.
    private static final int MAX_BCRYPT_BYTES = 72;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // A BCrypt hash of a throwaway value, made once at startup. Used to keep
    // login timing the same whether or not the username exists (see login).
    private final String dummyHash;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.dummyHash = passwordEncoder.encode("timing-safeguard-placeholder");
    }

    @Override
    public UserResponse signup(SignupRequest request) {
        String normalized = normalize(request.getUsername());

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
            // Only report "taken" if the name really exists now; any other constraint
            // failure is a real error and is passed on rather than disguised.
            if (userRepository.existsByUsernameNormalized(normalized)) {
                throw new UsernameTakenException();
            }
            throw e;
        }
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // BCrypt can't check passwords over 72 bytes (it throws), and every stored
        // password is at most 64 characters, so anything longer is simply wrong.
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > MAX_BCRYPT_BYTES) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByUsernameNormalized(normalize(request.getUsername()))
                .orElse(null);

        if (user == null) {
            // Run a BCrypt check anyway, so an unknown username takes as long as a
            // wrong password and response times don't reveal which names exist.
            passwordEncoder.matches(request.getPassword(), dummyHash);
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return new LoginResponse(
                jwtService.createToken(user),
                "Bearer",
                jwtService.getLifetimeSeconds(),
                new UserResponse(user.getId(), user.getUsername()));
    }

    // Trims surrounding spaces (signup never allows spaces, so this can't make two
    // names collide) and lowercases. Locale.ROOT keeps lowercasing the same on every
    // machine, regardless of the server's language settings.
    private String normalize(String username) {
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
