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

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.WebView;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class for the Uber SDK.
 */
class AuthUtils {

    /**
     * Converts a {@link Collection} of {@link Scope}s to a {@link Set} of {@link String}s.
     *
     * @param scopeCollection the {@link Collection} of {@link Scope}s to convert.
     * @return a {@link Set} of {@link String}s.
     */
    @NonNull
    static Set<String> scopeCollectionToStringSet(@NonNull Collection<Scope> scopeCollection) {
        Set<String> stringCollection = new HashSet<>();
        for (Scope scope : scopeCollection) {
            stringCollection.add(scope.name());
        }

        return stringCollection;
    }

    /**
     * Converts a {@link String} representing space delimited {@link Scope}s to a {@link Collection<Scope>}.
     *
     * @param scopesString the {@link String} to convert.
     * @return a {@link Collection} of {@link Scope}s.
     * @throws IllegalArgumentException if a part of the string doesn't match a scope name.
     */
    @NonNull
    static Collection<Scope> stringToScopeCollection(@NonNull String scopesString) throws IllegalArgumentException {
        Set<Scope> scopeCollection = new HashSet<>();

        if (scopesString.isEmpty()) {
            return scopeCollection;
        }

        String[] scopeStrings = scopesString.split(" ");
        for (String scopeName : scopeStrings) {
            scopeCollection.add(Scope.valueOf(scopeName.toUpperCase()));
        }

        return scopeCollection;
    }

    /**
     * Converts a {@link Set} of {@link String}s to {@link Collection} of {@link Scope}s.
     *
     * @param stringSet the {@link Set} of {@link String}s to convert.
     * @return a {@link Collection} of {@link Scope}s.
     */
    @NonNull
    static Collection<Scope> stringSetToScopeCollection(@NonNull Set<String> stringSet)
            throws IllegalArgumentException {
        Set<Scope> scopeCollection = new HashSet<>();

        for (String scopeName : stringSet) {
            scopeCollection.add(Scope.valueOf(scopeName));
        }
        return scopeCollection;
    }

    /**
     * Converts a {@link Collection} of {@link Scope}s into a space-delimited {@link String}.
     *
     * @param scopes the {@link Collection} of {@link Scope}s to convert
     * @return a space-delimited {@link String} of {@link Scope}s
     */
    @NonNull
    static String scopeCollectionToString(@NonNull Collection<Scope> scopes) {
        Set<String> stringSet = scopeCollectionToStringSet(scopes);
        return TextUtils.join(" ", stringSet).toLowerCase();
    }

    /**
     * Generates an {@link AccessToken} from the redirect URI.
     * The redirect URI can either contain valid access token information (token string, expiration time, scopes),
     * or error in the query parameter (ex. ?error=invalid_client_id).
     * This functions only throws if an error has occurred and contains the {@link AuthenticationError}.
     *
     * @param uri the {@link Uri} to parse from the {@link WebView}.
     * @return the {@link AccessToken} if successful.
     * @throws LoginAuthenticationException containing the {@link AuthenticationError}
     */
    @NonNull
    static AccessToken generateAccessTokenFromUrl(@NonNull Uri uri) throws LoginAuthenticationException {
        final String ERROR = "error";

        // It's possible to receive an error in the query parameter of the redirect URI.
        if (uri.getQueryParameter(ERROR) != null) {
            AuthenticationError error;
            try {
                error = AuthenticationError.valueOf(uri.getQueryParameter(ERROR).toUpperCase());
            } catch (IllegalArgumentException e) {
                error = AuthenticationError.INVALID_RESPONSE;
            }
            throw new LoginAuthenticationException(error);
        }

        String fragment = uri.getFragment();

        // The access token information is contained in the fragment and it should not be null.
        if (fragment == null) {
            throw new LoginAuthenticationException(AuthenticationError.INVALID_RESPONSE);
        }

        // The access token requires three pieces of information to be successful: token string, expiration date,
        // scopes.
        Uri fragmentUri = new Uri.Builder().encodedQuery(fragment).build();
        String accessToken = fragmentUri.getQueryParameter(AccessToken.KEY_TOKEN);
        String expirationSeconds = fragmentUri.getQueryParameter(AccessToken.KEY_EXPIRATION_TIME);
        String scopesString = fragmentUri.getQueryParameter(AccessToken.KEY_SCOPES);

        if (TextUtils.isEmpty(accessToken) || TextUtils.isEmpty(expirationSeconds) || TextUtils.isEmpty(scopesString)) {
            throw new LoginAuthenticationException(AuthenticationError.INVALID_RESPONSE);
        }

        long time;

        try {
            time = Long.valueOf(expirationSeconds);
        } catch (NumberFormatException ignored) {
            throw new LoginAuthenticationException(AuthenticationError.INVALID_RESPONSE);
        }

        Collection<Scope> scopes;
        try {
            scopes = stringToScopeCollection(scopesString);
        } catch (IllegalArgumentException ignored) {
            throw new LoginAuthenticationException(AuthenticationError.INVALID_RESPONSE);
        }

        return new AccessToken(new Date(time), scopes, accessToken);

    }
}
