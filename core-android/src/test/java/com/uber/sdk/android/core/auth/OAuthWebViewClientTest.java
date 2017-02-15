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
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;

import com.uber.sdk.android.core.RobolectricTestBase;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.SessionConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.util.ActivityController;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import static junit.framework.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class OAuthWebViewClientTest extends RobolectricTestBase {

    private static final String ACCESS_TOKEN_STRING = "accessToken1234";
    private static final long EXPIRATION_TIME = 1458770906206l;
    private static final String REDIRECT_URI = "http://localhost:1234";

    LoginActivity.OAuthWebViewClient client;
    TestLoginActivity testLoginActivity;

    @Before
    public void setUp() {
        testLoginActivity = spy(new TestLoginActivity());
        client = testLoginActivity.new AccessTokenClient(REDIRECT_URI);
    }

    @Test
    public void onBuildUrl_withDefaultRegion_shouldHaveDefaultUberDomain() {
        String clientId = "clientId1234";
        String redirectUri = "localHost1234";

        SessionConfiguration loginConfiguration = new SessionConfiguration.Builder()
                .setRedirectUri(redirectUri)
                .setScopes(Arrays.asList(Scope.HISTORY))
                .setClientId(clientId)
                .build();

        String url = testLoginActivity.buildUrl(redirectUri, ResponseType.TOKEN, loginConfiguration);
        assertEquals("https://login.uber.com/oauth/v2/authorize?client_id=" + clientId +
                "&redirect_uri=" + redirectUri + "&response_type=token&scope=history&" +
                "show_fb=false&signup_params=eyJyZWRpcmVjdF90b19sb2dpbiI6dHJ1ZX0%3D%0A", url);
    }

    @Test
    public void onLoadLoginView_withNoRedirectUrl_shouldReturnError() {
        SessionConfiguration config = new SessionConfiguration.Builder().setClientId("clientId").build();
        Intent intent = new Intent();
        intent.putExtra(LoginActivity.EXTRA_SESSION_CONFIGURATION, config);
        final ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class)
                .withIntent(intent);
        final ShadowActivity shadowActivity = Shadows.shadowOf(controller.get());

        controller.create();

        assertThat(shadowActivity.isFinishing()).isTrue();
        assertThat(shadowActivity.getResultCode()).isEqualTo(Activity.RESULT_CANCELED);
        assertThat(shadowActivity.getResultIntent()).isNotNull();
        assertThat(shadowActivity.getResultIntent().getStringExtra(LoginManager.EXTRA_ERROR)).isNotEmpty();
    }

    @Test
    public void onLoadUrl_withValidAccessToken_redirectUriShouldBeSent() {
        final String fragment = "#access_token=" + ACCESS_TOKEN_STRING + "&expires_in=" + EXPIRATION_TIME +
                "&scope=history";
        final String redirectUrl = REDIRECT_URI + fragment;

        Collection<Scope> scopes = new HashSet<>();
        scopes.add(Scope.HISTORY);

        client.shouldOverrideUrlLoading(mock(WebView.class), redirectUrl);

        ArgumentCaptor<Uri> uriCaptor = ArgumentCaptor.forClass(Uri.class);
        verify(testLoginActivity).onTokenReceived(uriCaptor.capture());

        assertThat(uriCaptor.getValue().toString()).isEqualTo(fragment.replaceFirst("#", "?"));
    }

    @Test
    public void onLoadUrl_withEmptyAccessToken_shouldGetAccessTokenError() {
        String redirectUrl = REDIRECT_URI + "#access_token=&token_type=bearer";
        client.shouldOverrideUrlLoading(mock(WebView.class), redirectUrl);
        verify(testLoginActivity).onError(AuthenticationError.INVALID_RESPONSE);
    }

    @Test
    public void onLoadUrl_withNullFragment_shouldGetAccessTokenError() {
        client.shouldOverrideUrlLoading(mock(WebView.class), REDIRECT_URI);
        verify(testLoginActivity).onError(AuthenticationError.INVALID_RESPONSE);
    }

    @Test
    public void onLoadUrl_withIncompleteAccessToken_shouldGetAccessTokenError() {
        String redirectUrl = REDIRECT_URI + "#access_token=accessToken1234&scope=all_trips";
        client.shouldOverrideUrlLoading(mock(WebView.class), redirectUrl);
        verify(testLoginActivity).onError(AuthenticationError.INVALID_RESPONSE);
    }

    @Test
    public void onLoadUrl_withErrorQueryParam_shouldReturnError() {
        String redirectUrl = REDIRECT_URI + "?error=invalid_scope#_";
        client.shouldOverrideUrlLoading(mock(WebView.class), redirectUrl);

        verify(testLoginActivity).onError(AuthenticationError.INVALID_SCOPE);
    }

    @Test
    public void onLoadUrl_withErrorFragment_shouldReturnError() {
        String redirectUrl = REDIRECT_URI + "#error=invalid_scope";
        client.shouldOverrideUrlLoading(mock(WebView.class), redirectUrl);

        verify(testLoginActivity).onError(AuthenticationError.INVALID_SCOPE);
    }

    @Test
    public void onLoadUrl_withMismatchingRedirectUri_shouldReturnError() {
        String redirectUrl = "http://login.uber.com/oauth/errors?error=mismatching_redirect_uri#_";

        client.shouldOverrideUrlLoading(mock(WebView.class), redirectUrl);

        verify(testLoginActivity).onError(AuthenticationError.MISMATCHING_REDIRECT_URI);
    }

    @Test
    public void onLoadUrl_withNotRedirectUrl_shouldIgnore() {
        String redirectUrl = "https://login.uber.com";
        client.shouldOverrideUrlLoading(mock(WebView.class), redirectUrl);

        verify(testLoginActivity, never()).onError(any(AuthenticationError.class));
        verify(testLoginActivity, never()).onCodeReceived(any(Uri.class));
        verify(testLoginActivity, never()).onTokenReceived(any(Uri.class));
    }

    @Test
    public void onPageFinished_withMatchingRedirectUri_shouldReturnedUri() {
        client = testLoginActivity.new AuthorizationCodeClient(REDIRECT_URI);

        String redirectUrl = REDIRECT_URI + "code=myCode123";

        client.onPageFinished(mock(WebView.class), redirectUrl);

        verify(testLoginActivity).onCodeReceived(eq(Uri.parse(redirectUrl)));
    }

    class TestLoginActivity extends LoginActivity {

        @Override
        protected OAuthWebViewClient createOAuthClient(String redirectUri) {
            return client;
        }
    }
}
