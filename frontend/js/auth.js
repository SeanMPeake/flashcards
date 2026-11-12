// Keeps the logged-in session in the browser: the token, the user, and when the
// token expires. Stored in localStorage so the login survives tabs and browser
// restarts until the token's 24 hours run out.
//
// Production note: any script on the page can read localStorage, so a script
// injected through an XSS bug could steal the token. A production app would keep
// the token in an httpOnly cookie instead (unreadable by scripts), which then
// needs CSRF protection.

const STORAGE_KEY = "flashcards.session";

// Called with the login response: { token, expiresIn (seconds), user: { id, username } }.
export function saveSession(loginResponse) {
    const session = {
        token: loginResponse.token,
        username: loginResponse.user.username,
        expiresAt: Date.now() + loginResponse.expiresIn * 1000
    };
    try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
    } catch {
        // Storage can be unavailable (e.g. some private-browsing modes); the
        // user stays logged in until the page is reloaded.
    }
}

// Returns the saved session, or null if there is none or it has expired.
export function getSession() {
    let session = null;
    try {
        session = JSON.parse(localStorage.getItem(STORAGE_KEY));
    } catch {
        return null;
    }
    if (!session || Date.now() >= session.expiresAt) {
        clearSession();
        return null;
    }
    return session;
}

export function getToken() {
    const session = getSession();
    return session ? session.token : null;
}

export function getUsername() {
    const session = getSession();
    return session ? session.username : null;
}

export function isLoggedIn() {
    return getSession() !== null;
}

export function clearSession() {
    try {
        localStorage.removeItem(STORAGE_KEY);
    } catch {
        // Nothing to clear if storage is unavailable.
    }
}
