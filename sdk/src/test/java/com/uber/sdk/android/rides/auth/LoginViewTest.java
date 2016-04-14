package com.uber.sdk.android.rides.auth;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.WebView;

import com.google.common.collect.ImmutableList;
import com.uber.sdk.android.rides.RobolectricTestBase;
import com.uber.sdk.android.rides.SdkPreferences;
import com.uber.sdk.android.rides.UberSdk;
import com.uber.sdk.android.rides.UberSdkAccessor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LoginViewTest extends RobolectricTestBase {
    private static final String ACCESS_TOKEN_STRING = "accessToken1234";
    private static final long EXPIRATION_TIME = 1458770906206l;
    private static final String REDIRECT_URI = "http://localhost:1234";

    private LoginCallback callback;
    private LoginView.OAuthWebViewClient client;

    @Before
    public void setUp() {
        callback = mock(LoginCallback.class);
        client = new LoginView(Robolectric.setupActivity(Activity.class)).new OAuthWebViewClient(REDIRECT_URI,
                callback);
    }

    @Test
    public void onBuildUrl_withDefaultRegion_shouldHaveDefaultUberDomain() {
        String clientId = "clientId1234";
        UberSdk.initialize(RuntimeEnvironment.application, clientId);

        String redirectUri = "localHost1234";
        String url = LoginView.buildUrl(redirectUri, ImmutableList.of(Scope.HISTORY));
        assertEquals("https://login.uber.com/oauth/v2/authorize?client_id="+clientId+"&redirect_uri="+redirectUri
                + "&response_type=token"
                + "&scope=history&show_fb=false", url);
    }

    @Test
    public void onBuildUrl_withChinaRegion_shouldHaveChinaDomain() {
        String clientId = "clientId1234";
        SdkPreferences sdkPreferences = new SdkPreferences(RuntimeEnvironment.application);
        sdkPreferences.setRegion(UberSdk.Region.CHINA);
        UberSdk.initialize(RuntimeEnvironment.application, clientId);

        String redirectUri = "localHost1234";
        String url = LoginView.buildUrl(redirectUri, ImmutableList.of(Scope.HISTORY));
        assertEquals("https://login.uber.com.cn/oauth/v2/authorize?client_id="+clientId+"&redirect_uri="+redirectUri
                + "&response_type=token"
                + "&scope=history&show_fb=false", url);
    }

    @Test
    public void onLoadLoginView_withNoRedirectUrl_shouldReturnError() {
        UberSdk.initialize(RuntimeEnvironment.application, "clientId");
        LoginView loginView = new LoginView(Robolectric.setupActivity(Activity.class));

        LoginCallback loginCallback = mock(LoginCallback.class);
        loginView.setLoginCallback(loginCallback);
        loginView.load();
        verify(loginCallback, times(1)).onLoginError(AuthenticationError.INVALID_REDIRECT_URI);
    }

    @Test
    public void onLoadUrl_withValidAccessToken_accessTokenShouldBeParsed() {
        String redirectUrl = REDIRECT_URI+"#access_token="+ACCESS_TOKEN_STRING+"&expires_in="+EXPIRATION_TIME+"&scope"
                + "=history";

        Collection<Scope> scopes = new HashSet<>();
        scopes.add(Scope.HISTORY);

        client.shouldOverrideUrlLoading(mock(WebView.class), redirectUrl);

        ArgumentCaptor<AccessToken> argumentCaptor = ArgumentCaptor.forClass(AccessToken.class);
        verify(callback, times(1)).onLoginSuccess(argumentCaptor.capture());

        AccessToken accessTokenCaptured = argumentCaptor.getValue();
        assertEquals(ACCESS_TOKEN_STRING, accessTokenCaptured.getToken());
        assertEquals(new Date(1458770906206l), accessTokenCaptured.getExpirationTime());
        assertEquals(1, accessTokenCaptured.getScopes().size());
        assertTrue(accessTokenCaptured.getScopes().contains(Scope.HISTORY));
    }

    @Test
    public void onLoadUrl_withEmptyAccessToken_shouldGetAccessTokenError() {
        String redirectUrl = REDIRECT_URI+"#access_token=&token_type=bearer";
        client.shouldOverrideUrlLoading(mock(WebView.class), redirectUrl);
        verify(callback, times(1)).onLoginError(AuthenticationError.INVALID_RESPONSE);
    }

    @Test
    public void onLoadUrl_withNullFragment_shouldGetAccessTokenError() {
        client.shouldOverrideUrlLoading(mock(WebView.class), REDIRECT_URI);
        verify(callback, times(1)).onLoginError(AuthenticationError.INVALID_RESPONSE);
    }

    @Test
    public void onLoadUrl_withIncompleteAccessToken_shouldGetAccessTokenError() {
        String redirectUrl = REDIRECT_URI+"#access_token=accessToken1234&scope=all_trips";
        client.shouldOverrideUrlLoading(mock(WebView.class), redirectUrl);
        verify(callback, times(1)).onLoginError(AuthenticationError.INVALID_RESPONSE);
    }

    @Test
    public void onLoadUrl_withRedirectError_shouldReturnError() {
        String redirectUrl = "https://login.uber.com/errors?error=mismatching_redirect_uri";
        client.shouldOverrideUrlLoading(mock(WebView.class), redirectUrl);
        verify(callback, times(1)).onLoginError(AuthenticationError.MISMATCHING_REDIRECT_URI);
    }

    @After
    public void teardown() {
        UberSdkAccessor.clearPrefs();
    }
}
