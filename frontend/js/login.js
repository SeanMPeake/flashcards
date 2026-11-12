import { signup, login } from "./api.js";
import { saveSession } from "./auth.js";
import {
    authTitleEl,
    authFormEl,
    authUsernameInputEl,
    authUsernameHintEl,
    authUsernameErrorEl,
    authPasswordInputEl,
    authPasswordHintEl,
    authPasswordErrorEl,
    authFormErrorEl,
    authSubmitButtonEl,
    authSwitchTextEl,
    authSwitchButtonEl
} from "./dom.js";

// The login screen has two modes sharing one form: "login" and "signup".
let mode = "login";

// Called after a successful login (or signup + automatic login).
let onLoggedIn = () => {};

function setMode(newMode) {
    mode = newMode;
    const isSignup = mode === "signup";

    authTitleEl.textContent = isSignup ? "Create an account" : "Log in";
    authSubmitButtonEl.textContent = isSignup ? "Create account" : "Log in";
    authSwitchTextEl.textContent = isSignup ? "Already have an account?" : "New here?";
    authSwitchButtonEl.textContent = isSignup ? "Log in" : "Create an account";
    authPasswordInputEl.autocomplete = isSignup ? "new-password" : "current-password";

    // The rules are only shown when creating an account, so login gives no
    // hint about what a valid username looks like.
    authUsernameHintEl.classList.toggle("hidden", !isSignup);
    authPasswordHintEl.classList.toggle("hidden", !isSignup);

    clearErrors();
}

function clearErrors() {
    for (const el of [authUsernameErrorEl, authPasswordErrorEl, authFormErrorEl]) {
        el.textContent = "";
        el.classList.add("hidden");
    }
}

function showError(field, message) {
    const el = field === "username" ? authUsernameErrorEl
        : field === "password" ? authPasswordErrorEl
        : authFormErrorEl;
    el.textContent = message;
    el.classList.remove("hidden");
}

// The server's messages start with the field name ("password must be 10-64
// characters"); show them as a sentence under that field.
function toSentence(message) {
    return message.charAt(0).toUpperCase() + message.slice(1) + (message.endsWith(".") ? "" : ".");
}

async function handleSubmit(event) {
    event.preventDefault();   // stay on the page instead of submitting the form
    clearErrors();

    const username = authUsernameInputEl.value.trim();
    const password = authPasswordInputEl.value;

    authSubmitButtonEl.disabled = true;
    try {
        if (mode === "signup") {
            await signup(username, password);
        }
        // After signup, log straight in with the same details.
        saveSession(await login(username, password));
        authPasswordInputEl.value = "";
        onLoggedIn();
    } catch (error) {
        showError(error.field ?? null, toSentence(error.message));
    } finally {
        authSubmitButtonEl.disabled = false;
    }
}

export function initLoginView(handleLoggedIn) {
    onLoggedIn = handleLoggedIn;
    authFormEl.addEventListener("submit", handleSubmit);
    authSwitchButtonEl.addEventListener("click", () => {
        setMode(mode === "login" ? "signup" : "login");
    });
}

// Resets the screen to login mode. An optional message (e.g. "your session has
// ended") is shown above the button.
export function resetLoginView(message) {
    setMode("login");
    authPasswordInputEl.value = "";
    if (message) {
        showError(null, message);
    }
}
