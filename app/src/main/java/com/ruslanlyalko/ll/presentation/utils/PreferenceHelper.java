package com.ruslanlyalko.ll.presentation.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.ruslanlyalko.ll.data.models.User;

public class PreferenceHelper {

    private static final String PREF_USER_DATA = "user_data_pref";
    private static final String PREF_USER = "user";
    private static PreferenceHelper mInstance;
    private SharedPreferences mUserPreferences;
    private User currentUser;

    private PreferenceHelper(final Context context) {
        mUserPreferences = context.getSharedPreferences(PREF_USER_DATA, Context.MODE_PRIVATE);
        try {
            String userString = mUserPreferences.getString(PREF_USER, "");
            currentUser = new Gson().fromJson(userString, User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PreferenceHelper newInstance(Context context) {
        if (mInstance == null)
            mInstance = new PreferenceHelper(context);
        return mInstance;
    }

    public User getUser() {
        return currentUser;
    }

    public void setUser(User user) {
        currentUser = user;
        try {
            String userString = new Gson().toJson(user);
            mUserPreferences.edit().putString(PREF_USER, userString).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void releaseData() {
        final SharedPreferences.Editor editor = mUserPreferences.edit();
        editor.clear().apply();
    }
}
