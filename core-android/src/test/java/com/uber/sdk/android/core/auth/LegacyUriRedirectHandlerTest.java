package com.uber.sdk.android.core.auth;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.uber.sdk.android.core.RobolectricTestBase;
import com.uber.sdk.core.client.SessionConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class LegacyUriRedirectHandlerTest extends RobolectricTestBase {

    @Mock LoginManager loginManager;
    @Mock PackageManager packageManager;
    @Mock SessionConfiguration sessionConfiguration;

    Activity activity;
    LegacyUriRedirectHandler legacyUriRedirectHandler;


    private ApplicationInfo applicationInfo;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        applicationInfo = new ApplicationInfo();
        applicationInfo.flags = ApplicationInfo.FLAG_DEBUGGABLE;
        activity = spy(Robolectric.setupActivity(Activity.class));
        //applicationInfo.flags = 0;

        when(sessionConfiguration.getRedirectUri()).thenReturn("com.example.uberauth://redirect");
        when(loginManager.getSessionConfiguration()).thenReturn(sessionConfiguration);
        when(activity.getApplicationInfo()).thenReturn(applicationInfo);
        when(activity.getPackageManager()).thenReturn(packageManager);
        when(activity.getPackageName()).thenReturn("com.example");

        legacyUriRedirectHandler = new LegacyUriRedirectHandler();
    }

    @Test
    public void handleInvalidState_withMismatchingUriInDebug_invalidState() throws Exception {
        when(sessionConfiguration.getRedirectUri())
                .thenReturn("com.example2.uberauth://redirect-uri");

        assertThat(legacyUriRedirectHandler.checkValidState(activity,
                loginManager)).isFalse();
        assertThat(legacyUriRedirectHandler.isLegacyMode()).isTrue();
    }

    @Test
    public void handleInvalidState_withMismatchingUriInRelease_validState() throws Exception {
        when(sessionConfiguration.getRedirectUri())
                .thenReturn("com.example2.uberauth://redirect-uri");
        applicationInfo.flags = 0;

        assertThat(legacyUriRedirectHandler.checkValidState(activity,
                loginManager)).isTrue();
        assertThat(legacyUriRedirectHandler.isLegacyMode()).isTrue();
    }

    @Test
    public void handleInvalidState_withLegacyAuthCodeFlowInDebug_invalidState() throws Exception {
        when(loginManager.isRedirectForAuthorizationCode()).thenReturn(true);

        assertThat(legacyUriRedirectHandler.checkValidState(activity, loginManager)).isFalse();
        assertThat(legacyUriRedirectHandler.isLegacyMode()).isTrue();
    }

    @Test
    public void handleInvalidState_withLegacyAuthCodeFlowInRelease_validState() throws Exception {
        when(loginManager.isRedirectForAuthorizationCode()).thenReturn(true);
        applicationInfo.flags = 0;

        assertThat(legacyUriRedirectHandler.checkValidState(activity,
                loginManager)).isTrue();
        assertThat(legacyUriRedirectHandler.isLegacyMode()).isTrue();
    }

    @Test
    public void handleInvalidState_withMissingRedirectUriInDebug_invalidState() throws Exception {
        when(sessionConfiguration.getRedirectUri())
                .thenReturn(null);

        assertThat(legacyUriRedirectHandler.checkValidState(activity,
                loginManager)).isFalse();
        assertThat(legacyUriRedirectHandler.isLegacyMode()).isTrue();
    }

    @Test
    public void handleInvalidState_withMissingRedirectUriInRelease_validState() throws Exception {
        when(sessionConfiguration.getRedirectUri())
                .thenReturn(null);
        applicationInfo.flags = 0;

        assertThat(legacyUriRedirectHandler.checkValidState(activity,
                loginManager)).isTrue();
        assertThat(legacyUriRedirectHandler.isLegacyMode()).isTrue();
    }

    @Test
    public void handleInvalidState_withValidState_validState() throws Exception {
        assertThat(legacyUriRedirectHandler.checkValidState(activity,
                loginManager)).isTrue();
        assertThat(legacyUriRedirectHandler.isLegacyMode()).isFalse();
    }

    @Test
    public void isLegacyMode_uninitialized_validState() throws Exception {
        assertThat(legacyUriRedirectHandler.isLegacyMode()).isFalse();
    }
}