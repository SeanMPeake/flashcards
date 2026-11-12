package com.flashcards.service;

import com.flashcards.datastructure.StudySession;
import com.flashcards.model.Card;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Manages in-memory study sessions, one per user per deck.
// A session is rebuilt from the database each time study mode is entered for a deck,
// ensuring the heap and map always reflect current card priorities.
// Session invalidation on card mutations is not implemented because the session
// is always rebuilt fresh on entry to study mode.
@Component
public class StudySessionManager {

    // Sessions are keyed by user AND deck, so two users studying the same deck id
    // (or one user's request for another user's deck) can never share a session.
    private record SessionKey(Long userId, Long deckId) {
    }

    // ConcurrentHashMap rather than HashMap: requests from different users arrive at
    // the same time on different threads, and a plain HashMap can corrupt itself when
    // changed by several threads at once. This is application plumbing, not a data
    // structure under study; CardHashMap is the custom implementation used within each session.
    //
    // Known limits, accepted until study moves to the browser:
    // - This protects the map, not the sessions inside it. Two simultaneous requests
    //   from the SAME user on the same deck share one StudySession, whose heap is not
    //   thread-safe. Different users never share a session.
    // - Sessions are never removed, so memory grows with users x decks studied.
    //   At scale: expire idle sessions, or keep study state outside the server.
    private final Map<SessionKey, StudySession> sessions = new ConcurrentHashMap<>();

    // Builds a fresh session for this user's deck, replacing any existing one.
    // Called when the user enters study mode for a deck.
    public StudySession createSession(Long userId, Long deckId, List<Card> cards) {
        StudySession session = new StudySession(cards);
        sessions.put(new SessionKey(userId, deckId), session);

        return session;
    }

    // Returns the active session for this user's deck, or null if none exists.
    // Under normal flow this should always return a session since start
    // is called before any next requests.
    public StudySession getSession(Long userId, Long deckId) {
        return sessions.get(new SessionKey(userId, deckId));
    }
}
