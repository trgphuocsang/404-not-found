package com.nhom404.bachhoa.admin.session;

import android.content.Context;
import android.content.SharedPreferences;
public class SessionManager {
    private static final String PREF_NAME = "_session";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_ROLE = "role";
    private final SharedPreferences preferences;

    public SessionManager(Context context){
        preferences = context.getApplicationContext()
                .getSharedPreferences(
                        PREF_NAME,
                        Context.MODE_PRIVATE
                );
    }

    public void saveSession(String accessToken, String role) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Access token is required");
        }

        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required");
        }

        preferences.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_ROLE, role)
                .apply();
    }
    public String getAccessToken() {
        return preferences.getString(KEY_ACCESS_TOKEN, null);
    }
    public String getRole() {
        return preferences.getString(KEY_ROLE, null);
    }
    public boolean hasSession() {
        String token = getAccessToken();
        String role = getRole();

        return token != null
                && !token.trim().isEmpty()
                && role != null
                && !role.trim().isEmpty();
    }

    public void clearSession() {
        preferences.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_ROLE)
                .apply();
    }
}
