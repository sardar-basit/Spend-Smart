package com.example.spendsmart.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.spendsmart.data.local.dao.UserDao;
import com.example.spendsmart.data.local.entity.User;
import com.example.spendsmart.data.session.SessionManager;
import com.example.spendsmart.domain.model.AuthResult;
import com.example.spendsmart.domain.model.AuthState;
import com.example.spendsmart.domain.model.UserProfile;
import com.example.spendsmart.util.PasswordHasher;

import java.util.concurrent.ExecutorService;

public class AuthRepository {
    private final UserDao userDao;
    private final SessionManager sessionManager;
    private final ExecutorService executor;
    private final MediatorLiveData<AuthState> authState = new MediatorLiveData<>();

    public AuthRepository(UserDao userDao, SessionManager sessionManager, ExecutorService executor) {
        this.userDao = userDao;
        this.sessionManager = sessionManager;
        this.executor = executor;
        authState.setValue(AuthState.loading());
        authState.addSource(sessionManager.getCurrentUserIdLive(), userId -> refreshAuthState(userId));
    }

    public LiveData<AuthState> observeAuthState() {
        return authState;
    }

    private void refreshAuthState(Long userId) {
        if (userId == null) {
            authState.postValue(AuthState.loggedOut());
            return;
        }
        executor.execute(() -> {
            User user = userDao.getById(userId);
            if (user == null) {
                sessionManager.clear();
                authState.postValue(AuthState.loggedOut());
            } else {
                authState.postValue(AuthState.loggedIn(toProfile(user)));
            }
        });
    }

    public void signUp(String name, String email, String password, MutableLiveData<AuthResult> result) {
        executor.execute(() -> {
            String trimmedName = name == null ? "" : name.trim();
            String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
            if (trimmedName.isEmpty()) {
                result.postValue(AuthResult.error("Enter your name"));
                return;
            }
            if (!isValidEmail(normalizedEmail)) {
                result.postValue(AuthResult.error("Enter a valid email"));
                return;
            }
            if (password == null || password.length() < 6) {
                result.postValue(AuthResult.error("Password must be at least 6 characters"));
                return;
            }
            if (userDao.countByEmail(normalizedEmail) > 0) {
                result.postValue(AuthResult.error("An account with this email already exists"));
                return;
            }
            String salt = PasswordHasher.generateSalt();
            String hash = PasswordHasher.hash(password, salt);
            User user = new User(trimmedName, normalizedEmail, hash, salt);
            long newId = userDao.insert(user);
            user.id = newId;
            sessionManager.setUserId(newId);
            result.postValue(AuthResult.success(toProfile(user)));
        });
    }

    public void login(String email, String password, MutableLiveData<AuthResult> result) {
        executor.execute(() -> {
            String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
            if (!isValidEmail(normalizedEmail) || password == null || password.isEmpty()) {
                result.postValue(AuthResult.error("Enter a valid email and password"));
                return;
            }
            User user = userDao.getByEmail(normalizedEmail);
            if (user == null) {
                result.postValue(AuthResult.error("No account found for this email"));
                return;
            }
            if (!PasswordHasher.matches(password, user.salt, user.passwordHash)) {
                result.postValue(AuthResult.error("Incorrect password"));
                return;
            }
            sessionManager.setUserId(user.id);
            result.postValue(AuthResult.success(toProfile(user)));
        });
    }

    public void logout() {
        sessionManager.clear();
    }

    public void updateName(String name, MutableLiveData<AuthResult> result) {
        executor.execute(() -> {
            Long userId = sessionManager.getUserId();
            if (userId == null) {
                result.postValue(AuthResult.error("Not logged in"));
                return;
            }
            String trimmed = name == null ? "" : name.trim();
            if (trimmed.isEmpty()) {
                result.postValue(AuthResult.error("Enter your name"));
                return;
            }
            User user = userDao.getById(userId);
            if (user == null) {
                result.postValue(AuthResult.error("User not found"));
                return;
            }
            user.name = trimmed;
            userDao.update(user);
            authState.postValue(AuthState.loggedIn(toProfile(user)));
            result.postValue(AuthResult.success(toProfile(user)));
        });
    }

    public void changePassword(String currentPassword, String newPassword,
                               MutableLiveData<AuthResult> result) {
        executor.execute(() -> {
            Long userId = sessionManager.getUserId();
            if (userId == null) {
                result.postValue(AuthResult.error("Not logged in"));
                return;
            }
            User user = userDao.getById(userId);
            if (user == null) {
                result.postValue(AuthResult.error("User not found"));
                return;
            }
            if (!PasswordHasher.matches(currentPassword, user.salt, user.passwordHash)) {
                result.postValue(AuthResult.error("Current password is incorrect"));
                return;
            }
            if (newPassword == null || newPassword.length() < 6) {
                result.postValue(AuthResult.error("New password must be at least 6 characters"));
                return;
            }
            String salt = PasswordHasher.generateSalt();
            String hash = PasswordHasher.hash(newPassword, salt);
            user.salt = salt;
            user.passwordHash = hash;
            userDao.update(user);
            result.postValue(AuthResult.success(toProfile(user)));
        });
    }

    private static boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.length() >= 5;
    }

    private static UserProfile toProfile(User user) {
        return new UserProfile(user.id, user.name, user.email);
    }
}
