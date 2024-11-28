/*
 * Copyright (c) 2016 Uber Technologies, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.uber.sdk.android.core.auth;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.uber.sdk.core.auth.AccessToken;

import java.util.Set;

public class AccessTokenPreferences {

    private static final String ACCESS_TOKEN_DEFAULT_KEY = "defaultAccessToken";
    private static final String ACCESS_TOKEN_SHARED_PREFERENCES = "uberSdkAccessTokenConfig";
    private static final String DATE_KEY_SUFFIX = "_date";
    private static final String TOKEN_KEY_SUFFIX = "_token";
    private static final String SCOPES_KEY_SUFFIX = "_scopes";

    @NonNull
    private SharedPreferences sharedPreferences;

    public AccessTokenPreferences(@NonNull Context context) {
        sharedPreferences = context.getSharedPreferences(ACCESS_TOKEN_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    @Nullable
    public AccessToken getAccessToken() {
        return getAccessToken(ACCESS_TOKEN_DEFAULT_KEY);
    }

    @Nullable
    public AccessToken getAccessToken(@NonNull String key) {
        long expirationTime = sharedPreferences.getLong(key + DATE_KEY_SUFFIX, -1);
        String token = sharedPreferences.getString(key + TOKEN_KEY_SUFFIX, null);
        Set<String> scopes = sharedPreferences.getStringSet(key + SCOPES_KEY_SUFFIX, null);

        if (expirationTime == -1 || token == null || scopes == null) {
            // Return null, if we can't parse it this token is considered unsaved.
            return null;
        }

        return new AccessToken(expirationTime, AuthUtils.stringCollectionToScopeCollection(scopes), token,
                "refreshToken", "tokenType");
    }

    public void removeAccessToken() {
        removeAccessToken(ACCESS_TOKEN_DEFAULT_KEY);
    }

    public void removeAccessToken(@NonNull String key) {
        sharedPreferences.edit().remove(key + DATE_KEY_SUFFIX).apply();
        sharedPreferences.edit().remove(key + TOKEN_KEY_SUFFIX).apply();
        sharedPreferences.edit().remove(key + SCOPES_KEY_SUFFIX).apply();
    }

    public void setAccessToken(@NonNull AccessToken accessToken) {
        setAccessToken(accessToken, ACCESS_TOKEN_DEFAULT_KEY);
    }

    public void setAccessToken(@NonNull AccessToken accessToken, @NonNull String key) {
        sharedPreferences.edit().putLong(key + DATE_KEY_SUFFIX, accessToken.getExpiresIn()).apply();
        sharedPreferences.edit().putString(key + TOKEN_KEY_SUFFIX, accessToken.getToken()).apply();
        sharedPreferences.edit().putStringSet(key + SCOPES_KEY_SUFFIX,
                AuthUtils.scopeCollectionToStringSet(accessToken.getScopes())).apply();
    }

    public void setAccessTokensDateOnly(long date, @NonNull String key) {
        sharedPreferences.edit().putLong(key + DATE_KEY_SUFFIX, date).apply();
    }

    public void setAccessTokensDateBad(@NonNull String key) {
        sharedPreferences.edit().putString(key + DATE_KEY_SUFFIX, "notALong").apply();
    }

    public void setAccessTokensTokenOnly(@NonNull String token, @NonNull String key) {
        sharedPreferences.edit().putString(key + TOKEN_KEY_SUFFIX, token).apply();
    }

    public void setAccessTokensTokenBad(@NonNull String key) {
        sharedPreferences.edit().putInt(key + TOKEN_KEY_SUFFIX, 1234).apply();
    }

    public void setAccessTokensScopesOnly(@NonNull Set<String> scopes, @NonNull String key) {
        sharedPreferences.edit().putStringSet(key + SCOPES_KEY_SUFFIX, scopes).apply();
    }

    public void setAccessTokensScopesBad(@NonNull String key) {
        sharedPreferences.edit().putInt(key + SCOPES_KEY_SUFFIX, 1234).apply();
    }
}
