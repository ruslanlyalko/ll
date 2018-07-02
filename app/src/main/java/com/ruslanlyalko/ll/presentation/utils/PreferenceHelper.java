package com.ruslanlyalko.ll.presentation.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.ruslanlyalko.ll.data.models.User;

public class PreferenceHelper {

    private static final String PREF_SESSION = "session_pref";
    private static final String PREF_USER = "user";
    private SharedPreferences mSessionPreferences;

    private User currentUser;

    public PreferenceHelper(final Context context) {
        mSessionPreferences = context.getSharedPreferences(PREF_SESSION, Context.MODE_PRIVATE);
        try {
            String userString = mSessionPreferences.getString(PREF_USER, "");
            currentUser = new Gson().fromJson(userString, User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public User getUser() {
        return currentUser;
    }

    public void setUser(User user) {
        currentUser = user;
        try {
            String userString = new Gson().toJson(user);
            mSessionPreferences.edit().putString(PREF_USER, userString).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void releaseData() {
        final SharedPreferences.Editor editor = mSessionPreferences.edit();
        editor.clear().apply();
    }
}
