package com.uber.sdk.android.core.auth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.uber.sdk.android.core.BuildConfig;
import com.uber.sdk.android.core.R;
import com.uber.sdk.android.core.RobolectricTestBase;
import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.core.client.SessionConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowLog;

import java.util.List;
import java.util.logging.LogManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.contentOf;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

public class LegacyUriRedirectHandlerTest extends RobolectricTestBase {

    @Mock LoginManager loginManager;
    @Mock PackageManager packageManager;
    @Mock SessionConfiguration sessionConfiguration;

    Activity activity;
    LegacyUriRedirectHandler legacyUriRedirectHandler;

    String misconfiguredAuthCode;
    String missingRedirectUri;
    String mismatchingRedirectUri;

    String alertTitle;
    String alertMessage;


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

        misconfiguredAuthCode = RuntimeEnvironment.application.getString(
                R.string.ub__misconfigured_auth_code_flow_log);
        missingRedirectUri = RuntimeEnvironment.application.getString(
                R.string.ub__missing_redirect_uri_log);
        mismatchingRedirectUri = RuntimeEnvironment.application.getString(
                R.string.ub__mismatching_redirect_uri_log);

        alertTitle = RuntimeEnvironment.application.getString(
                R.string.ub__misconfigured_redirect_uri_title);
        alertMessage = RuntimeEnvironment.application.getString(
                R.string.ub__misconfigured_redirect_uri_message);
    }

    @Test
    public void handleInvalidState_withMismatchingUriInDebug_invalidStateWithAlertDialog() {
        when(sessionConfiguration.getRedirectUri())
                .thenReturn("com.example2.uberauth://redirect-uri");

        assertThat(legacyUriRedirectHandler.checkValidState(activity,
                loginManager)).isFalse();
        assertThat(legacyUriRedirectHandler.isLegacyMode()).isTrue();

        assertDialogShown();
        assertLastLog(mismatchingRedirectUri);

    }

    @Test
    public void handleInvalidState_withMismatchingUriInRelease_validStateWithLog() {
        when(sessionConfiguration.getRedirectUri())
                .thenReturn("com.example2.uberauth://redirect-uri");
        applicationInfo.flags = 0;

        assertThat(legacyUriRedirectHandler.checkValidState(activity,
                loginManager)).isTrue();
        assertThat(legacyUriRedirectHandler.isLegacyMode()).isTrue();

        assertNoDialogShown();
        assertLastLog(mismatchingRedirectUri);
    }

    @Test
    public void handleInvalidState_withLegacyAuthCodeFlowInDebug_invalidStatWithAlertDialog() {
        when(loginManager.isRedirectForAuthorizationCode()).thenReturn(true);

        assertThat(legacyUriRedirectHandler.checkValidState(activity, loginManager)).isFalse();
        assertThat(legacyUriRedirectHandler.isLegacyMode()).isTrue();

        assertDialogShown();
        assertLastLog(misconfiguredAuthCode);
    }

    @Test
    public void handleInvalidState_withLegacyAuthCodeFlowInRelease_validState() {
        when(loginManager.isRedirectForAuthorizationCode()).thenReturn(true);
        applicationInfo.flags = 0;

        assertThat(legacyUriRedirectHandler.checkValidState(activity,
                loginManager)).isTrue();
        assertThat(legacyUriRedirectHandler.isLegacyMode()).isTrue();

        assertNoDialogShown();
        assertLastLog(misconfiguredAuthCode);
    }

    @Test
    public void handleInvalidState_withMissingRedirectUriInDebug_invalidStateWithAlertDialog() {
        when(sessionConfiguration.getRedirectUri())
                .thenReturn(null);

        assertThat(legacyUriRedirectHandler.checkValidState(activity,
                loginManager)).isFalse();
        assertThat(legacyUriRedirectHandler.isLegacyMode()).isTrue();

        assertDialogShown();
        assertLastLog(missingRedirectUri);
    }

    @Test
    public void handleInvalidState_withMissingRedirectUriInRelease_validState() {
        when(sessionConfiguration.getRedirectUri())
                .thenReturn(null);
        applicationInfo.flags = 0;

        assertThat(legacyUriRedirectHandler.checkValidState(activity,
                loginManager)).isTrue();
        assertThat(legacyUriRedirectHandler.isLegacyMode()).isTrue();

        assertNoDialogShown();
        assertLastLog(missingRedirectUri);
    }

    @Test
    public void handleInvalidState_withMatchingRedirectUriAndNoLegacyAuthCodeFlow_validState() {
        assertThat(legacyUriRedirectHandler.checkValidState(activity,
                loginManager)).isTrue();
        assertThat(legacyUriRedirectHandler.isLegacyMode()).isFalse();

        assertNoDialogShown();
        assertNoLogs();
    }

    @Test
    public void handleInvalidState_withAuthCodeFlowAndMisMatchingRedirectUriInDebug_validState() {
        when(sessionConfiguration.getRedirectUri())
                .thenReturn("com.example2.uberauth://redirect-uri");
        when(loginManager.isAuthCodeFlowEnabled()).thenReturn(true);

        assertThat(legacyUriRedirectHandler.checkValidState(activity,
                loginManager)).isTrue();
        assertThat(legacyUriRedirectHandler.isLegacyMode()).isFalse();

        assertNoDialogShown();
        assertNoLogs();
    }

    @Test
    public void handleInvalidState_withAuthCodeFlowAndMisMatchingRedirectUriInRelease_validState() {
        when(sessionConfiguration.getRedirectUri())
                .thenReturn("com.example2.uberauth://redirect-uri");
        when(loginManager.isAuthCodeFlowEnabled()).thenReturn(true);
        applicationInfo.flags = 0;

        assertThat(legacyUriRedirectHandler.checkValidState(activity,
                loginManager)).isTrue();
        assertThat(legacyUriRedirectHandler.isLegacyMode()).isFalse();

        assertNoDialogShown();
        assertNoLogs();
    }

    @Test
    public void isLegacyMode_uninitialized_validState() {
        assertThat(legacyUriRedirectHandler.isLegacyMode()).isFalse();

        assertNoDialogShown();
        assertNoLogs();
    }

    private void assertLastLog(String message) {
        List<ShadowLog.LogItem> logItemList = ShadowLog.getLogsForTag(UberSdk.UBER_SDK_LOG_TAG);
        assertThat(ShadowLog.getLogsForTag(UberSdk.UBER_SDK_LOG_TAG)).isNotEmpty();
        ShadowLog.LogItem logItem = logItemList.get(logItemList.size()-1);
        assertThat(logItem.msg).isEqualTo(message);
    }

    private void assertNoLogs() {
        List<ShadowLog.LogItem> logItemList = ShadowLog.getLogsForTag(UberSdk.UBER_SDK_LOG_TAG);
        assertThat(ShadowLog.getLogsForTag(UberSdk.UBER_SDK_LOG_TAG)).isEmpty();
    }

    private void assertDialogShown() {
        AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertThat(alertDialog.isShowing()).isTrue();
        assertThat(shadowOf(alertDialog).getTitle())
                .isEqualTo(alertTitle);
        assertThat(shadowOf(alertDialog).getMessage())
                .isEqualTo(alertMessage);
    }

    private void assertNoDialogShown() {
        AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertThat(alertDialog).isNull();
    }
}