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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Base64;
import android.webkit.WebView;

import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.core.client.SessionConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A utility class for the Uber SDK.
 */
class AuthUtils {
    static final String KEY_AUTHENTICATION_CODE = "code";
    static final String KEY_EXPIRATION_TIME = "expires_in";
    static final String KEY_SCOPES = "scope";
    static final String KEY_TOKEN = "access_token";
    static final String KEY_REFRESH_TOKEN = "refresh_token";
    static final String KEY_TOKEN_TYPE = "token_type";

    /**
     * @param scopeCollection
     * @return true if any {@link com.uber.sdk.core.auth.Scope}s requested is {@link com.uber.sdk.core.auth.Scope.ScopeType#PRIVILEGED}
     */
    static boolean isPrivilegeScopeRequired(@NonNull Collection<Scope> scopeCollection) {
        for (Scope scope : scopeCollection) {
            if (scope.getScopeType().equals(Scope.ScopeType.PRIVILEGED)) {
                return true;
            }
        }
        return false;
    }

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
            try {
                scopeCollection.add(Scope.valueOf(scopeName.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // do nothing, will omit custom or bad scopes
            }
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
    static Collection<Scope> stringCollectionToScopeCollection(@NonNull Collection<String> stringSet)
            throws IllegalArgumentException {
        Set<Scope> scopeCollection = new HashSet<>();

        for (String scopeName : stringSet) {
            scopeCollection.add(Scope.valueOf(scopeName));
        }
        return scopeCollection;
    }


    public static boolean isRedirectUriRegistered(@NonNull Activity activity, @NonNull Uri uri) {

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);
        ComponentName info = intent.resolveActivity(activity.getPackageManager());

        return info != null && info.getClassName().equals(LoginRedirectReceiverActivity.class
                .getName());


    }

    /**
     * Converts a {@link Collection} of {@link Scope}s into a space-delimited {@link String}.
     *
     * @param scopes the {@link Collection} of {@link Scope}s to convert
     * @return a space-delimited {@link String} of {@link Scope}s
     */
    @NonNull
    public static String scopeCollectionToString(@NonNull Collection<Scope> scopes) {
        Set<String> stringSet = scopeCollectionToStringSet(scopes);
        return TextUtils.join(" ", stringSet).toLowerCase();
    }

    /**
     * Converts a {@link Collection} of {@link String}s into a space-delimited {@link String}.
     *
     * @param scopes the {@link Collection} of {@link String}s to convert
     * @return a space-delimited {@link String} of {@link Scope}s
     */
    public static String customScopeCollectionToString(@NonNull Collection<String> scopes) {
        return TextUtils.join(" ", scopes).toLowerCase();
    }

    /**
     *
     * @param scopes array to return as space delimited
     * @return space-delimited {@link String} of Scopes and Custom Scopes
     */
    public static String mergeScopeStrings(String... scopes) {
        return TextUtils.join(" ", scopes).trim();
    }

    @NonNull
    static AccessToken parseTokenUri(@NonNull Uri uri) throws LoginAuthenticationException {
        final long expiresIn;
        try {
            expiresIn = Long.valueOf(uri.getQueryParameter(KEY_EXPIRATION_TIME));
        } catch (NumberFormatException ex) {
            throw new LoginAuthenticationException(AuthenticationError.INVALID_RESPONSE);
        }

        final String accessToken = uri.getQueryParameter(KEY_TOKEN);
        final String refreshToken = uri.getQueryParameter(KEY_REFRESH_TOKEN);
        final String scope = uri.getQueryParameter(KEY_SCOPES);
        final String tokenType = uri.getQueryParameter(KEY_TOKEN_TYPE);

        if (TextUtils.isEmpty(accessToken) || TextUtils.isEmpty(scope) || TextUtils.isEmpty(tokenType)) {
            throw new LoginAuthenticationException(AuthenticationError.INVALID_RESPONSE);
        }

        return new AccessToken(expiresIn, AuthUtils.stringToScopeCollection
                (scope), accessToken, refreshToken, tokenType);
    }

    @NonNull
    static Intent parseTokenUriToIntent(@NonNull Uri uri) throws LoginAuthenticationException {
        final long expiresIn;
        try {
            expiresIn = Long.valueOf(uri.getQueryParameter(KEY_EXPIRATION_TIME));
        } catch (NumberFormatException ex) {
            throw new LoginAuthenticationException(AuthenticationError.INVALID_RESPONSE);
        }

        final String accessToken = uri.getQueryParameter(KEY_TOKEN);
        final String refreshToken = uri.getQueryParameter(KEY_REFRESH_TOKEN);
        final String scope = uri.getQueryParameter(KEY_SCOPES);
        final String tokenType = uri.getQueryParameter(KEY_TOKEN_TYPE);

        if (TextUtils.isEmpty(accessToken) || TextUtils.isEmpty(scope) || TextUtils.isEmpty(tokenType)) {
            throw new LoginAuthenticationException(AuthenticationError.INVALID_RESPONSE);
        }

        Intent data = new Intent();
        data.putExtra(LoginManager.EXTRA_ACCESS_TOKEN, accessToken);
        data.putExtra(LoginManager.EXTRA_REFRESH_TOKEN, refreshToken);
        data.putExtra(LoginManager.EXTRA_SCOPE, scope);
        data.putExtra(LoginManager.EXTRA_EXPIRES_IN, expiresIn);
        data.putExtra(LoginManager.EXTRA_TOKEN_TYPE, tokenType);
        return data;
    }

    static boolean isAuthorizationCodePresent(@NonNull Uri uri) {
        return !TextUtils.isEmpty(uri.getQueryParameter(KEY_AUTHENTICATION_CODE));
    }

    static String parseAuthorizationCode(@NonNull Uri uri) throws LoginAuthenticationException {
        final String code = uri.getQueryParameter(KEY_AUTHENTICATION_CODE);
        if (TextUtils.isEmpty(code)) {
            throw new LoginAuthenticationException(AuthenticationError.INVALID_RESPONSE);
        }

        return code;
    }

    @NonNull
    static AccessToken createAccessToken(Intent intent) {
        String token = intent.getStringExtra(LoginManager.EXTRA_ACCESS_TOKEN);
        String refreshToken = intent.getStringExtra(LoginManager.EXTRA_REFRESH_TOKEN);
        String scope = intent.getStringExtra(LoginManager.EXTRA_SCOPE);
        String tokenType = intent.getStringExtra(LoginManager.EXTRA_TOKEN_TYPE);
        long expiresIn = intent.getLongExtra(LoginManager.EXTRA_EXPIRES_IN, 0);

        return new AccessToken(expiresIn, AuthUtils.stringToScopeCollection
                (scope), token, refreshToken, tokenType);

    }

    static String createEncodedParam(String rawParam) {
        return Base64.encodeToString(rawParam.getBytes(), Base64.DEFAULT);
    }

    /**
     * Builds a URL {@link String} using the necessary parameters to load in the {@link WebView}.
     *
     * @return the URL to load in the {@link WebView}
     */
    @NonNull
    static String buildUrl(
            @NonNull String redirectUri,
            @NonNull ResponseType responseType,
            @NonNull SessionConfiguration configuration) {

        final String CLIENT_ID_PARAM = "client_id";
        final String ENDPOINT = "login";
        final String HTTPS = "https";
        final String PATH = "oauth/v2/authorize";
        final String REDIRECT_PARAM = "redirect_uri";
        final String RESPONSE_TYPE_PARAM = "response_type";
        final String SCOPE_PARAM = "scope";
        final String SHOW_FB_PARAM = "show_fb";
        final String SIGNUP_PARAMS = "signup_params";
        final String REDIRECT_LOGIN = "{\"redirect_to_login\":true}";



        Uri.Builder builder = new Uri.Builder();
        builder.scheme(HTTPS)
                .authority(ENDPOINT + "." + configuration.getEndpointRegion().getDomain())
                .appendEncodedPath(PATH)
                .appendQueryParameter(CLIENT_ID_PARAM, configuration.getClientId())
                .appendQueryParameter(REDIRECT_PARAM, redirectUri)
                .appendQueryParameter(RESPONSE_TYPE_PARAM, responseType.toString().toLowerCase(
                        Locale.US))
                .appendQueryParameter(SCOPE_PARAM, getScopes(configuration))
                .appendQueryParameter(SHOW_FB_PARAM, "false")
                .appendQueryParameter(SIGNUP_PARAMS, AuthUtils.createEncodedParam(REDIRECT_LOGIN));

        return builder.build().toString();
    }

    private static String getScopes(SessionConfiguration configuration) {
        String scopes = AuthUtils.scopeCollectionToString(configuration.getScopes());
        if (!configuration.getCustomScopes().isEmpty()) {
            scopes =  AuthUtils.mergeScopeStrings(scopes,
                    AuthUtils.customScopeCollectionToString(configuration.getCustomScopes()));
        }
        return scopes;
    }
}
