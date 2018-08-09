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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import com.google.common.collect.ImmutableList;
import com.uber.sdk.android.core.BuildConfig;
import com.uber.sdk.android.core.RobolectricTestBase;
import com.uber.sdk.android.core.utils.AppProtocol;
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
import org.robolectric.Robolectric;

import static com.uber.sdk.android.core.auth.LoginManager.EXTRA_ACCESS_TOKEN;
import static com.uber.sdk.android.core.auth.LoginManager.EXTRA_CODE_RECEIVED;
import static com.uber.sdk.android.core.auth.LoginManager.EXTRA_ERROR;
import static com.uber.sdk.android.core.auth.LoginManager.EXTRA_EXPIRES_IN;
import static com.uber.sdk.android.core.auth.LoginManager.EXTRA_REFRESH_TOKEN;
import static com.uber.sdk.android.core.auth.LoginManager.EXTRA_SCOPE;
import static com.uber.sdk.android.core.auth.LoginManager.EXTRA_TOKEN_TYPE;
import static com.uber.sdk.android.core.auth.LoginManager.REQUEST_CODE_LOGIN_DEFAULT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
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
    private static final String PUBLIC_SIGNATURE =
            "3082022730820190a00302010202044cb88a8e300d06092a864886f70d01010505003057311330110603550408130a43616c696" +
                    "66f726e6961311630140603550407130d53616e204672616e636973636f3110300e060355040a130755626572436162" +
                    "311630140603550403130d4a6f7264616e20426f6e6e65743020170d3130313031353137303833305a180f323036303" +
                    "13030323137303833305a3057311330110603550408130a43616c69666f726e6961311630140603550407130d53616e" +
                    "204672616e636973636f3110300e060355040a130755626572436162311630140603550403130d4a6f7264616e20426" +
                    "f6e6e657430819f300d06092a864886f70d010101050003818d00308189028181009769b8ee7e4af5eae5bfbac410a0" +
                    "b0daf8d58ca8c9503878cbfb9461d617b2a5695a639962492ee7f5938f036c7927e4e1a680f186d98fdebf38955fb3f" +
                    "c23077bd3ff39551cdb35690fd451411c643b26f31d280dc4a55b501e9a0d53d8f8f72a407854516f0f2a4e4d48c02b" +
                    "dfae408d162a5da34397f845ddfa17de57cd3d0203010001300d06092a864886f70d010105050003818100283f752dc" +
                    "67c2d8ea2a7e47b1269b2cb37f961c53db3d1c9158af0722978f6a3c396149447557fcf63caa497a795514922f3a4e8" +
                    "5990608c47d90955ce9cc71f93199a5f3c7624cca8fac70ff70b1e4cf9eb887a92f358aa21ba42e0e86bbecf7d030d8" +
                    "1a383b716f22ac98746f2956e90b96e8f35d298498e55cdbe4d42a762";

    private static final AccessToken ACCESS_TOKEN = new AccessToken(2592000,
            ImmutableList.of(Scope.PROFILE, Scope.HISTORY), "thisIsAnAccessToken", "refreshToken",
            "tokenType");

    private static final int REQUEST_CODE = 9321;
    private static final String CLIENT_ID = "Client1234";
    private static final ImmutableList<Scope> MIXED_SCOPES = ImmutableList.of(Scope.PROFILE, Scope.REQUEST_RECEIPT);
    private static final ImmutableList<Scope> GENERAL_SCOPES = ImmutableList.of(Scope.PROFILE, Scope.HISTORY);

    private static final String DEFAULT_REGION =
            "uber://connect?client_id=Client1234&scope=profile%20request_receipt&sdk=android&sdk_version="
                    + BuildConfig.VERSION_NAME;

    private static final String INSTALL =
            String.format("https://m.uber.com/sign-up?client_id=Client1234&user-agent=core-android-v%s-login_manager",
                    BuildConfig.VERSION_NAME);
    private static final String AUTHORIZATION_CODE = "Auth123Code";

    @Mock
    Activity activity;

    @Mock
    PackageManager packageManager;

    @Mock
    LoginCallback callback;

    @Mock
    AccessTokenStorage accessTokenStorage;

    @Mock LegacyUriRedirectHandler legacyUriRedirectHandler;

    SessionConfiguration sessionConfiguration;

    private LoginManager loginManager;

    @Before
    public void setup() {
        sessionConfiguration = new SessionConfiguration.Builder().setClientId(CLIENT_ID)
                .setRedirectUri("com.example.uberauth://redirect")
                .setScopes(MIXED_SCOPES).build();
        loginManager = new LoginManager(accessTokenStorage, callback,
                sessionConfiguration, REQUEST_CODE_LOGIN_DEFAULT,
                legacyUriRedirectHandler);


        when(activity.getPackageManager()).thenReturn(packageManager);
        when(activity.getApplicationInfo()).thenReturn(new ApplicationInfo());
        when(activity.getPackageName()).thenReturn("com.example");
        when(legacyUriRedirectHandler.checkValidState(eq(activity), eq(loginManager))).thenReturn(true);
    }

    @Test
    public void login_withLegacyModeBlocking_shouldNotLogin() {
        stubAppInstalled(packageManager, AppProtocol.UBER_PACKAGE_NAMES[0], SsoDeeplink.MIN_VERSION_SUPPORTED);
        when(legacyUriRedirectHandler.checkValidState(eq(activity), eq(loginManager))).thenReturn(false);
        loginManager.login(activity);

        verify(activity, never()).startActivityForResult(any(Intent.class), anyInt());
    }

    @Test
    public void login_withLegacyModeNotBlocking_shouldLogin() {
        stubAppInstalled(packageManager, AppProtocol.UBER_PACKAGE_NAMES[0], SsoDeeplink.MIN_VERSION_SUPPORTED);
        when(legacyUriRedirectHandler.checkValidState(eq(activity), eq(loginManager))).thenReturn(true);
        loginManager.login(activity);

        verify(activity).startActivityForResult(any(Intent.class), anyInt());
    }

    @Test
    public void loginWithAppInstalledPrivilegedScopes_shouldLaunchIntent() {
        stubAppInstalled(packageManager, AppProtocol.UBER_PACKAGE_NAMES[0], SsoDeeplink.MIN_VERSION_SUPPORTED);

        loginManager.login(activity);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        ArgumentCaptor<Integer> codeCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(activity).startActivityForResult(intentCaptor.capture(), codeCaptor.capture());

        assertThat(intentCaptor.getValue().getData().toString()).isEqualTo(DEFAULT_REGION);
        assertThat(codeCaptor.getValue()).isEqualTo(REQUEST_CODE_LOGIN_DEFAULT);
    }

    @Test
    public void loginWithAppInstalledPrivilegedScopesAndRequestCode_shouldLaunchIntent() {
        loginManager = new LoginManager(accessTokenStorage, callback,
                sessionConfiguration, REQUEST_CODE);

        stubAppInstalled(packageManager, AppProtocol.UBER_PACKAGE_NAMES[0], SsoDeeplink.MIN_VERSION_SUPPORTED);

        loginManager.login(activity);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        ArgumentCaptor<Integer> codeCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(activity).startActivityForResult(intentCaptor.capture(), codeCaptor.capture());

        assertThat(intentCaptor.getValue().getData().toString()).isEqualTo(DEFAULT_REGION);
        assertThat(codeCaptor.getValue()).isEqualTo(REQUEST_CODE);
    }

    @Test
    public void loginWithoutAppInstalledGeneralScopes_shouldLaunchImplicitGrant() {
        sessionConfiguration = sessionConfiguration.newBuilder().setScopes(GENERAL_SCOPES).build();
        loginManager = new LoginManager(accessTokenStorage, callback,
                sessionConfiguration);

        stubAppNotInstalled(packageManager, AppProtocol.UBER_PACKAGE_NAMES[0]);

        loginManager.login(activity);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        ArgumentCaptor<Integer> codeCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(activity).startActivityForResult(intentCaptor.capture(), codeCaptor.capture());

        assertThat(codeCaptor.getValue()).isEqualTo(REQUEST_CODE_LOGIN_DEFAULT);

        final Intent capturedIntent = intentCaptor.getValue();
        final ResponseType responseType = (ResponseType) capturedIntent
                .getSerializableExtra(LoginActivity.EXTRA_RESPONSE_TYPE);
        final SessionConfiguration configuration = (SessionConfiguration) intentCaptor.getValue()
                .getSerializableExtra(LoginActivity.EXTRA_SESSION_CONFIGURATION);
        assertThat(responseType).isEqualTo(ResponseType.TOKEN);
        assertThat(configuration.getScopes()).containsAll(GENERAL_SCOPES);
    }

    @Test
    public void loginWithoutAppInstalledGeneralScopesAndAuthCodeFlowEnabled_shouldLaunchAuthCodeFlow() {
        sessionConfiguration = sessionConfiguration.newBuilder().setScopes(GENERAL_SCOPES).build();
        loginManager = new LoginManager(accessTokenStorage, callback,
                sessionConfiguration);

        stubAppNotInstalled(packageManager, AppProtocol.UBER_PACKAGE_NAMES[0]);

        loginManager.setAuthCodeFlowEnabled(true);
        loginManager.login(activity);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        ArgumentCaptor<Integer> codeCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(activity).startActivityForResult(intentCaptor.capture(), codeCaptor.capture());

        assertThat(codeCaptor.getValue()).isEqualTo(REQUEST_CODE_LOGIN_DEFAULT);

        final Intent capturedIntent = intentCaptor.getValue();
        final ResponseType responseType = (ResponseType) capturedIntent
                .getSerializableExtra(LoginActivity.EXTRA_RESPONSE_TYPE);
        final SessionConfiguration configuration = (SessionConfiguration) intentCaptor.getValue()
                .getSerializableExtra(LoginActivity.EXTRA_SESSION_CONFIGURATION);
        assertThat(responseType).isEqualTo(ResponseType.CODE);
        assertThat(configuration.getScopes()).containsAll(GENERAL_SCOPES);
    }

    @Test
    public void loginWithoutAppInstalledPrivilegedScopesAndAuthCodeFlowEnabled_shouldLaunchAuthCodeFlow() {
        stubAppNotInstalled(packageManager, AppProtocol.UBER_PACKAGE_NAMES[0]);

        loginManager.setAuthCodeFlowEnabled(true);
        loginManager.login(activity);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        ArgumentCaptor<Integer> codeCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(activity).startActivityForResult(intentCaptor.capture(), codeCaptor.capture());

        assertThat(codeCaptor.getValue()).isEqualTo(REQUEST_CODE_LOGIN_DEFAULT);

        final Intent capturedIntent = intentCaptor.getValue();
        final ResponseType responseType = (ResponseType) capturedIntent
                .getSerializableExtra(LoginActivity.EXTRA_RESPONSE_TYPE);
        final SessionConfiguration configuration = (SessionConfiguration) intentCaptor.getValue()
                .getSerializableExtra(LoginActivity.EXTRA_SESSION_CONFIGURATION);
        assertThat(responseType).isEqualTo(ResponseType.CODE);
        assertThat(configuration.getScopes()).containsAll(MIXED_SCOPES);
    }

    @Test
    public void loginWithoutAppInstalledPrivilegedScopes_shouldLaunchAppInstall() {
        final Activity activity = spy(Robolectric.setupActivity(Activity.class));
        when(activity.getPackageManager()).thenReturn(packageManager);

        sessionConfiguration = sessionConfiguration.newBuilder().build();
        loginManager = new LoginManager(accessTokenStorage, callback,
                sessionConfiguration)
                .setAuthCodeFlowEnabled(false);

        stubAppNotInstalled(packageManager, AppProtocol.UBER_PACKAGE_NAMES[0]);

        loginManager.login(activity);

        ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);

        verify(activity).startActivity(intentCaptor.capture());

        assertThat(intentCaptor.getValue().getData().toString()).isEqualTo(INSTALL);
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
                .getSerializableExtra(LoginActivity.EXTRA_RESPONSE_TYPE);
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
                .getSerializableExtra(LoginActivity.EXTRA_RESPONSE_TYPE);
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
                .getSerializableExtra(LoginActivity.EXTRA_RESPONSE_TYPE);
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
        assertEquals(ACCESS_TOKEN, ((AccessTokenAuthenticator)session.getAuthenticator()).getTokenStorage().getAccessToken());
    }

    @Test(expected = IllegalStateException.class)
    public void getSession_withoutAccessTokenOrToken_fails() {
        when(accessTokenStorage.getAccessToken()).thenReturn(null);
        loginManager.getSession();
    }

    private static PackageManager stubAppInstalled(PackageManager packageManager, String packageName, int versionCode) {
        final PackageInfo packageInfo = new PackageInfo();
        packageInfo.versionCode = versionCode;
        packageInfo.signatures = new Signature[]{new Signature(PUBLIC_SIGNATURE)};
        packageInfo.packageName = packageName;
        try {
            when(packageManager.getPackageInfo(eq(packageName), anyInt()))
                    .thenReturn(packageInfo);
        } catch (PackageManager.NameNotFoundException e) {
            fail("Unable to mock Package Manager");
        }
        return packageManager;
    }

    private static PackageManager stubAppNotInstalled(PackageManager packageManager, String packageName) {
        try {
            when(packageManager.getPackageInfo(eq(packageName), anyInt()))
                    .thenThrow(PackageManager.NameNotFoundException.class);
        } catch (PackageManager.NameNotFoundException e) {
            fail("Unable to mock Package Manager");
        }
        return packageManager;
    }
}
