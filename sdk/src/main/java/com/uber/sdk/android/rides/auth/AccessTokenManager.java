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

package com.uber.sdk.android.rides.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.webkit.CookieManager;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * Manages the storage of {@link AccessToken}s.
 */
public class AccessTokenManager {

    private static final String ACCESS_TOKEN_DEFAULT_KEY = "defaultAccessToken";
    private static final String ACCESS_TOKEN_SHARED_PREFERENCES = "uberSdkAccessTokenConfig";
    private static final String DATE_KEY_SUFFIX = "_date";
    private static final String EXPIRED_EMPTY_LOGGED_IN_COOKIE = "logged_in=;expires=Thu, 01 Jan 1970 00:00:01 GMT";
    private static final String EXPIRED_EMPTY_SESSION_COOKIE = "session=;expires=Thu, 01 Jan 1970 00:00:01 GMT";
    private static final String LOGIN_COOKIE_URL = "https://.login.uber.com";
    private static final String TOKEN_KEY_SUFFIX = "_token";
    private static final String SCOPES_KEY_SUFFIX = "_scopes";
    private static final String UBER_COOKIE_URL = ".uber.com";

    @NonNull private final SharedPreferences mSharedPreferences;
    private final CookieUtils mCookieUtils;

    public AccessTokenManager(@NonNull Context context) {
        this(context, new CookieUtils());
    }

    @VisibleForTesting AccessTokenManager(@NonNull Context context, @NonNull CookieUtils cookieManagerUtil) {
        mSharedPreferences = context.getApplicationContext().getSharedPreferences(ACCESS_TOKEN_SHARED_PREFERENCES,
                Context.MODE_PRIVATE);
        mCookieUtils = cookieManagerUtil;
    }

    /**
     * Gets the default {@link AccessToken}.
     */
    @Nullable
    public AccessToken getAccessToken() {
        return getAccessToken(ACCESS_TOKEN_DEFAULT_KEY);
    }

    /**
     * Gets an {@link AccessToken} stored with a key.
     *
     * @param key the identifer pointing to a stored {@link AccessToken}.
     */
    @Nullable
    public AccessToken getAccessToken(@NonNull String key) {

        long expirationTime;
        String token;
        Set<String> scopesString;
        try {
            expirationTime = mSharedPreferences.getLong(key + DATE_KEY_SUFFIX, -1);
            token = mSharedPreferences.getString(key + TOKEN_KEY_SUFFIX, null);
            scopesString = mSharedPreferences.getStringSet(key + SCOPES_KEY_SUFFIX, null);
        } catch (ClassCastException ignored) {
            return null;
        }

        if (expirationTime == -1 || token == null || scopesString == null) {
            // Return null, if we can't parse it this token is considered unsaved.
            return null;
        }

        Collection<Scope> scopes;
        try {
            scopes = AuthUtils.stringSetToScopeCollection(scopesString);
        } catch (IllegalArgumentException ignored) {
            return null;
        }

        return new AccessToken(new Date(expirationTime), scopes, token);
    }

    /**
     * Removes the default {@link AccessToken}.
     */
    public void removeAccessToken() {
        removeAccessToken(ACCESS_TOKEN_DEFAULT_KEY);
    }

    /**
     * Removes an {@link AccessToken} stored with a key.
     *
     * @param key the identifer pointing to a stored {@link AccessToken}.
     */
    public void removeAccessToken(@NonNull String key) {
        mCookieUtils.clearUberCookies();

        mSharedPreferences.edit().remove(key + DATE_KEY_SUFFIX).apply();
        mSharedPreferences.edit().remove(key + TOKEN_KEY_SUFFIX).apply();
        mSharedPreferences.edit().remove(key + SCOPES_KEY_SUFFIX).apply();
    }

    /**
     * Stores an {@link AccessToken} in the default location.
     */
    public void setAccessToken(@NonNull AccessToken accessToken) {
        setAccessToken(accessToken, ACCESS_TOKEN_DEFAULT_KEY);
    }

    /**
     * Stores an {@link AccessToken} with a key.
     *
     * @param key the identifer pointing to a stored {@link AccessToken}.
     */
    public void setAccessToken(@NonNull AccessToken accessToken, @NonNull String key) {
        mSharedPreferences.edit().putLong(key + DATE_KEY_SUFFIX, accessToken.getExpirationTime().getTime()).apply();
        mSharedPreferences.edit().putString(key + TOKEN_KEY_SUFFIX, accessToken.getToken()).apply();
        mSharedPreferences.edit().putStringSet(key + SCOPES_KEY_SUFFIX,
                AuthUtils.scopeCollectionToStringSet(accessToken.getScopes())).apply();
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
