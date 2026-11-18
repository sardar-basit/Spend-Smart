package com.example.spendsmart.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.spendsmart.util.CurrencyFormatter;
import com.example.spendsmart.util.PasswordHasher;

public class PreferencesManager {
    private static final String PREFS = "spendsmart_preferences";
    private static final String KEY_CURRENCY = "currency_code";
    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_BUDGET_ALERTS = "budget_alerts_enabled";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";
    private static final String KEY_APP_LOCK = "app_lock_enabled";
    private static final String KEY_PIN_HASH = "app_lock_pin_hash";
    private static final String KEY_PIN_SALT = "app_lock_pin_salt";
    private static final String KEY_SEMESTER_MODE = "semester_mode";

    private final SharedPreferences prefs;
    private final MutableLiveData<String> currencyLive = new MutableLiveData<>();
    private final MutableLiveData<ThemeMode> themeLive = new MutableLiveData<>();
    private final MutableLiveData<Boolean> budgetAlertsLive = new MutableLiveData<>();
    private final MutableLiveData<Boolean> semesterModeLive = new MutableLiveData<>();

    public PreferencesManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String currency = prefs.getString(KEY_CURRENCY, "PKR");
        ThemeMode theme = parseTheme(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name()));
        boolean alerts = prefs.getBoolean(KEY_BUDGET_ALERTS, true);
        currencyLive.setValue(currency);
        themeLive.setValue(theme);
        budgetAlertsLive.setValue(alerts);
        semesterModeLive.setValue(prefs.getBoolean(KEY_SEMESTER_MODE, false));
        CurrencyFormatter.setActiveCurrency(currency);
    }

    public LiveData<String> getCurrencyCodeLive() {
        return currencyLive;
    }

    public LiveData<ThemeMode> getThemeModeLive() {
        return themeLive;
    }

    public LiveData<Boolean> getBudgetAlertsEnabledLive() {
        return budgetAlertsLive;
    }

    public LiveData<Boolean> getSemesterModeLive() {
        return semesterModeLive;
    }

    public String getCurrencyCode() {
        return prefs.getString(KEY_CURRENCY, "PKR");
    }

    public ThemeMode getThemeMode() {
        return parseTheme(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name()));
    }

    public boolean isBudgetAlertsEnabled() {
        return prefs.getBoolean(KEY_BUDGET_ALERTS, true);
    }

    public boolean isOnboardingDone() {
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false);
    }

    public void setOnboardingDone(boolean done) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, done).apply();
    }

    public boolean isAppLockEnabled() {
        return prefs.getBoolean(KEY_APP_LOCK, false) && hasPin();
    }

    public boolean hasPin() {
        String hash = prefs.getString(KEY_PIN_HASH, null);
        String salt = prefs.getString(KEY_PIN_SALT, null);
        return hash != null && salt != null;
    }

    public void setAppLockEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_APP_LOCK, enabled).apply();
    }

    public void setPin(String pin) {
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hash(pin, salt);
        prefs.edit()
                .putString(KEY_PIN_SALT, salt)
                .putString(KEY_PIN_HASH, hash)
                .putBoolean(KEY_APP_LOCK, true)
                .apply();
    }

    public boolean verifyPin(String pin) {
        String salt = prefs.getString(KEY_PIN_SALT, null);
        String hash = prefs.getString(KEY_PIN_HASH, null);
        if (salt == null || hash == null || pin == null) {
            return false;
        }
        return hash.equals(PasswordHasher.hash(pin, salt));
    }

    public void clearPin() {
        prefs.edit()
                .remove(KEY_PIN_HASH)
                .remove(KEY_PIN_SALT)
                .putBoolean(KEY_APP_LOCK, false)
                .apply();
    }

    public boolean isSemesterMode() {
        return prefs.getBoolean(KEY_SEMESTER_MODE, false);
    }

    public void setSemesterMode(boolean enabled) {
        prefs.edit().putBoolean(KEY_SEMESTER_MODE, enabled).apply();
        semesterModeLive.postValue(enabled);
    }

    public void setCurrencyCode(String code) {
        String normalized = "USD".equalsIgnoreCase(code) ? "USD" : "PKR";
        prefs.edit().putString(KEY_CURRENCY, normalized).apply();
        CurrencyFormatter.setActiveCurrency(normalized);
        currencyLive.postValue(normalized);
    }

    public void setThemeMode(ThemeMode mode) {
        prefs.edit().putString(KEY_THEME, mode.name()).apply();
        themeLive.postValue(mode);
    }

    public void setBudgetAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BUDGET_ALERTS, enabled).apply();
        budgetAlertsLive.postValue(enabled);
    }

    private static ThemeMode parseTheme(String value) {
        try {
            return ThemeMode.valueOf(value);
        } catch (Exception e) {
            return ThemeMode.SYSTEM;
        }
    }
}
