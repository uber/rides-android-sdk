package com.uber.sdk.android.rides.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.Set;

public class AccessTokenPreferences {

    private static final String ACCESS_TOKEN_DEFAULT_KEY = "defaultAccessToken";
    private static final String ACCESS_TOKEN_SHARED_PREFERENCES = "uberSdkAccessTokenConfig";
    private static final String DATE_KEY_SUFFIX = "_date";
    private static final String TOKEN_KEY_SUFFIX = "_token";
    private static final String SCOPES_KEY_SUFFIX = "_scopes";

    @NonNull private SharedPreferences mSharedPreferences;

    public AccessTokenPreferences(@NonNull Context context) {
        mSharedPreferences = context.getSharedPreferences(ACCESS_TOKEN_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    public void clear() {
        mSharedPreferences.edit().clear().apply();
    }

    @Nullable
    public AccessToken getAccessToken() {
        return getAccessToken(ACCESS_TOKEN_DEFAULT_KEY);
    }

    @Nullable
    public AccessToken getAccessToken(@NonNull String key) {
        long expirationTime = mSharedPreferences.getLong(key + DATE_KEY_SUFFIX, -1);
        String token = mSharedPreferences.getString(key + TOKEN_KEY_SUFFIX, null);
        Set<String> scopes = mSharedPreferences.getStringSet(key + SCOPES_KEY_SUFFIX, null);

        if (expirationTime == -1 || token == null || scopes == null) {
            // Return null, if we can't parse it this token is considered unsaved.
            return null;
        }

        return new AccessToken(new Date(expirationTime), AuthUtils.stringSetToScopeCollection(scopes), token);
    }

    public void removeAccessToken() {
        removeAccessToken(ACCESS_TOKEN_DEFAULT_KEY);
    }

    public void removeAccessToken(@NonNull String key) {
        mSharedPreferences.edit().remove(key + DATE_KEY_SUFFIX).apply();
        mSharedPreferences.edit().remove(key + TOKEN_KEY_SUFFIX).apply();
        mSharedPreferences.edit().remove(key + SCOPES_KEY_SUFFIX).apply();
    }

    public void setAccessToken(@NonNull AccessToken accessToken) {
        setAccessToken(accessToken, ACCESS_TOKEN_DEFAULT_KEY);
    }

    public void setAccessToken(@NonNull AccessToken accessToken, @NonNull String key) {
        mSharedPreferences.edit().putLong(key + DATE_KEY_SUFFIX, accessToken.getExpirationTime().getTime()).apply();
        mSharedPreferences.edit().putString(key + TOKEN_KEY_SUFFIX, accessToken.getToken()).apply();
        mSharedPreferences.edit().putStringSet(key + SCOPES_KEY_SUFFIX,
                AuthUtils.scopeCollectionToStringSet(accessToken.getScopes())).apply();
    }

    public void setAccessTokensDateOnly(long date, @NonNull String key) {
        mSharedPreferences.edit().putLong(key + DATE_KEY_SUFFIX, date).apply();
    }

    public void setAccessTokensDateBad(@NonNull String key) {
        mSharedPreferences.edit().putString(key + DATE_KEY_SUFFIX, "notALong").apply();
    }

    public void setAccessTokensTokenOnly(@NonNull String token, @NonNull String key) {
        mSharedPreferences.edit().putString(key + TOKEN_KEY_SUFFIX, token).apply();
    }

    public void setAccessTokensTokenBad(@NonNull String key) {
        mSharedPreferences.edit().putInt(key + TOKEN_KEY_SUFFIX, 1234).apply();
    }

    public void setAccessTokensScopesOnly(@NonNull Set<String> scopes, @NonNull String key) {
        mSharedPreferences.edit().putStringSet(key + SCOPES_KEY_SUFFIX, scopes).apply();
    }

    public void setAccessTokensScopesBad(@NonNull String key) {
        mSharedPreferences.edit().putInt(key + SCOPES_KEY_SUFFIX, 1234).apply();
    }
}
