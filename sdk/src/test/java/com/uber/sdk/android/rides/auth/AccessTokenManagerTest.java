/*
 * Copyright (c) 2015 Uber Technologies, Inc.
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.uber.sdk.android.rides.RobolectricTestBase;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.Date;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AccessTokenManagerTest extends RobolectricTestBase {

    private static final AccessToken ACCESS_TOKEN_FIRST = new AccessToken(new Date(1458770906206l),
            ImmutableList.of(Scope.PROFILE, Scope.HISTORY),
            "thisIsAnAccessToken");
    private static final AccessToken ACCESS_TOKEN_SECOND = new AccessToken(new Date(1458770901337l),
            ImmutableList.of(Scope.PROFILE),
            "thisIsASecondAccessToken");
    private static final String DEFAULT_ACCESS_TOKEN_KEY = "defaultAccessToken";
    private static final String CUSTOM_ACCESS_TOKEN_KEY = "customTag";

    private AccessTokenPreferences mTokenPreferences;
    private AccessTokenManager mAccessTokenManager;
    private AccessTokenManager.CookieUtils mCookieUtils;

    @Before
    public void setup() {
        mTokenPreferences = new AccessTokenPreferences(RuntimeEnvironment.application);
        mTokenPreferences.clear();

        mCookieUtils = mock(AccessTokenManager.CookieUtils.class);
        mAccessTokenManager = new AccessTokenManager(RuntimeEnvironment.application, mCookieUtils);
    }

    @Test
    public void getAccessToken_whentNoDefaultTokenStored_shouldReturnNull() {
        assertNull(mAccessTokenManager.getAccessToken());
    }

    @Test
    public void getAccessToken_whenNoCustomTokenStored_shouldReturnNull() {
        assertNull(mAccessTokenManager.getAccessToken(CUSTOM_ACCESS_TOKEN_KEY));
    }

    @Test
    public void getAccessToken_whenNoDateStored_shouldReturnNull() {
        Set<String> scopes = ImmutableSet.of("profile");
        mTokenPreferences.setAccessTokensScopesOnly(scopes, DEFAULT_ACCESS_TOKEN_KEY);
        mTokenPreferences.setAccessTokensTokenOnly("accessToken", DEFAULT_ACCESS_TOKEN_KEY);

        assertNull(mAccessTokenManager.getAccessToken());
    }

    @Test
    public void getAccessToken_whenNoScopesStringStored_shouldReturnNull() {
        mTokenPreferences.setAccessTokensDateOnly(1458770906206l, DEFAULT_ACCESS_TOKEN_KEY);
        mTokenPreferences.setAccessTokensTokenOnly("accessToken", DEFAULT_ACCESS_TOKEN_KEY);

        assertNull(mAccessTokenManager.getAccessToken());
    }

    @Test
    public void getAccessToken_whenNoTokenStringStored_shouldReturnNull() {
        mTokenPreferences.setAccessTokensDateOnly(1458770906206l, DEFAULT_ACCESS_TOKEN_KEY);
        Set<String> scopes = ImmutableSet.of("profile");
        mTokenPreferences.setAccessTokensScopesOnly(scopes, DEFAULT_ACCESS_TOKEN_KEY);

        assertNull(mAccessTokenManager.getAccessToken());
    }

    @Test
    public void getAccessToken_whenInvalidScopesStringStored_shouldReturnNull() {
        mTokenPreferences.setAccessTokensDateOnly(1458770906206l, DEFAULT_ACCESS_TOKEN_KEY);
        mTokenPreferences.setAccessTokensTokenOnly("accessToken", DEFAULT_ACCESS_TOKEN_KEY);
        Set<String> scopes = ImmutableSet.of("thisIsNotAScope");
        mTokenPreferences.setAccessTokensScopesOnly(scopes, DEFAULT_ACCESS_TOKEN_KEY);

        assertNull(mAccessTokenManager.getAccessToken());
    }

    @Test
    public void getAccessToken_whenWrongClassDateStored_shouldReturnNull() {
        mTokenPreferences.setAccessTokensDateBad(DEFAULT_ACCESS_TOKEN_KEY);
        mTokenPreferences.setAccessTokensTokenOnly("accessToken", DEFAULT_ACCESS_TOKEN_KEY);
        Set<String> scopes = ImmutableSet.of("profile");
        mTokenPreferences.setAccessTokensScopesOnly(scopes, DEFAULT_ACCESS_TOKEN_KEY);

        assertNull(mAccessTokenManager.getAccessToken());
    }

    @Test
    public void getAccessToken_whenWrongClassTokenStored_shouldReturnNull() {
        mTokenPreferences.setAccessTokensDateOnly(1458770906206l, DEFAULT_ACCESS_TOKEN_KEY);
        mTokenPreferences.setAccessTokensTokenBad(DEFAULT_ACCESS_TOKEN_KEY);
        Set<String> scopes = ImmutableSet.of("profile");
        mTokenPreferences.setAccessTokensScopesOnly(scopes, DEFAULT_ACCESS_TOKEN_KEY);

        assertNull(mAccessTokenManager.getAccessToken());
    }

    @Test
    public void getAccessToken_whenWrongClassScopesStored_shouldReturnNull() {
        mTokenPreferences.setAccessTokensDateOnly(1458770906206l, DEFAULT_ACCESS_TOKEN_KEY);
        mTokenPreferences.setAccessTokensTokenOnly("accessToken", DEFAULT_ACCESS_TOKEN_KEY);
        mTokenPreferences.setAccessTokensScopesBad(DEFAULT_ACCESS_TOKEN_KEY);

        assertNull(mAccessTokenManager.getAccessToken());
    }

    @Test
    public void getAccessToken_whenTokenWithDefaultTagStored_shouldReturnToken() {
        mTokenPreferences.setAccessToken(ACCESS_TOKEN_FIRST);

        AccessToken accessTokenFromManager = mAccessTokenManager.getAccessToken();
        assertAccessTokensEqual(ACCESS_TOKEN_FIRST, accessTokenFromManager);
    }

    @Test
    public void getAccessToken_whenTokenWithCustomTagStoredAndNoDefault_shouldReturnTokenAndDefaultNull()
            throws JSONException {

        mTokenPreferences.setAccessToken(ACCESS_TOKEN_FIRST, CUSTOM_ACCESS_TOKEN_KEY);

        assertNull(mAccessTokenManager.getAccessToken());
        AccessToken accessTokenWithTag = mAccessTokenManager.getAccessToken(CUSTOM_ACCESS_TOKEN_KEY);
        assertAccessTokensEqual(ACCESS_TOKEN_FIRST, accessTokenWithTag);
    }

    @Test
    public void getAccessToken_whenMultipleTokensStoredWithTagsAndDefault_shouldReturnTokens()
            throws JSONException {
        mTokenPreferences.setAccessToken(ACCESS_TOKEN_FIRST);
        mTokenPreferences.setAccessToken(ACCESS_TOKEN_SECOND, CUSTOM_ACCESS_TOKEN_KEY);

        assertAccessTokensEqual(ACCESS_TOKEN_FIRST, mAccessTokenManager.getAccessToken());
        assertAccessTokensEqual(ACCESS_TOKEN_SECOND, mAccessTokenManager.getAccessToken(CUSTOM_ACCESS_TOKEN_KEY));
    }

    @Test
    public void removeAccessToken_whenNoDefaultTokenStored_shouldSucceed() {
        mAccessTokenManager.removeAccessToken();
        assertNull(mTokenPreferences.getAccessToken());
        verify(mCookieUtils, times(1)).clearUberCookies();
    }

    @Test
    public void removeAccessToken_whenNoCustomTokenStored_shouldSucceed() {
        mAccessTokenManager.removeAccessToken(CUSTOM_ACCESS_TOKEN_KEY);
        assertNull(mTokenPreferences.getAccessToken(CUSTOM_ACCESS_TOKEN_KEY));
        verify(mCookieUtils, times(1)).clearUberCookies();
    }

    @Test
    public void removeAccessToken_whenTokenWithDefaultTagStored_shouldSucceed() throws JSONException {
        mTokenPreferences.setAccessToken(ACCESS_TOKEN_FIRST);

        mAccessTokenManager.removeAccessToken();
        assertNull(mTokenPreferences.getAccessToken());
        verify(mCookieUtils, times(1)).clearUberCookies();
    }

    @Test
    public void removeAccessToken_whenTokenWithCustomTagStored_shouldSucceed() throws JSONException {
        mTokenPreferences.setAccessToken(ACCESS_TOKEN_FIRST, CUSTOM_ACCESS_TOKEN_KEY);

        mAccessTokenManager.removeAccessToken(CUSTOM_ACCESS_TOKEN_KEY);
        assertNull(mTokenPreferences.getAccessToken(CUSTOM_ACCESS_TOKEN_KEY));
        verify(mCookieUtils, times(1)).clearUberCookies();
    }

    @Test
    public void removeAccessToken_whenMultipleTokensStoredAndOnlyDefaultRemoved_shouldOnlyRemoveDefaultToken()
            throws JSONException {
        mTokenPreferences.setAccessToken(ACCESS_TOKEN_FIRST);
        mTokenPreferences.setAccessToken(ACCESS_TOKEN_SECOND, CUSTOM_ACCESS_TOKEN_KEY);

        mAccessTokenManager.removeAccessToken();
        assertNull(mTokenPreferences.getAccessToken());
        assertAccessTokensEqual(ACCESS_TOKEN_SECOND, mTokenPreferences.getAccessToken(CUSTOM_ACCESS_TOKEN_KEY));
        verify(mCookieUtils, times(1)).clearUberCookies();
    }

    @Test
    public void removeAccessToken_whenMultipleTokensStoredAndOnlyCustomRemoved_shouldOnlyRemoveCustomToken()
            throws JSONException {
        mTokenPreferences.setAccessToken(ACCESS_TOKEN_FIRST);
        mTokenPreferences.setAccessToken(ACCESS_TOKEN_SECOND, CUSTOM_ACCESS_TOKEN_KEY);

        mAccessTokenManager.removeAccessToken(CUSTOM_ACCESS_TOKEN_KEY);
        assertAccessTokensEqual(ACCESS_TOKEN_FIRST, mTokenPreferences.getAccessToken());
        assertNull(mTokenPreferences.getAccessToken(CUSTOM_ACCESS_TOKEN_KEY));
        verify(mCookieUtils, times(1)).clearUberCookies();
    }

    @Test
    public void setAccessToken_whenNoDefaultTokenStored_shouldSucceed() throws JSONException {
        mAccessTokenManager.setAccessToken(ACCESS_TOKEN_FIRST);

        assertAccessTokensEqual(ACCESS_TOKEN_FIRST, mAccessTokenManager.getAccessToken());
    }

    @Test
    public void setAccessToken_whenNoCustomTokenStored_shouldSucceed() throws JSONException {
        mAccessTokenManager.setAccessToken(ACCESS_TOKEN_FIRST, CUSTOM_ACCESS_TOKEN_KEY);

        assertNull(mTokenPreferences.getAccessToken());
        assertAccessTokensEqual(ACCESS_TOKEN_FIRST, mTokenPreferences.getAccessToken(CUSTOM_ACCESS_TOKEN_KEY));
    }

    @Test
    public void setAccessToken_whenDefaultTokenAlreadyStored_shouldOverwrite() throws JSONException {
        mTokenPreferences.setAccessToken(ACCESS_TOKEN_FIRST);

        mAccessTokenManager.setAccessToken(ACCESS_TOKEN_SECOND);
        assertAccessTokensEqual(ACCESS_TOKEN_SECOND, mTokenPreferences.getAccessToken());
    }

    @Test
    public void setAccessToken_whenCustomTokenAlreadyStored_shouldOverwrite() throws JSONException {
        mTokenPreferences.setAccessToken(ACCESS_TOKEN_FIRST, CUSTOM_ACCESS_TOKEN_KEY);

        mAccessTokenManager.setAccessToken(ACCESS_TOKEN_SECOND, CUSTOM_ACCESS_TOKEN_KEY);
        assertAccessTokensEqual(ACCESS_TOKEN_SECOND, mTokenPreferences.getAccessToken(CUSTOM_ACCESS_TOKEN_KEY));
    }

    @Test
    public void setAccessToken_whenBothDefaultAndCustomTokenSet_shouldSucceed() throws JSONException {
        mAccessTokenManager.setAccessToken(ACCESS_TOKEN_FIRST);
        mAccessTokenManager.setAccessToken(ACCESS_TOKEN_SECOND, CUSTOM_ACCESS_TOKEN_KEY);

        assertAccessTokensEqual(ACCESS_TOKEN_FIRST, mTokenPreferences.getAccessToken());
        assertAccessTokensEqual(ACCESS_TOKEN_SECOND, mTokenPreferences.getAccessToken(CUSTOM_ACCESS_TOKEN_KEY));
    }

    private void assertAccessTokensEqual(AccessToken accessTokenExpected, AccessToken accessTokenActual) {
        assertEquals(accessTokenExpected.getExpirationTime().getTime(),
                accessTokenActual.getExpirationTime().getTime());
        assertEquals(accessTokenExpected.getToken(), accessTokenActual.getToken());
        assertTrue(accessTokenActual.getScopes().containsAll(accessTokenExpected.getScopes()));
        assertEquals(accessTokenExpected.getScopes().size(), accessTokenActual.getScopes().size());
    }
}
