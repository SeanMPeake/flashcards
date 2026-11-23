package com.flashcards.common.events;

// Names shared by the publisher and consumers of study events.
public final class StudyEvents {

    // A fanout exchange: every message published to it is copied to every queue bound
    // to it (publish/subscribe). The deck service publishes here without knowing who
    // listens; each consuming service binds its own queue.
    public static final String EXCHANGE = "study.events";

    private StudyEvents() {
    }
}
