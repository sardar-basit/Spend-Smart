package com.example.spendsmart.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.example.spendsmart.data.repository.AuthRepository;
import com.example.spendsmart.domain.model.AuthResult;
import com.example.spendsmart.domain.model.AuthState;

public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<AuthResult> authResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LiveData<AuthState> getAuthState() {
        return authRepository.observeAuthState();
    }

    public LiveData<AuthResult> getAuthResult() {
        return authResult;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void login(String email, String password) {
        loading.setValue(true);
        errorMessage.setValue(null);
        MutableLiveData<AuthResult> result = new MutableLiveData<>();
        Observer<AuthResult> observer = new Observer<AuthResult>() {
            @Override
            public void onChanged(AuthResult r) {
                result.removeObserver(this);
                loading.setValue(false);
                if (r != null && !r.success) {
                    errorMessage.setValue(r.errorMessage);
                }
                authResult.setValue(r);
            }
        };
        result.observeForever(observer);
        authRepository.login(email, password, result);
    }

    public void signUp(String name, String email, String password, String confirmPassword) {
        if (password == null || !password.equals(confirmPassword)) {
            errorMessage.setValue("Passwords do not match");
            return;
        }
        loading.setValue(true);
        errorMessage.setValue(null);
        MutableLiveData<AuthResult> result = new MutableLiveData<>();
        Observer<AuthResult> observer = new Observer<AuthResult>() {
            @Override
            public void onChanged(AuthResult r) {
                result.removeObserver(this);
                loading.setValue(false);
                if (r != null && !r.success) {
                    errorMessage.setValue(r.errorMessage);
                }
                authResult.setValue(r);
            }
        };
        result.observeForever(observer);
        authRepository.signUp(name, email, password, result);
    }

    public void logout() {
        authRepository.logout();
    }

    public void clearError() {
        errorMessage.setValue(null);
    }
}
