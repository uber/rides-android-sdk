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

import androidx.browser.customtabs.CustomTabsIntent;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.uber.sdk.android.core.RobolectricTestBase;
import com.uber.sdk.android.core.SupportedAppType;
import com.uber.sdk.android.core.utils.CustomTabsHelper;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.core.client.SessionConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowWebView;

import java.util.ArrayList;
import java.util.Set;

import static com.uber.sdk.android.core.SupportedAppType.UBER;
import static com.uber.sdk.android.core.SupportedAppType.UBER_EATS;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static org.robolectric.Shadows.shadowOf;

/**
 * Tests {@link LoginActivity}
 */
public class LoginActivityTest extends RobolectricTestBase {

    private static final String REDIRECT_URI = "localHost1234";
    private static final String CLIENT_ID = "clientId1234";
    private static final String CODE = "auth123Code";
    private static final Set<Scope> GENERAL_SCOPES = Sets.newHashSet(Scope.HISTORY, Scope.PROFILE);
    private static final Set<Scope> MIXED_SCOPES = Sets.newHashSet(Scope.REQUEST, Scope.PROFILE, Scope.PAYMENT_METHODS);
    private static final String SIGNUP_DEEPLINK_URL =  "https://m.uber.com/sign-up?client_id=" + CLIENT_ID + "&user-agent=" + LoginActivity.USER_AGENT;

    private final ArrayList<SupportedAppType> productPriority = new ArrayList<>(ImmutableList.of(UBER_EATS, UBER));
    private LoginActivity loginActivity;
    private SessionConfiguration loginConfiguration;

    @Mock
    SsoDeeplink ssoDeeplink;

    @Mock
    SsoDeeplinkFactory ssoDeeplinkFactory;

    @Mock
    CustomTabsHelper customTabsHelper;

    @Before
    public void setup() {
        loginConfiguration = new SessionConfiguration.Builder()
                .setClientId(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .setScopes(GENERAL_SCOPES)
                .build();

        Intent data = LoginActivity.newIntent(Robolectric.setupActivity(Activity.class), loginConfiguration, ResponseType.TOKEN);
        loginActivity = Robolectric.buildActivity(LoginActivity.class, data).get();

        when(ssoDeeplinkFactory.getSsoDeeplink(any(LoginActivity.class),
                eq(productPriority), any(SessionConfiguration.class))).thenReturn(ssoDeeplink);
    }

    @Test
    public void onLoginLoad_withEmptySessionConfiguration_shouldReturnErrorResultIntent() {
        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class);
        controller.create();

        ShadowActivity shadowActivity = shadowOf(controller.get());

        assertThat(shadowActivity.getResultCode()).isEqualTo(Activity.RESULT_CANCELED);
        assertThat(shadowActivity.getResultIntent()).isNotNull();
        assertThat(getErrorFromIntent(shadowActivity.getResultIntent()))
                .isEqualTo(AuthenticationError.INVALID_PARAMETERS);
        assertThat(shadowActivity.isFinishing()).isTrue();
    }

    @Test
    public void onLoginLoad_withEmptyScopes_shouldReturnErrorResultIntent() {
        Intent intent = new Intent();
        intent.putExtra(LoginActivity.EXTRA_SESSION_CONFIGURATION, new SessionConfiguration.Builder().setClientId(CLIENT_ID).build());

        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class, intent)
                .create();

        ShadowActivity shadowActivity = shadowOf(controller.get());

        assertThat(shadowActivity.getResultCode()).isEqualTo(Activity.RESULT_CANCELED);
        assertThat(shadowActivity.getResultIntent()).isNotNull();
        assertThat(getErrorFromIntent(shadowActivity.getResultIntent()))
                .isEqualTo(AuthenticationError.INVALID_SCOPE);
        assertThat(shadowActivity.isFinishing()).isTrue();
    }

    @Test
    public void onLoginLoad_withNullResponseType_shouldReturnErrorResultIntent() {
        Intent intent = new Intent();
        intent.putExtra(LoginActivity.EXTRA_SESSION_CONFIGURATION, loginConfiguration);
        intent.putExtra(LoginActivity.EXTRA_RESPONSE_TYPE, (ResponseType) null);

        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class)
                .newIntent(intent)
                .create();

        ShadowActivity shadowActivity = shadowOf(controller.get());

        assertThat(shadowActivity.getResultCode()).isEqualTo(Activity.RESULT_CANCELED);
        assertThat(shadowActivity.getResultIntent()).isNotNull();
        assertThat(getErrorFromIntent(shadowActivity.getResultIntent()))
                .isEqualTo(AuthenticationError.INVALID_RESPONSE_TYPE);
        assertThat(shadowActivity.isFinishing()).isTrue();
    }

    @Test
    public void onLoginLoad_withSsoEnabled_andSupported_shouldExecuteSsoDeeplink() {
        Intent intent = LoginActivity.newIntent(Robolectric.setupActivity(Activity.class), productPriority,
                loginConfiguration, ResponseType.TOKEN, false, true, true);

        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class, intent);
        loginActivity = controller.get();
        loginActivity.ssoDeeplinkFactory = ssoDeeplinkFactory;

        when(ssoDeeplink.isSupported(SsoDeeplink.FlowVersion.REDIRECT_TO_SDK)).thenReturn(true);

        controller.create();

        verify(ssoDeeplink).execute(SsoDeeplink.FlowVersion.REDIRECT_TO_SDK);
    }

    @Test
    public void onLoginLoad_withSsoEnabled_andNotSupported_shouldReturnErrorResultIntent() {
        Intent intent = LoginActivity.newIntent(Robolectric.setupActivity(Activity.class), productPriority,
                loginConfiguration, ResponseType.TOKEN, false, true, true);

        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class).newIntent(intent);
        loginActivity = controller.get();
        loginActivity.ssoDeeplinkFactory = ssoDeeplinkFactory;
        ShadowActivity shadowActivity = shadowOf(loginActivity);
        when(ssoDeeplink.isSupported(SsoDeeplink.FlowVersion.REDIRECT_TO_SDK)).thenReturn(false);

        controller.create();

        assertThat(shadowActivity.getResultCode()).isEqualTo(Activity.RESULT_CANCELED);
        assertThat(shadowActivity.getResultIntent()).isNotNull();
        assertThat(getErrorFromIntent(shadowActivity.getResultIntent()))
                .isEqualTo(AuthenticationError.INVALID_REDIRECT_URI);
        assertThat(shadowActivity.isFinishing()).isTrue();
    }

    @Test
    public void onLoginLoad_withResponseTypeCode_andForceWebview_shouldLoadWebview() {
        Intent intent = LoginActivity.newIntent(Robolectric.setupActivity(Activity.class), loginConfiguration,
                ResponseType.CODE, true);

        loginActivity = Robolectric.buildActivity(LoginActivity.class, intent).create().get();
        ShadowWebView webview = Shadows.shadowOf(loginActivity.webView);

        String expectedUrl = AuthUtils.buildUrl(REDIRECT_URI, ResponseType.CODE, loginConfiguration);
        assertThat(webview.getLastLoadedUrl()).isEqualTo(expectedUrl);
    }

    @Test
    public void onLoginLoad_withResponseTypeCode_andNotForceWebview_shouldLoadChrometab() {
        Intent intent = LoginActivity.newIntent(Robolectric.setupActivity(Activity.class), loginConfiguration,
                ResponseType.CODE, false);

        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class, intent);
        loginActivity = controller.get();
        loginActivity.customTabsHelper = customTabsHelper;
        controller.create();

        String expectedUrl = AuthUtils.buildUrl(REDIRECT_URI, ResponseType.CODE, loginConfiguration);
        verify(customTabsHelper).openCustomTab(any(LoginActivity.class), any(CustomTabsIntent.class),
                eq(Uri.parse(expectedUrl)), any(CustomTabsHelper.BrowserFallback.class));
    }

    @Test
    public void onLoginLoad_withResponseTypeToken_andForceWebview_andGeneralScopes_shouldLoadWebview() {
        Intent intent = LoginActivity.newIntent(Robolectric.setupActivity(Activity.class), loginConfiguration,
                ResponseType.TOKEN, true);

        loginActivity = Robolectric.buildActivity(LoginActivity.class, intent).create().get();
        ShadowWebView webview = Shadows.shadowOf(loginActivity.webView);

        String expectedUrl = AuthUtils.buildUrl(REDIRECT_URI, ResponseType.TOKEN, loginConfiguration);
        assertThat(webview.getLastLoadedUrl()).isEqualTo(expectedUrl);
    }

    @Test
    public void onLoginLoad_withResponseTypeToken_andNotForceWebview_andGeneralScopes_shouldLoadChrometab() {
        Intent intent = LoginActivity.newIntent(Robolectric.setupActivity(Activity.class), loginConfiguration,
                ResponseType.TOKEN, false);

        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class).newIntent(intent);
        loginActivity = controller.get();
        loginActivity.customTabsHelper = customTabsHelper;
        controller.create();

        String expectedUrl = AuthUtils.buildUrl(REDIRECT_URI, ResponseType.TOKEN, loginConfiguration);
        verify(customTabsHelper).openCustomTab(any(LoginActivity.class), any(CustomTabsIntent.class),
                eq(Uri.parse(expectedUrl)), any(CustomTabsHelper.BrowserFallback.class));
    }

    @Test
    public void onLoginLoad_withResponseTypeToken_andForceWebview_andPrivilegedScopes_andRedirectToPlayStoreDisabled_shouldLoadWebview() {
        loginConfiguration = new SessionConfiguration.Builder()
                .setClientId(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .setScopes(MIXED_SCOPES)
                .build();
        Intent intent = LoginActivity.newIntent(Robolectric.setupActivity(Activity.class), loginConfiguration,
                ResponseType.TOKEN, true);

        loginActivity = Robolectric.buildActivity(LoginActivity.class, intent).create().get();
        ShadowWebView webview = Shadows.shadowOf(loginActivity.webView);

        String expectedUrl = AuthUtils.buildUrl(REDIRECT_URI, ResponseType.TOKEN, loginConfiguration);
        assertThat(webview.getLastLoadedUrl()).isEqualTo(expectedUrl);
    }

    @Test
    public void onLoginLoad_withResponseTypeToken_andNotForceWebview_andPrivilegedScopes_andRedirectToPlayStoreDisabled_shouldLoadChrometab() {
        loginConfiguration = new SessionConfiguration.Builder()
                .setClientId(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .setScopes(MIXED_SCOPES)
                .build();
        Intent intent = LoginActivity.newIntent(Robolectric.setupActivity(Activity.class), loginConfiguration,
                ResponseType.TOKEN, false);

        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class, intent);
        loginActivity = controller.get();
        loginActivity.customTabsHelper = customTabsHelper;
        controller.create();

        String expectedUrl = AuthUtils.buildUrl(REDIRECT_URI, ResponseType.TOKEN, loginConfiguration);
        verify(customTabsHelper).openCustomTab(any(LoginActivity.class), any(CustomTabsIntent.class),
                eq(Uri.parse(expectedUrl)), any(CustomTabsHelper.BrowserFallback.class));
    }

    @Test
    public void onLoginLoad_withResponseTypeToken_andPrivilegedScopes_andRedirectToPlayStoreEnabled_shouldRedirectToPlayStore() {
        loginConfiguration = new SessionConfiguration.Builder()
                .setClientId(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .setScopes(MIXED_SCOPES)
                .build();
        Intent intent = LoginActivity.newIntent(Robolectric.setupActivity(Activity.class),
                new ArrayList<SupportedAppType>(), loginConfiguration, ResponseType.TOKEN, true, false, true);

        ShadowActivity shadowActivity = shadowOf(Robolectric.buildActivity(LoginActivity.class, intent).create().get());

        final Intent signupDeeplinkIntent = shadowActivity.peekNextStartedActivity();
        assertThat(signupDeeplinkIntent.getData().toString()).isEqualTo(SIGNUP_DEEPLINK_URL);
    }

    @Test
    public void onTokenReceived_shouldReturnAccessTokenResult() {
        String tokenString = "accessToken1234";

        String redirectUrl = REDIRECT_URI + "?access_token=accessToken1234&expires_in=" + 2592000
                + "&scope=history&refresh_token=refreshToken1234&token_type=bearer";

        ShadowActivity shadowActivity = shadowOf(loginActivity);
        loginActivity.onTokenReceived(Uri.parse(redirectUrl));

        assertNotNull(shadowActivity.getResultIntent());
        assertEquals(shadowActivity.getResultCode(), Activity.RESULT_OK);

        AccessToken resultAccessToken =
                AuthUtils.createAccessToken(shadowActivity.getResultIntent());

        assertEquals(resultAccessToken.getExpiresIn(), 2592000);
        assertEquals(resultAccessToken.getToken(), tokenString);
        assertEquals(resultAccessToken.getScopes().size(), 1);
        assertTrue(resultAccessToken.getScopes().contains(Scope.HISTORY));
        assertThat(shadowActivity.isFinishing()).isTrue();
    }

    @Test
    public void onError_shouldReturnErrorResultIntent() {
        ShadowActivity shadowActivity = shadowOf(loginActivity);
        loginActivity.onError(AuthenticationError.MISMATCHING_REDIRECT_URI);

        assertThat(shadowActivity.getResultIntent()).isNotNull();

        assertThat(shadowActivity.getResultCode()).isEqualTo(Activity.RESULT_CANCELED);
        assertThat(getErrorFromIntent(shadowActivity.getResultIntent()))
                .isEqualTo(AuthenticationError.MISMATCHING_REDIRECT_URI);
        assertThat(shadowActivity.isFinishing()).isTrue();
    }

    @Test
    public void onCodeReceived_shouldReturnResultIntentWithCode() {
        ShadowActivity shadowActivity = shadowOf(loginActivity);

        String redirectUrl = REDIRECT_URI + "?code=" + CODE;

        loginActivity.onCodeReceived(Uri.parse(redirectUrl));

        assertThat(shadowActivity.getResultIntent()).isNotNull();

        assertThat(shadowActivity.getResultCode()).isEqualTo(Activity.RESULT_OK);
        assertThat(shadowActivity.getResultIntent().getStringExtra(LoginManager.EXTRA_CODE_RECEIVED)).isEqualTo(CODE);
        assertThat(shadowActivity.isFinishing()).isTrue();
    }

    private AuthenticationError getErrorFromIntent(Intent intent) {
        return AuthenticationError.fromString(intent.getStringExtra(LoginManager.EXTRA_ERROR));
    }
}
