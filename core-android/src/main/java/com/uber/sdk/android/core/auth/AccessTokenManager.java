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
import androidx.annotation.VisibleForTesting;
import android.webkit.CookieManager;

import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.AccessTokenStorage;
import com.uber.sdk.core.auth.Scope;

import java.util.Collection;
import java.util.Set;

/**
 * Manages the storage of {@link AccessToken}s.
 */
public class AccessTokenManager implements AccessTokenStorage {

    public static final String ACCESS_TOKEN_DEFAULT_KEY = "defaultAccessToken";
    private static final String ACCESS_TOKEN_SHARED_PREFERENCES = "uberSdkAccessTokenConfig";
    private static final String DATE_KEY_SUFFIX = "_date";
    private static final String EXPIRED_EMPTY_LOGGED_IN_COOKIE = "logged_in=;expires=Thu, 01 Jan 1970 00:00:01 GMT";
    private static final String EXPIRED_EMPTY_SESSION_COOKIE = "session=;expires=Thu, 01 Jan 1970 00:00:01 GMT";
    private static final String LOGIN_COOKIE_URL = "https://.login.uber.com";
    private static final String TOKEN_KEY_SUFFIX = "_token";
    private static final String REFRESH_TOKEN_KEY_SUFFIX = "_refresh_token";
    private static final String TOKEN_TYPE_KEY_SUFFIX = "_token_type";
    private static final String SCOPES_KEY_SUFFIX = "_scopes";
    private static final String UBER_COOKIE_URL = ".uber.com";

    @NonNull
    private final SharedPreferences sharedPreferences;

    @NonNull
    private final CookieUtils cookieUtils;

    @NonNull
    private final String accessTokenKey;

    /**
     *
     * @param context for access {@link SharedPreferences} to save {@link AccessToken}
     */
    public AccessTokenManager(@NonNull Context context) {
        this(context, ACCESS_TOKEN_DEFAULT_KEY);
    }

    /**
     * Instantiate {@link AccessTokenManager} and override accessTokenKey.
     * Use one instance per token.
     *
     * @param context
     * @param accessTokenKey key for storage
     */
    public AccessTokenManager(@NonNull Context context, @NonNull String accessTokenKey) {
        this(context, new CookieUtils(), accessTokenKey);
    }

    @VisibleForTesting
    AccessTokenManager(@NonNull Context context,
                       @NonNull CookieUtils cookieManagerUtil) {
        this(context, cookieManagerUtil, ACCESS_TOKEN_DEFAULT_KEY);
    }

    AccessTokenManager(@NonNull Context context,
                               @NonNull CookieUtils cookieManagerUtil,
                               @NonNull String accessTokenKey) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(ACCESS_TOKEN_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        cookieUtils = cookieManagerUtil;
        this.accessTokenKey = accessTokenKey;
    }

    /**
     * Gets an {@link AccessToken} stored.
     */
    @Override
    @Nullable
    public AccessToken getAccessToken() {

        long expiresIn;
        String token;
        Set<String> scopesString;
        String refreshToken;
        String tokenType;

        try {
            expiresIn = sharedPreferences.getLong(accessTokenKey + DATE_KEY_SUFFIX, -1);
            token = sharedPreferences.getString(accessTokenKey + TOKEN_KEY_SUFFIX, null);
            scopesString = sharedPreferences.getStringSet(accessTokenKey + SCOPES_KEY_SUFFIX, null);
            refreshToken = sharedPreferences.getString(accessTokenKey + REFRESH_TOKEN_KEY_SUFFIX, null);
            tokenType = sharedPreferences.getString(accessTokenKey + TOKEN_TYPE_KEY_SUFFIX, null);
        } catch (ClassCastException ignored) {
            return null;
        }

        if (expiresIn == -1 || token == null || scopesString == null) {
            // Return null, if we can't parse it this token is considered unsaved.
            return null;
        }

        Collection<Scope> scopes;
        try {
            scopes = AuthUtils.stringCollectionToScopeCollection(scopesString);
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        return new AccessToken(expiresIn, scopes, token, refreshToken, tokenType);
    }

    /**
     * Removes the {@link AccessToken} stored.
     **/
    @Override
    public void removeAccessToken() {
        cookieUtils.clearUberCookies();

        sharedPreferences.edit().remove(accessTokenKey + DATE_KEY_SUFFIX).apply();
        sharedPreferences.edit().remove(accessTokenKey + TOKEN_KEY_SUFFIX).apply();
        sharedPreferences.edit().remove(accessTokenKey + SCOPES_KEY_SUFFIX).apply();
        sharedPreferences.edit().remove(accessTokenKey + REFRESH_TOKEN_KEY_SUFFIX).apply();
        sharedPreferences.edit().remove(accessTokenKey + TOKEN_TYPE_KEY_SUFFIX).apply();
    }

    /**
     * Stores the {@link AccessToken}.
     *
     */
    @Override
    public void setAccessToken(@NonNull AccessToken accessToken) {
        sharedPreferences.edit().putLong(accessTokenKey + DATE_KEY_SUFFIX, accessToken.getExpiresIn()).apply();
        sharedPreferences.edit().putString(accessTokenKey + TOKEN_KEY_SUFFIX, accessToken.getToken()).apply();
        sharedPreferences.edit().putStringSet(accessTokenKey + SCOPES_KEY_SUFFIX,
                AuthUtils.scopeCollectionToStringSet(accessToken.getScopes())).apply();
        sharedPreferences.edit().putString(accessTokenKey + REFRESH_TOKEN_KEY_SUFFIX, accessToken.getRefreshToken()).apply();
        sharedPreferences.edit().putString(accessTokenKey + TOKEN_TYPE_KEY_SUFFIX, accessToken.getTokenType()).apply();
    }

    @VisibleForTesting
    static class CookieUtils {

        /**
         * Clears Uber logged_in and session cookies.
         */
        void clearUberCookies() {
            CookieManager cookieManager = CookieManager.getInstance();

            cookieManager.setCookie(UBER_COOKIE_URL, EXPIRED_EMPTY_LOGGED_IN_COOKIE);
            cookieManager.setCookie(LOGIN_COOKIE_URL, EXPIRED_EMPTY_SESSION_COOKIE);

            cookieManager.removeExpiredCookie();
        }
    }
}
