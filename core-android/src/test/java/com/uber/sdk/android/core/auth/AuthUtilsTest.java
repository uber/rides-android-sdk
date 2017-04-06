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

import android.net.Uri;

import com.uber.sdk.android.core.RobolectricTestBase;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.Scope;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthUtilsTest extends RobolectricTestBase {

    private static final String AUTH_CODE = "auth123Code";
    private static final String BEARER = "Bearer";

    private final String ACCESS_TOKEN_STRING = "accessToken1234";
    private final long EXPIRATION_TIME = 1458770906206l;

    @Test
    public void stringToScopeCollection_whenOneScopeInString_shouldReturnCollectionOfOneScope() {
        String scopeString = "history";
        List<Scope> scopes = new ArrayList<>();
        scopes.addAll(AuthUtils.stringToScopeCollection(scopeString));

        assertEquals(scopes.size(), 1);
        assertTrue(scopes.contains(Scope.HISTORY));
    }

    @Test
    public void stringToScopeCollection_whenCollectionOfStrings_shouldReturnCollectionOfScopes() {
        String scopeString = "profile history";
        List<Scope> scopes = new ArrayList<>();
        scopes.addAll(AuthUtils.stringToScopeCollection(scopeString));

        assertTrue(scopes.contains(Scope.PROFILE));
        assertTrue(scopes.contains(Scope.HISTORY));
    }

    @Test
    public void stringToScopeCollection_whenEmptyScopeString_shouldReturnEmptyScopeCollection() {
        String scopeString = "";
        List<Scope> scopes = new ArrayList<>();
        scopes.addAll(AuthUtils.stringToScopeCollection(scopeString));

        assertEquals(scopes.size(), 0);
    }

    @Test
    public void stringToScopeCollection_whenUnknownScopeInString_shouldReturnOnlyKnownScopes() {
        String scopeString = "profile custom";
        List<Scope> scopes = new ArrayList<>();
        scopes.addAll(AuthUtils.stringToScopeCollection(scopeString));

        assertEquals(scopes.size(), 1);
        assertTrue(scopes.contains(Scope.PROFILE));
    }

    @Test
    public void scopeCollectionToString_withMultipleScopes_shouldReturnSpaceDelimitedStringScopes() {
        List<Scope> scopes = new ArrayList<Scope>();
        scopes.add(Scope.PROFILE);
        scopes.add(Scope.HISTORY);
        String result = AuthUtils.scopeCollectionToString(scopes);

        assertEquals(result, "profile history");
    }

    @Test
    public void scopeCollectionToString_withOneScope_shouldReturnStringWithOneScope() {
        List<Scope> scopes = new ArrayList<Scope>();
        scopes.add(Scope.HISTORY);
        String result = AuthUtils.scopeCollectionToString(scopes);

        assertEquals(result, "history");
    }

    @Test
    public void scopeCollectionToString_whenEmptyScopeCollection_shouldReturnEmptyString() {
        List<Scope> scopes = new ArrayList<Scope>();
        String result = AuthUtils.scopeCollectionToString(scopes);

        assertTrue(result.isEmpty());
    }

    @Test
    public void generateAccessTokenFromUrl_whenNullFragment_shouldThrowInvalidResponseError() {
        String redirectUri = "http://localhost:1234/";
        try {
            AuthUtils.parseTokenUri(Uri.parse(redirectUri));
            fail("Should throw an exception");
        } catch (LoginAuthenticationException e) {
            assertEquals(AuthenticationError.INVALID_RESPONSE, e.getAuthenticationError());
        }
    }

    @Test
    public void generateAccessTokenFromUrl_whenValidErrorInQueryParameter_shouldThrowAuthenticationError() {
        String redirectUri = "http://localhost:1234?error=mismatching_redirect_uri";
        try {
            AuthUtils.parseTokenUri(Uri.parse(redirectUri));
            fail("Should throw an exception");
        } catch (LoginAuthenticationException e) {
            assertEquals(AuthenticationError.INVALID_RESPONSE, e.getAuthenticationError());
        }
    }

    @Test
    public void generateAccessTokenFromUrl_whenInvalidErrorInQueryParameter_shouldThrowAuthenticationError() {
        String redirectUri = "http://localhost:1234?error=bogus_error";
        try {
            AuthUtils.parseTokenUri(Uri.parse(redirectUri));
            fail("Should throw an exception");
        } catch (LoginAuthenticationException e) {
            assertEquals(AuthenticationError.INVALID_RESPONSE, e.getAuthenticationError());
        }
    }

    @Test
    public void generateAccessTokenFromUrl_whenNoToken_shouldThrowAuthenticationError() {
        String redirectUrl = "http://localhost:1234?expires_in=" + EXPIRATION_TIME + "&scope=history";
        try {
            AuthUtils.parseTokenUri(Uri.parse(redirectUrl));
            fail("Should throw an exception");
        } catch (LoginAuthenticationException e) {
            assertEquals(AuthenticationError.INVALID_RESPONSE, e.getAuthenticationError());
        }
    }

    @Test
    public void generateAccessTokenFromUrl_whenNoExpirationTime_shouldThrowAuthenticationError() {
        String redirectUrl = "http://localhost:1234?access_token=" + ACCESS_TOKEN_STRING + "&scope=history";
        try {
            AuthUtils.parseTokenUri(Uri.parse(redirectUrl));
            fail("Should throw an exception");
        } catch (LoginAuthenticationException e) {
            assertEquals(AuthenticationError.INVALID_RESPONSE, e.getAuthenticationError());
        }
    }

    @Test
    public void generateAccessTokenFromUrl_whenNoScopes_shouldThrowAuthenticationError() {
        String redirectUrl = "http://localhost:1234?access_token=" + ACCESS_TOKEN_STRING
                + "&expires_in=" + EXPIRATION_TIME;
        try {
            AuthUtils.parseTokenUri(Uri.parse(redirectUrl));
            fail("Should throw an exception");
        } catch (LoginAuthenticationException e) {
            assertEquals(AuthenticationError.INVALID_RESPONSE, e.getAuthenticationError());
        }
    }

    @Test
    public void generateAccessTokenFromUrl_whenBadExpirationTime_shouldThrowAuthenticationError() {
        String redirectUrl = "http://localhost:1234?access_token=" + ACCESS_TOKEN_STRING + "&expires_in=notALong"
                + "&scope=history";
        try {
            AuthUtils.parseTokenUri(Uri.parse(redirectUrl));
            fail("Should throw an exception");
        } catch (LoginAuthenticationException e) {
            assertEquals(AuthenticationError.INVALID_RESPONSE, e.getAuthenticationError());
        }
    }

    @Test
    public void generateAccessTokenFromUrl_whenBadScopes_shouldThrowAuthenticationError() {
        String redirectUrl = "http://localhost:1234?access_token=" + ACCESS_TOKEN_STRING + "&expires_in=" +
                EXPIRATION_TIME
                + "&scope=history notAScopeAtAll";
        try {
            AuthUtils.parseTokenUri(Uri.parse(redirectUrl));
            fail("Should throw an exception");
        } catch (LoginAuthenticationException e) {
            assertEquals(AuthenticationError.INVALID_RESPONSE, e.getAuthenticationError());
        }
    }

    @Test
    public void generateAccessTokenFromUrl_whenValidAccessTokenWithOneScope_shouldGenerateValidAccessToken()
            throws LoginAuthenticationException {
        String redirectUrl = "http://localhost:1234?access_token=" + ACCESS_TOKEN_STRING
                + "&expires_in=" + EXPIRATION_TIME + "&scope=history" + "&token_type=" + BEARER;

        AccessToken accessToken = AuthUtils.createAccessToken(AuthUtils.parseTokenUri(Uri.parse(redirectUrl)));
        assertNotNull(accessToken);
        assertEquals(accessToken.getToken(), ACCESS_TOKEN_STRING);
        assertEquals(accessToken.getScopes().size(), 1);
        assertTrue(accessToken.getScopes().contains(Scope.HISTORY));
        assertEquals(accessToken.getExpiresIn(), EXPIRATION_TIME);
    }

    @Test
    public void generateAccessTokenFromUrl_whenValidAccessTokenWithMultipleScopes_shouldGenerateValidAccessToken()
            throws LoginAuthenticationException {
        String redirectUrl = "http://localhost:1234?access_token=" + ACCESS_TOKEN_STRING
                + "&expires_in=" + EXPIRATION_TIME + "&scope=history profile" + "&token_type=" + BEARER;

        AccessToken accessToken = AuthUtils.createAccessToken(AuthUtils.parseTokenUri(Uri.parse(redirectUrl)));
        assertNotNull(accessToken);
        assertEquals(accessToken.getToken(), ACCESS_TOKEN_STRING);
        assertEquals(accessToken.getScopes().size(), 2);
        assertTrue(accessToken.getScopes().contains(Scope.HISTORY));
        assertTrue(accessToken.getScopes().contains(Scope.PROFILE));
        assertEquals(accessToken.getExpiresIn(), EXPIRATION_TIME);
        assertThat(accessToken.getTokenType()).isEqualTo(BEARER);
    }

    @Test
    public void getCodeFromUrl_whenValidAuthorizationCodePassed() throws LoginAuthenticationException {
        String redirectUrl = "http://localhost:1234?code=" + AUTH_CODE;

        assertThat(AuthUtils.parseAuthorizationCode(Uri.parse(redirectUrl))).isEqualTo(AUTH_CODE);
    }

    @Test
    public void getCodeFromUrl_whenNoValidAuthorizationCodePassed() throws LoginAuthenticationException {
        String redirectUrl = "http://localhost:1234?access_token=" + ACCESS_TOKEN_STRING
                + "&expires_in=" + EXPIRATION_TIME + "&scope=history";


        try {
            AuthUtils.parseAuthorizationCode(Uri.parse(redirectUrl));
            fail("Should throw an exception");
        } catch (LoginAuthenticationException e) {
            assertThat(e.getAuthenticationError()).isEqualTo(AuthenticationError.INVALID_RESPONSE);
        }
    }

    @Test
    public void testCreateEncodedParam() {
        assertThat(AuthUtils.createEncodedParam("{\"redirect_to_login\":true}")).isEqualTo("eyJyZWRpcmVjdF90b19sb2dpbiI6dHJ1ZX0=\n");
    }
}
