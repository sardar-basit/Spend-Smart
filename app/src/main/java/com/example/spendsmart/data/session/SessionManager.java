package com.example.spendsmart.data.session;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SessionManager {
    private static final String PREFS = "spendsmart_session";
    private static final String KEY_USER_ID = "current_user_id";

    private final SharedPreferences prefs;
    private final MutableLiveData<Long> userIdLive = new MutableLiveData<>();

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        long id = prefs.getLong(KEY_USER_ID, -1L);
        userIdLive.setValue(id > 0 ? id : null);
    }

    public LiveData<Long> getCurrentUserIdLive() {
        return userIdLive;
    }

    public Long getUserId() {
        long id = prefs.getLong(KEY_USER_ID, -1L);
        return id > 0 ? id : null;
    }

    public void setUserId(long userId) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply();
        userIdLive.postValue(userId);
    }

    public void clear() {
        prefs.edit().remove(KEY_USER_ID).apply();
        userIdLive.postValue(null);
    }
}
