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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import com.google.common.collect.ImmutableList;
import com.uber.sdk.android.core.RobolectricTestBase;
import com.uber.sdk.android.core.SupportedAppType;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.AccessTokenAuthenticator;
import com.uber.sdk.core.auth.AccessTokenStorage;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.core.client.Session;
import com.uber.sdk.core.client.SessionConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static com.uber.sdk.android.core.SupportedAppType.UBER;
import static com.uber.sdk.android.core.SupportedAppType.UBER_EATS;
import static com.uber.sdk.android.core.auth.LoginActivity.EXTRA_FORCE_WEBVIEW;
import static com.uber.sdk.android.core.auth.LoginActivity.EXTRA_PRODUCT_PRIORITY;
import static com.uber.sdk.android.core.auth.LoginActivity.EXTRA_REDIRECT_TO_PLAY_STORE_ENABLED;
import static com.uber.sdk.android.core.auth.LoginActivity.EXTRA_RESPONSE_TYPE;
import static com.uber.sdk.android.core.auth.LoginActivity.EXTRA_SESSION_CONFIGURATION;
import static com.uber.sdk.android.core.auth.LoginActivity.EXTRA_SSO_ENABLED;
import static com.uber.sdk.android.core.auth.LoginManager.EXTRA_ACCESS_TOKEN;
import static com.uber.sdk.android.core.auth.LoginManager.EXTRA_CODE_RECEIVED;
import static com.uber.sdk.android.core.auth.LoginManager.EXTRA_ERROR;
import static com.uber.sdk.android.core.auth.LoginManager.EXTRA_EXPIRES_IN;
import static com.uber.sdk.android.core.auth.LoginManager.EXTRA_REFRESH_TOKEN;
import static com.uber.sdk.android.core.auth.LoginManager.EXTRA_SCOPE;
import static com.uber.sdk.android.core.auth.LoginManager.EXTRA_TOKEN_TYPE;
import static com.uber.sdk.android.core.auth.LoginManager.REQUEST_CODE_LOGIN_DEFAULT;
import static com.uber.sdk.android.core.auth.SsoDeeplink.FlowVersion.DEFAULT;
import static com.uber.sdk.android.core.auth.SsoDeeplink.FlowVersion.REDIRECT_TO_SDK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class LoginManagerTest extends RobolectricTestBase {
    private static final AccessToken ACCESS_TOKEN = new AccessToken(2592000,
            ImmutableList.of(Scope.PROFILE, Scope.HISTORY), "thisIsAnAccessToken", "refreshToken",
            "tokenType");

    private static final String CLIENT_ID = "Client1234";
    private static final String REDIRECT_URI = "com.example.uberauth://redirect";
    private static final ImmutableList<Scope> MIXED_SCOPES = ImmutableList.of(Scope.PROFILE, Scope.REQUEST_RECEIPT);
    private static final ImmutableList<Scope> GENERAL_SCOPES = ImmutableList.of(Scope.PROFILE, Scope.HISTORY);
    private static final ImmutableList<Scope> EMPTY_SCOPES = ImmutableList.of();
    private static final ImmutableList<String> CUSTOM_SCOPES = ImmutableList.of("foo", "bar");

    private static final String AUTHORIZATION_CODE = "Auth123Code";
    private static final String PACKAGE_NAME = "com.example";

    @Mock
    Activity activity;

    @Mock
    PackageManager packageManager;

    @Mock
    LoginCallback callback;

    @Mock
    AccessTokenStorage accessTokenStorage;

    @Mock
    LegacyUriRedirectHandler legacyUriRedirectHandler;

    @Mock
    SsoDeeplink ssoDeeplink;

    SessionConfiguration sessionConfiguration;

    private LoginManager loginManager;

    @Before
    public void setup() {
        sessionConfiguration = new SessionConfiguration.Builder().setClientId(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
                .setScopes(MIXED_SCOPES).build();
        loginManager = spy(new LoginManager(accessTokenStorage, callback,
                sessionConfiguration, REQUEST_CODE_LOGIN_DEFAULT,
                legacyUriRedirectHandler));

        when(loginManager.getSsoDeeplink(any(Activity.class))).thenReturn(ssoDeeplink);

        when(activity.getPackageManager()).thenReturn(packageManager);
        when(activity.getApplicationInfo()).thenReturn(new ApplicationInfo());
        when(activity.getPackageName()).thenReturn(PACKAGE_NAME);

        when(legacyUriRedirectHandler.checkValidState(eq(activity), eq(loginManager))).thenReturn(true);
    }

    @Test
    public void login_withLegacyModeBlocking_shouldNotLogin() {
        when(legacyUriRedirectHandler.checkValidState(eq(activity), eq(loginManager))).thenReturn(false);
        loginManager.login(activity);

        verify(activity, never()).startActivityForResult(any(Intent.class), anyInt());
    }

    @Test
    public void login_withRedirectToSdkFlowSsoSupported_shouldLoginActivityWithSsoParams() {
        when(ssoDeeplink.isSupported(REDIRECT_TO_SDK)).thenReturn(true);

        List<SupportedAppType> productPriority = ImmutableList.of(UBER_EATS, UBER);
        loginManager.setProductFlowPriority(productPriority);
        loginManager.login(activity);

        verify(ssoDeeplink).isSupported(REDIRECT_TO_SDK);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        ArgumentCaptor<Integer> codeCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(activity).startActivityForResult(intentCaptor.capture(), codeCaptor.capture());

        final Intent resultIntent = intentCaptor.getValue();
        validateLoginIntentFields(resultIntent, new ArrayList<>(productPriority), sessionConfiguration,
                ResponseType.TOKEN, false, true, true);
        assertThat(codeCaptor.getValue()).isEqualTo(REQUEST_CODE_LOGIN_DEFAULT);
    }

    @Test
    public void login_withoutAppPriority_shouldLoginActivityWithSsoParams() {
        when(ssoDeeplink.isSupported(REDIRECT_TO_SDK)).thenReturn(true);

        loginManager.login(activity);

        verify(ssoDeeplink).isSupported(REDIRECT_TO_SDK);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        ArgumentCaptor<Integer> codeCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(activity).startActivityForResult(intentCaptor.capture(), codeCaptor.capture());

        final Intent resultIntent = intentCaptor.getValue();
        validateLoginIntentFields(resultIntent, new ArrayList<SupportedAppType>(), sessionConfiguration,
                ResponseType.TOKEN, false, true, true);
        assertThat(codeCaptor.getValue()).isEqualTo(REQUEST_CODE_LOGIN_DEFAULT);
    }

    @Test
    public void login_withDefaultSsoFlowSupported_shouldExecuteDeeplink() {
        when(ssoDeeplink.isSupported(REDIRECT_TO_SDK)).thenReturn(false);
        when(ssoDeeplink.isSupported(DEFAULT)).thenReturn(true);

        loginManager.login(activity);

        verify(ssoDeeplink).isSupported(DEFAULT);
        verify(ssoDeeplink).execute(DEFAULT);
    }

    @Test
    public void login_withSsoNotSupported_andAuthCodeFlowEnabled_shouldLoginWithAuthCodeFlowParams() {
        when(ssoDeeplink.isSupported(REDIRECT_TO_SDK)).thenReturn(false);
        when(ssoDeeplink.isSupported(DEFAULT)).thenReturn(false);
        loginManager.setAuthCodeFlowEnabled(true);

        loginManager.login(activity);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        ArgumentCaptor<Integer> codeCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(activity).startActivityForResult(intentCaptor.capture(), codeCaptor.capture());

        final Intent resultIntent = intentCaptor.getValue();
        validateLoginIntentFields(resultIntent, new ArrayList<SupportedAppType>(), sessionConfiguration,
                ResponseType.CODE, false, false, false);
        assertThat(codeCaptor.getValue()).isEqualTo(REQUEST_CODE_LOGIN_DEFAULT);
    }

    @Test
    public void login_withSsoNotSupported_andAuthCodeFlowDisabled_shouldLoginWithImplicitGrantParamsAndRedirectToPlayStoreEnabled() {
        when(ssoDeeplink.isSupported(REDIRECT_TO_SDK)).thenReturn(false);
        when(ssoDeeplink.isSupported(DEFAULT)).thenReturn(false);

        loginManager.login(activity);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        ArgumentCaptor<Integer> codeCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(activity).startActivityForResult(intentCaptor.capture(), codeCaptor.capture());

        final Intent resultIntent = intentCaptor.getValue();
        validateLoginIntentFields(resultIntent, new ArrayList<SupportedAppType>(), sessionConfiguration,
                ResponseType.TOKEN, false, false, true);
        assertThat(codeCaptor.getValue()).isEqualTo(REQUEST_CODE_LOGIN_DEFAULT);
    }

    @Test
    public void loginForImplicitGrant_withLegacyModeBlocking_shouldNotLogin() {
        when(legacyUriRedirectHandler.checkValidState(eq(activity), eq(loginManager))).thenReturn(false);
        loginManager.loginForImplicitGrant(activity);

        verify(activity, never()).startActivityForResult(any(Intent.class), anyInt());
    }

    @Test
    public void loginForImplicitGrant_withoutLegacyModeBlocking_shouldLoginWithImplicitGrantParamsAndRedirectToPlayStoreDisabled() {
        loginManager.loginForImplicitGrant(activity);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        ArgumentCaptor<Integer> codeCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(activity).startActivityForResult(intentCaptor.capture(), codeCaptor.capture());

        final Intent resultIntent = intentCaptor.getValue();
        validateLoginIntentFields(resultIntent, new ArrayList<SupportedAppType>(), sessionConfiguration,
                ResponseType.TOKEN, false, false, false);
        assertThat(codeCaptor.getValue()).isEqualTo(REQUEST_CODE_LOGIN_DEFAULT);
    }

    @Test
    public void loginForAuthorizationCode_withLegacyModeBlocking_shouldNotLogin() {
        when(legacyUriRedirectHandler.checkValidState(eq(activity), eq(loginManager))).thenReturn(false);
        loginManager.loginForAuthorizationCode(activity);

        verify(activity, never()).startActivityForResult(any(Intent.class), anyInt());
    }

    @Test
    public void loginForAuthorizationCode_withoutLegacyModeBlocking_shouldLoginWithAuthCodeFlowParams() {
        loginManager.loginForAuthorizationCode(activity);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        ArgumentCaptor<Integer> codeCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(activity).startActivityForResult(intentCaptor.capture(), codeCaptor.capture());

        final Intent resultIntent = intentCaptor.getValue();
        validateLoginIntentFields(resultIntent, new ArrayList<SupportedAppType>(), sessionConfiguration,
                ResponseType.CODE, false, false, false);
        assertThat(codeCaptor.getValue()).isEqualTo(REQUEST_CODE_LOGIN_DEFAULT);
    }

    @Test(expected = IllegalStateException.class)
    public void login_whenMissingScopes_shouldThrowException() {
        sessionConfiguration = sessionConfiguration.newBuilder().setScopes(EMPTY_SCOPES).build();
        loginManager = new LoginManager(accessTokenStorage, callback, sessionConfiguration);

        loginManager.setAuthCodeFlowEnabled(true);
        loginManager.login(activity);
    }

    @Test
    public void login_whenOnlyCustomScopes_shouldLogin() {
        sessionConfiguration = sessionConfiguration.newBuilder()
                .setScopes(EMPTY_SCOPES)
                .setCustomScopes(CUSTOM_SCOPES)
                .build();
        loginManager = new LoginManager(accessTokenStorage, callback, sessionConfiguration);

        loginManager.setAuthCodeFlowEnabled(true);
        loginManager.login(activity);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        ArgumentCaptor<Integer> codeCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(activity).startActivityForResult(intentCaptor.capture(), codeCaptor.capture());

        final Intent resultIntent = intentCaptor.getValue();
        validateLoginIntentFields(resultIntent, new ArrayList<SupportedAppType>(), sessionConfiguration,
                ResponseType.CODE, false, false, false);
        assertThat(codeCaptor.getValue()).isEqualTo(REQUEST_CODE_LOGIN_DEFAULT);
    }

    @Test
    public void onActivityResult_whenResultOkAndHasToken_shouldCallbackSuccess() {
        Intent intent = new Intent()
                .putExtra(EXTRA_ACCESS_TOKEN, ACCESS_TOKEN.getToken())
                .putExtra(EXTRA_SCOPE, AuthUtils.scopeCollectionToString(ACCESS_TOKEN.getScopes()))
                .putExtra(EXTRA_REFRESH_TOKEN, ACCESS_TOKEN.getRefreshToken())
                .putExtra(EXTRA_EXPIRES_IN, ACCESS_TOKEN.getExpiresIn())
                .putExtra(EXTRA_TOKEN_TYPE, ACCESS_TOKEN.getTokenType());

        loginManager.onActivityResult(activity, REQUEST_CODE_LOGIN_DEFAULT, Activity.RESULT_OK, intent);

        ArgumentCaptor<AccessToken> storedToken = ArgumentCaptor.forClass(AccessToken.class);
        ArgumentCaptor<AccessToken> returnedToken = ArgumentCaptor.forClass(AccessToken.class);
        verify(accessTokenStorage).setAccessToken(storedToken.capture());
        verify(callback).onLoginSuccess(returnedToken.capture());

        assertThat(storedToken.getValue()).isEqualTo(ACCESS_TOKEN);
        assertThat(returnedToken.getValue()).isEqualTo(ACCESS_TOKEN);
    }

    @Test
    public void onActivityResult_whenResultOkAndHasCode_shouldCallbackSuccess() {
        Intent intent = new Intent()
                .putExtra(EXTRA_CODE_RECEIVED, AUTHORIZATION_CODE);

        loginManager.onActivityResult(activity, REQUEST_CODE_LOGIN_DEFAULT, Activity.RESULT_OK, intent);

        ArgumentCaptor<String> capturedCode = ArgumentCaptor.forClass(String.class);
        verify(callback).onAuthorizationCodeReceived(capturedCode.capture());

        assertThat(capturedCode.getValue()).isEqualTo(AUTHORIZATION_CODE);
    }

    @Test
    public void onActivityResult_whenResultCanceledAndNoData_shouldCallbackCancel() {
        loginManager.onActivityResult(activity, REQUEST_CODE_LOGIN_DEFAULT, Activity.RESULT_CANCELED, null);
        verify(callback).onLoginCancel();
    }

    @Test
    public void onActivityResult_whenResultCanceledAndHasData_shouldCallbackError() {
        Intent intent = new Intent().putExtra(EXTRA_ERROR, AuthenticationError.INVALID_RESPONSE
                .toStandardString());

        loginManager.onActivityResult(activity, REQUEST_CODE_LOGIN_DEFAULT, Activity.RESULT_CANCELED, intent);
        verify(callback).onLoginError(AuthenticationError.INVALID_RESPONSE);
    }

    @Test
    public void onActivityResult_whenResultCanceledAndNoData_shouldCancel() {
        loginManager.onActivityResult(activity, REQUEST_CODE_LOGIN_DEFAULT, Activity.RESULT_CANCELED, null);
        verify(callback).onLoginCancel();
    }

    @Test
    public void onActivityResult_whenResultOkAndNoData_shouldCallbackErrorUnknown() {
        loginManager.onActivityResult(activity, REQUEST_CODE_LOGIN_DEFAULT, Activity.RESULT_OK, null);
        verify(callback).onLoginError(AuthenticationError.UNKNOWN);
    }

    @Test
    public void onActivityResult_whenRequestCodeDoesNotMatch_nothingShouldHappen() {
        Intent intent = mock(Intent.class);
        loginManager.onActivityResult(activity, 1337, Activity.RESULT_OK, intent);
        verifyZeroInteractions(intent);
        verifyZeroInteractions(callback);
    }

    @Test
    public void onActivityResult_whenResultCanceledAndDataButNoCallback_nothingShouldHappen() {
        Intent intent = mock(Intent.class);
        loginManager.onActivityResult(activity, 1337, Activity.RESULT_OK, intent);
        verifyZeroInteractions(intent);
    }

    @Test
    public void onActivityResult_whenUnavailableAndPrivilegedScopes_shouldTriggerAuthorizationCode() {
        Intent intent = new Intent().putExtra(EXTRA_ERROR, AuthenticationError.UNAVAILABLE.toStandardString());

        loginManager.setAuthCodeFlowEnabled(true);
        loginManager.onActivityResult(activity, REQUEST_CODE_LOGIN_DEFAULT, Activity.RESULT_CANCELED, intent);

        verify(callback, never()).onLoginError(AuthenticationError.UNAVAILABLE);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);

        verify(activity).startActivityForResult(intentCaptor.capture(), eq(REQUEST_CODE_LOGIN_DEFAULT));

        final Intent capturedIntent = intentCaptor.getValue();
        final ResponseType responseType = (ResponseType) capturedIntent
                .getSerializableExtra(EXTRA_RESPONSE_TYPE);
        final SessionConfiguration loginConfiguration = (SessionConfiguration) capturedIntent
                .getSerializableExtra(LoginActivity.EXTRA_SESSION_CONFIGURATION);

        assertThat(responseType).isEqualTo(ResponseType.CODE);
        assertThat(loginConfiguration.getScopes()).containsAll(MIXED_SCOPES);
    }

    @Test
    public void onActivityResult_whenUnavailableAndGeneralScopesWithAuthCodeEnabled_shouldTriggerAuthorizationCode() {
        sessionConfiguration = sessionConfiguration.newBuilder().setScopes(GENERAL_SCOPES).build();
        loginManager = new LoginManager(accessTokenStorage, callback,
                sessionConfiguration);

        Intent intent = new Intent().putExtra(EXTRA_ERROR, AuthenticationError.UNAVAILABLE.toStandardString());

        loginManager.setAuthCodeFlowEnabled(true);
        loginManager.onActivityResult(activity, REQUEST_CODE_LOGIN_DEFAULT, Activity.RESULT_CANCELED, intent);

        verify(callback, never()).onLoginError(AuthenticationError.UNAVAILABLE);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);

        verify(activity).startActivityForResult(intentCaptor.capture(), eq(REQUEST_CODE_LOGIN_DEFAULT));

        final Intent capturedIntent = intentCaptor.getValue();
        final ResponseType responseType = (ResponseType) capturedIntent
                .getSerializableExtra(EXTRA_RESPONSE_TYPE);
        final SessionConfiguration loginConfiguration = (SessionConfiguration) capturedIntent
                .getSerializableExtra(LoginActivity.EXTRA_SESSION_CONFIGURATION);

        assertThat(responseType).isEqualTo(ResponseType.CODE);
        assertThat(loginConfiguration.getScopes()).containsAll(GENERAL_SCOPES);
    }

    @Test
    public void onActivityResult_whenUnavailableAndPrivilegedScopesNoRedirect_shouldError() {
        Intent intent = new Intent().putExtra(EXTRA_ERROR, AuthenticationError.UNAVAILABLE.toStandardString());
        sessionConfiguration = sessionConfiguration.newBuilder().build();
        loginManager = new LoginManager(accessTokenStorage, callback,
                sessionConfiguration)
                .setAuthCodeFlowEnabled(false);

        loginManager.onActivityResult(activity, REQUEST_CODE_LOGIN_DEFAULT, Activity.RESULT_CANCELED, intent);

        verify(callback).onLoginError(AuthenticationError.UNAVAILABLE);
    }

    @Test
    public void onActivityResult_whenUnavailableAndGeneralScopes_shouldTriggerImplicitGrant() {
        sessionConfiguration = sessionConfiguration.newBuilder().setScopes(GENERAL_SCOPES).build();
        loginManager = new LoginManager(accessTokenStorage, callback,
                sessionConfiguration);

        Intent intent = new Intent().putExtra(EXTRA_ERROR, AuthenticationError.UNAVAILABLE.toStandardString());

        loginManager.onActivityResult(activity, REQUEST_CODE_LOGIN_DEFAULT, Activity.RESULT_CANCELED, intent);

        verify(callback, never()).onLoginError(AuthenticationError.UNAVAILABLE);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);

        verify(activity).startActivityForResult(intentCaptor.capture(), eq(REQUEST_CODE_LOGIN_DEFAULT));

        final Intent capturedIntent = intentCaptor.getValue();
        final ResponseType responseType = (ResponseType) capturedIntent
                .getSerializableExtra(EXTRA_RESPONSE_TYPE);
        final SessionConfiguration loginConfiguration = (SessionConfiguration) capturedIntent
                .getSerializableExtra(LoginActivity.EXTRA_SESSION_CONFIGURATION);

        assertThat(responseType).isEqualTo(ResponseType.TOKEN);
        assertThat(loginConfiguration.getScopes()).containsAll(GENERAL_SCOPES);
    }

    @Test
    public void isAuthenticated_withServerToken_true() {
        when(accessTokenStorage.getAccessToken()).thenReturn(null);
        loginManager = new LoginManager(accessTokenStorage, callback, sessionConfiguration
                .newBuilder().setServerToken("serverToken").build());
        assertTrue(loginManager.isAuthenticated());
    }

    @Test
    public void isAuthenticated_withAccessToken_true() {
        when(accessTokenStorage.getAccessToken()).thenReturn(ACCESS_TOKEN);
        assertTrue(loginManager.isAuthenticated());
    }

    @Test
    public void isAuthenticated_withoutAccessOrServerToken_false() {
        when(accessTokenStorage.getAccessToken()).thenReturn(null);
        assertFalse(loginManager.isAuthenticated());
    }

    @Test
    public void getSession_withServerToken_successful() {
        when(accessTokenStorage.getAccessToken()).thenReturn(null);
        loginManager = new LoginManager(accessTokenStorage, callback, sessionConfiguration
                .newBuilder().setServerToken("serverToken").build());
        Session session = loginManager.getSession();
        assertEquals("serverToken", session.getAuthenticator().getSessionConfiguration().getServerToken());
    }

    @Test
    public void getSession_withAccessToken_successful() {
        when(accessTokenStorage.getAccessToken()).thenReturn(ACCESS_TOKEN);
        Session session = loginManager.getSession();
        assertEquals(ACCESS_TOKEN, ((AccessTokenAuthenticator) session.getAuthenticator()).getTokenStorage().getAccessToken());
    }

    @Test(expected = IllegalStateException.class)
    public void getSession_withoutAccessTokenOrToken_fails() {
        when(accessTokenStorage.getAccessToken()).thenReturn(null);
        loginManager.getSession();
    }

    private void validateLoginIntentFields(
            @NonNull Intent loginIntent,
            @NonNull List<SupportedAppType> expectedProductPriority,
            @NonNull SessionConfiguration expectedSessionConfiguration,
            @NonNull ResponseType expectedResponseType,
            boolean expectedForceWebview,
            boolean expectedSsoEnabled,
            boolean expectedRedirectToPlayStoreEnabled) {
        assertThat(loginIntent.getSerializableExtra(EXTRA_SESSION_CONFIGURATION)).isEqualTo(expectedSessionConfiguration);
        assertThat(loginIntent.getSerializableExtra(EXTRA_RESPONSE_TYPE)).isEqualTo(expectedResponseType);
        assertThat((ArrayList<SupportedAppType>) loginIntent.getSerializableExtra(EXTRA_PRODUCT_PRIORITY))
                .containsAll(expectedProductPriority);
        assertThat(loginIntent.getBooleanExtra(EXTRA_FORCE_WEBVIEW, false)).isEqualTo(expectedForceWebview);
        assertThat(loginIntent.getBooleanExtra(EXTRA_SSO_ENABLED, false)).isEqualTo(expectedSsoEnabled);
        assertThat(loginIntent.getBooleanExtra(EXTRA_REDIRECT_TO_PLAY_STORE_ENABLED, false))
                .isEqualTo(expectedRedirectToPlayStoreEnabled);
    }
}
