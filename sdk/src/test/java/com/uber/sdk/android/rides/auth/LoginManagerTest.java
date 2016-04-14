package com.uber.sdk.android.rides.auth;

import android.app.Activity;
import android.content.Intent;

import com.google.common.collect.ImmutableList;
import com.uber.sdk.android.rides.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class LoginManagerTest {

    private static final AccessToken ACCESS_TOKEN = new AccessToken(new Date(1458770906206l),
            ImmutableList.of(Scope.PROFILE, Scope.HISTORY),
            "thisIsAnAccessToken");
    private static final int REQUEST_CODE_LOGIN_DEFAULT = 1001;

    private LoginCallback mLoginCallback;
    private LoginManager mLoginManager;
    private AccessTokenManager mAccessTokenManager;

    @Before
    public void setup() {
        mLoginCallback = mock(LoginCallback.class);
        mAccessTokenManager = mock(AccessTokenManager.class);

        mLoginManager = new LoginManager(mAccessTokenManager);
    }

    @Test
    public void loginWithScopes_whenNoSpecificRequestCode_shouldLaunchIntent() {
        Activity activity = Robolectric.setupActivity(Activity.class);
        Collection<Scope> scopes = ImmutableList.of(Scope.PROFILE, Scope.HISTORY);

        mLoginManager.loginWithScopes(activity, scopes);
        ShadowActivity.IntentForResult startedIntent = shadowOf(activity).getNextStartedActivityForResult();
        assertEquals(REQUEST_CODE_LOGIN_DEFAULT, startedIntent.requestCode);
        ArrayList<String> scopesList = startedIntent.intent.getStringArrayListExtra(LoginManager.SCOPES_KEY);
        Collection<Scope> scopesCollection = AuthUtils.stringSetToScopeCollection(new HashSet<>(scopesList));
        assertEquals(scopes.size(), scopesCollection.size());
        assertTrue(scopesCollection.containsAll(scopes));

    }

    @Test
    public void loginWithScopes_whenSpecifiedRequestCode_shouldLaunchIntent() {
        Activity activity = Robolectric.setupActivity(Activity.class);
        Collection<Scope> scopes = ImmutableList.of(Scope.PROFILE, Scope.HISTORY);

        mLoginManager.loginWithScopes(activity, scopes, 2001);
        ShadowActivity.IntentForResult startedIntent = shadowOf(activity).getNextStartedActivityForResult();
        assertEquals(2001, startedIntent.requestCode);
        ArrayList<String> scopesList = startedIntent.intent.getStringArrayListExtra(LoginManager.SCOPES_KEY);
        Collection<Scope> scopesCollection = AuthUtils.stringSetToScopeCollection(new HashSet<>(scopesList));
        assertEquals(scopes.size(), scopesCollection.size());
        assertTrue(scopesCollection.containsAll(scopes));
    }

    @Test
    public void onActivityResult_whenResultOkAndHasData_shouldCallbackSuccess() {
        Intent intent = new Intent().putExtra(LoginManager.ACCESS_TOKEN_KEY, ACCESS_TOKEN);

        mLoginManager.onActivityResult(REQUEST_CODE_LOGIN_DEFAULT, Activity.RESULT_OK, intent, mLoginCallback);
        verify(mAccessTokenManager, times(1)).setAccessToken(ACCESS_TOKEN);
        verify(mLoginCallback, times(1)).onLoginSuccess(ACCESS_TOKEN);
    }

    @Test
    public void onActivityResult_whenResultCanceledAndNoData_shouldCallbackCancel() {
        mLoginManager.onActivityResult(REQUEST_CODE_LOGIN_DEFAULT, Activity.RESULT_CANCELED, null, mLoginCallback);
        verify(mLoginCallback, times(1)).onLoginCancel();
    }

    @Test
    public void onActivityResult_whenResultCanceledAndHasData_shouldCallbackError() {
        Intent intent = new Intent().putExtra(LoginManager.LOGIN_ERROR_KEY, AuthenticationError.INVALID_RESPONSE);

        mLoginManager.onActivityResult(REQUEST_CODE_LOGIN_DEFAULT,
                Activity.RESULT_CANCELED,
                intent,
                mLoginCallback);
        verify(mLoginCallback, times(1)).onLoginError(AuthenticationError.INVALID_RESPONSE);
    }

    @Test
    public void onActivityResult_whenResultCanceledAndNoErrorExtraInData_shouldCallbackErrorUnknown() {
        Intent intent = new Intent();

        mLoginManager.onActivityResult(REQUEST_CODE_LOGIN_DEFAULT,
                Activity.RESULT_CANCELED,
                intent,
                mLoginCallback);
        verify(mLoginCallback, times(1)).onLoginError(AuthenticationError.UNKNOWN);
    }

    @Test
    public void onActivityResult_whenResultOkAndNoData_shouldCallbackErrorUnknown() {
        mLoginManager.onActivityResult(REQUEST_CODE_LOGIN_DEFAULT, Activity.RESULT_OK, null, mLoginCallback);
        verify(mLoginCallback, times(1)).onLoginError(AuthenticationError.UNKNOWN);
    }

    @Test
    public void onActiivtyResult_whenRequestCodeDoesNotMatch_nothingShouldHappen() {
        Intent intent = mock(Intent.class);
        mLoginManager.onActivityResult(1337, Activity.RESULT_OK, intent, mLoginCallback);
        verifyZeroInteractions(intent);
        verifyZeroInteractions(mLoginCallback);
    }

    @Test
    public void onActivityResult_whenResultCanceledAndDataButNoCallback_nothingShouldHappen() {
        Intent intent = mock(Intent.class);
        mLoginManager.onActivityResult(1337, Activity.RESULT_OK, intent, null);
        verifyZeroInteractions(intent);
    }
}
