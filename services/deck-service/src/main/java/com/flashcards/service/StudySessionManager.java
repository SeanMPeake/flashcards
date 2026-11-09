package com.flashcards.service;

import com.flashcards.datastructure.StudySession;
import com.flashcards.model.Card;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

// Manages in-memory study sessions for active decks.
// A session is rebuilt from the database each time study mode is entered for a deck,
// ensuring the heap and map always reflect current card priorities.
// Session invalidation on card mutations is not implemented because the session
// is always rebuilt fresh on entry to study mode.
@Component
public class StudySessionManager {

    // java.util.HashMap is used here intentionally — this is application plumbing,
    // not a data structure under study. CardHashMap is the custom implementation used within each session.
    private final HashMap<Long, StudySession> sessions = new HashMap<>();

    // Builds a fresh session for the given deck, replacing any existing one.
    // Called when the user enters study mode for a deck.
    public StudySession createSession(Long deckId, List<Card> cards) {
        StudySession session = new StudySession(cards);
        sessions.put(deckId, session);
        
        return session;
    }

    // Returns the active session for a deck, or null if none exists.
    // Under normal flow this should always return a session since start
    // is called before any next requests.
    public StudySession getSession(Long deckId) {
        return sessions.get(deckId);
    }
}
