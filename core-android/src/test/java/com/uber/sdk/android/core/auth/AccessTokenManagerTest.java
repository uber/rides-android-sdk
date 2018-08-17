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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.uber.sdk.android.core.RobolectricTestBase;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.Scope;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import javax.annotation.Nullable;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AccessTokenManagerTest extends RobolectricTestBase {

    private static final AccessToken ACCESS_TOKEN_FIRST = new AccessToken(2592000,
            ImmutableList.of(Scope.PROFILE, Scope.HISTORY), "thisIsAnAccessToken", "refreshToken", "tokenType");
    private static final AccessToken ACCESS_TOKEN_SECOND = new AccessToken(2592000,
            ImmutableList.of(Scope.PROFILE), "thisIsASecondAccessToken", "refreshToken", "tokenType");
    private static final String DEFAULT_ACCESS_TOKEN_KEY = "defaultAccessToken";
    private static final String CUSTOM_ACCESS_TOKEN_KEY = "customTag";

    private AccessTokenPreferences tokenPreferences;
    private AccessTokenManager accessTokenManager;
    private AccessTokenManager customAccessTokenManager;
    private AccessTokenManager.CookieUtils cookieUtils;

    @Before
    public void setup() {
        tokenPreferences = new AccessTokenPreferences(RuntimeEnvironment.application);
        tokenPreferences.clear();

        cookieUtils = mock(AccessTokenManager.CookieUtils.class);
        accessTokenManager = new AccessTokenManager(RuntimeEnvironment.application, cookieUtils);
        customAccessTokenManager = new AccessTokenManager(RuntimeEnvironment.application, cookieUtils,
                CUSTOM_ACCESS_TOKEN_KEY);
    }

    @Test
    public void getAccessToken_whentNoDefaultTokenStored_shouldReturnNull() {
        assertNull(accessTokenManager.getAccessToken());
    }

    @Test
    public void getAccessToken_whenNoCustomTokenStored_shouldReturnNull() {
        assertNull(customAccessTokenManager.getAccessToken());
    }

    @Test
    public void getAccessToken_whenNoDateStored_shouldReturnNull() {
        Set<String> scopes = ImmutableSet.of("profile");
        tokenPreferences.setAccessTokensScopesOnly(scopes, DEFAULT_ACCESS_TOKEN_KEY);
        tokenPreferences.setAccessTokensTokenOnly("accessToken", DEFAULT_ACCESS_TOKEN_KEY);

        assertNull(accessTokenManager.getAccessToken());
    }

    @Test
    public void getAccessToken_whenNoScopesStringStored_shouldReturnNull() {
        tokenPreferences.setAccessTokensDateOnly(2592000, DEFAULT_ACCESS_TOKEN_KEY);
        tokenPreferences.setAccessTokensTokenOnly("accessToken", DEFAULT_ACCESS_TOKEN_KEY);

        assertNull(accessTokenManager.getAccessToken());
    }

    @Test
    public void getAccessToken_whenNoTokenStringStored_shouldReturnNull() {
        tokenPreferences.setAccessTokensDateOnly(2592000, DEFAULT_ACCESS_TOKEN_KEY);
        Set<String> scopes = ImmutableSet.of("profile");
        tokenPreferences.setAccessTokensScopesOnly(scopes, DEFAULT_ACCESS_TOKEN_KEY);

        assertNull(accessTokenManager.getAccessToken());
    }

    @Test
    public void getAccessToken_whenInvalidScopesStringStored_shouldReturnNull() {
        tokenPreferences.setAccessTokensDateOnly(2592000, DEFAULT_ACCESS_TOKEN_KEY);
        tokenPreferences.setAccessTokensTokenOnly("accessToken", DEFAULT_ACCESS_TOKEN_KEY);
        Set<String> scopes = ImmutableSet.of("thisIsNotAScope");
        tokenPreferences.setAccessTokensScopesOnly(scopes, DEFAULT_ACCESS_TOKEN_KEY);

        assertNull(accessTokenManager.getAccessToken());
    }

    @Test
    public void getAccessToken_whenWrongClassDateStored_shouldReturnNull() {
        tokenPreferences.setAccessTokensDateBad(DEFAULT_ACCESS_TOKEN_KEY);
        tokenPreferences.setAccessTokensTokenOnly("accessToken", DEFAULT_ACCESS_TOKEN_KEY);
        Set<String> scopes = ImmutableSet.of("profile");
        tokenPreferences.setAccessTokensScopesOnly(scopes, DEFAULT_ACCESS_TOKEN_KEY);

        assertNull(accessTokenManager.getAccessToken());
    }

    @Test
    public void getAccessToken_whenWrongClassTokenStored_shouldReturnNull() {
        tokenPreferences.setAccessTokensDateOnly(2592000, DEFAULT_ACCESS_TOKEN_KEY);
        tokenPreferences.setAccessTokensTokenBad(DEFAULT_ACCESS_TOKEN_KEY);
        Set<String> scopes = ImmutableSet.of("profile");
        tokenPreferences.setAccessTokensScopesOnly(scopes, DEFAULT_ACCESS_TOKEN_KEY);

        assertNull(accessTokenManager.getAccessToken());
    }

    @Test
    public void getAccessToken_whenWrongClassScopesStored_shouldReturnNull() {
        tokenPreferences.setAccessTokensDateOnly(2592000, DEFAULT_ACCESS_TOKEN_KEY);
        tokenPreferences.setAccessTokensTokenOnly("accessToken", DEFAULT_ACCESS_TOKEN_KEY);
        tokenPreferences.setAccessTokensScopesBad(DEFAULT_ACCESS_TOKEN_KEY);

        assertNull(accessTokenManager.getAccessToken());
    }

    @Test
    public void getAccessToken_whenTokenWithDefaultTagStored_shouldReturnToken() {
        tokenPreferences.setAccessToken(ACCESS_TOKEN_FIRST);

        AccessToken accessTokenFromManager = accessTokenManager.getAccessToken();
        assertAccessTokensEqual(ACCESS_TOKEN_FIRST, accessTokenFromManager);
    }

    @Test
    public void getAccessToken_whenTokenWithCustomTagStoredAndNoDefault_shouldReturnTokenAndDefaultNull()
            throws JSONException {
        tokenPreferences.setAccessToken(ACCESS_TOKEN_FIRST, CUSTOM_ACCESS_TOKEN_KEY);

        assertNull(accessTokenManager.getAccessToken());
        AccessToken accessTokenWithTag = customAccessTokenManager.getAccessToken();
        assertAccessTokensEqual(ACCESS_TOKEN_FIRST, accessTokenWithTag);
    }

    @Test
    public void getAccessToken_whenMultipleTokensStoredWithTagsAndDefault_shouldReturnTokens()
            throws JSONException {
        tokenPreferences.setAccessToken(ACCESS_TOKEN_FIRST);
        tokenPreferences.setAccessToken(ACCESS_TOKEN_SECOND, CUSTOM_ACCESS_TOKEN_KEY);

        assertAccessTokensEqual(ACCESS_TOKEN_FIRST, accessTokenManager.getAccessToken());
        assertAccessTokensEqual(ACCESS_TOKEN_SECOND, customAccessTokenManager.getAccessToken());
    }

    @Test
    public void removeAccessToken_whenNoDefaultTokenStored_shouldSucceed() {
        accessTokenManager.removeAccessToken();
        assertNull(tokenPreferences.getAccessToken());
        verify(cookieUtils, times(1)).clearUberCookies();
    }

    @Test
    public void removeAccessToken_whenNoCustomTokenStored_shouldSucceed() {
        customAccessTokenManager.removeAccessToken();
        assertNull(tokenPreferences.getAccessToken(CUSTOM_ACCESS_TOKEN_KEY));
        verify(cookieUtils, times(1)).clearUberCookies();
    }

    @Test
    public void removeAccessToken_whenTokenWithDefaultTagStored_shouldSucceed() throws JSONException {
        tokenPreferences.setAccessToken(ACCESS_TOKEN_FIRST);

        accessTokenManager.removeAccessToken();
        assertNull(tokenPreferences.getAccessToken());
        verify(cookieUtils, times(1)).clearUberCookies();
    }

    @Test
    public void removeAccessToken_whenTokenWithCustomTagStored_shouldSucceed() throws JSONException {
        tokenPreferences.setAccessToken(ACCESS_TOKEN_FIRST, CUSTOM_ACCESS_TOKEN_KEY);

        customAccessTokenManager.removeAccessToken();
        assertNull(tokenPreferences.getAccessToken(CUSTOM_ACCESS_TOKEN_KEY));
        verify(cookieUtils, times(1)).clearUberCookies();
    }

    @Test
    public void removeAccessToken_whenMultipleTokensStoredAndOnlyDefaultRemoved_shouldOnlyRemoveDefaultToken()
            throws JSONException {
        tokenPreferences.setAccessToken(ACCESS_TOKEN_FIRST);
        tokenPreferences.setAccessToken(ACCESS_TOKEN_SECOND, CUSTOM_ACCESS_TOKEN_KEY);

        accessTokenManager.removeAccessToken();
        assertNull(tokenPreferences.getAccessToken());
        assertAccessTokensEqual(ACCESS_TOKEN_SECOND, tokenPreferences.getAccessToken(CUSTOM_ACCESS_TOKEN_KEY));
        verify(cookieUtils, times(1)).clearUberCookies();
    }

    @Test
    public void removeAccessToken_whenMultipleTokensStoredAndOnlyCustomRemoved_shouldOnlyRemoveCustomToken()
            throws JSONException {
        tokenPreferences.setAccessToken(ACCESS_TOKEN_FIRST);
        tokenPreferences.setAccessToken(ACCESS_TOKEN_SECOND, CUSTOM_ACCESS_TOKEN_KEY);

        customAccessTokenManager.removeAccessToken();

        assertAccessTokensEqual(ACCESS_TOKEN_FIRST, tokenPreferences.getAccessToken());
        assertNull(tokenPreferences.getAccessToken(CUSTOM_ACCESS_TOKEN_KEY));
        verify(cookieUtils, times(1)).clearUberCookies();
    }

    @Test
    public void setAccessToken_whenNoCustomTokenStored_shouldSucceed() throws JSONException {
        accessTokenManager.setAccessToken(ACCESS_TOKEN_FIRST);

        assertAccessTokensEqual(ACCESS_TOKEN_FIRST, accessTokenManager.getAccessToken());
    }

    @Test
    public void setAccessToken_whenCustomTokenStored_shouldSucceed() throws JSONException {
        customAccessTokenManager.setAccessToken(ACCESS_TOKEN_FIRST);

        assertNull(tokenPreferences.getAccessToken());
        assertAccessTokensEqual(ACCESS_TOKEN_FIRST, tokenPreferences.getAccessToken(CUSTOM_ACCESS_TOKEN_KEY));
    }

    @Test
    public void setAccessToken_whenDefaultTokenAlreadyStored_shouldOverwrite() throws JSONException {
        tokenPreferences.setAccessToken(ACCESS_TOKEN_FIRST);

        accessTokenManager.setAccessToken(ACCESS_TOKEN_SECOND);
        assertAccessTokensEqual(ACCESS_TOKEN_SECOND, tokenPreferences.getAccessToken());
    }

    @Test
    public void setAccessToken_whenCustomTokenAlreadyStored_shouldOverwrite() throws JSONException {
        tokenPreferences.setAccessToken(ACCESS_TOKEN_FIRST, CUSTOM_ACCESS_TOKEN_KEY);
        customAccessTokenManager.setAccessToken(ACCESS_TOKEN_SECOND);
        assertAccessTokensEqual(ACCESS_TOKEN_SECOND, tokenPreferences.getAccessToken(CUSTOM_ACCESS_TOKEN_KEY));
    }

    @Test
    public void setAccessToken_whenBothDefaultAndCustomTokenSet_shouldSucceed() throws JSONException {
        accessTokenManager.setAccessToken(ACCESS_TOKEN_FIRST);
        customAccessTokenManager.setAccessToken(ACCESS_TOKEN_SECOND);

        assertAccessTokensEqual(ACCESS_TOKEN_FIRST, tokenPreferences.getAccessToken());
        assertAccessTokensEqual(ACCESS_TOKEN_SECOND, tokenPreferences.getAccessToken(CUSTOM_ACCESS_TOKEN_KEY));
    }

    private void assertAccessTokensEqual(AccessToken accessTokenExpected, @Nullable AccessToken accessTokenActual) {
        assertNotNull(accessTokenActual);
        assertEquals(accessTokenExpected.getExpiresIn(),
                accessTokenActual.getExpiresIn());
        assertEquals(accessTokenExpected.getToken(), accessTokenActual.getToken());
        assertTrue(accessTokenActual.getScopes().containsAll(accessTokenExpected.getScopes()));
        assertEquals(accessTokenExpected.getScopes().size(), accessTokenActual.getScopes().size());
    }
}
