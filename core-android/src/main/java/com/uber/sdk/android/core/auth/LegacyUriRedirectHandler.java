package com.uber.sdk.android.core.auth;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;

import com.uber.sdk.android.core.R;
import com.uber.sdk.android.core.utils.Utility;
import com.uber.sdk.core.client.SessionConfiguration;

/**
 * Manages migration problems from old style of redirect URI handling to newer version for Custom
 * tabs support.
 *
 * See https://github.com/uber/rides-android-sdk#authentication-migration-version.
 */
class LegacyUriRedirectHandler {

    enum Mode {
        OFF,
        MISCONFIGURED_AUTH_CODE_FLOW,
        MISSING_REDIRECT,
        MISMATCHING_URI;
    }

    private Mode mode = Mode.OFF;

    /**
     * Will validate that the Redirect URI mode is valid {@link Mode#OFF} and return true
     *
     * If false, then the app should terminate codeflow, this will happen in Debug mode for
     * unhandled migration scenarios.
     * See https://github.com/uber/rides-android-sdk#authentication-migration-version.
     *
     * @param activity to lookup package info and launch blocking dialog
     * @param loginManager to validate old auth code flow
     * @return true if valid, false if invalid
     */
    boolean checkValidState(@NonNull Activity activity, @NonNull LoginManager
            loginManager) {
        initState(activity, loginManager);
        boolean validToContinueExecution = true;
        if (isLegacyMode()) {
            String logMessage = activity.getString(getLegacyModeErrorMessage());
            String uiTitle = activity.getString(R.string.ub__misconfigured_redirect_uri_title);
            String uiMessage = activity.getString(R.string.ub__misconfigured_redirect_uri_message);

            validToContinueExecution = !Utility.logAndShowBlockingDebugUIAlert(activity,
                    logMessage, uiTitle, uiMessage, new IllegalStateException(logMessage));
        }
        return validToContinueExecution;
    }

    boolean isLegacyMode() {
        return mode != Mode.OFF;
    }

    private void initState(@NonNull Activity activity, @NonNull LoginManager loginManager) {

        SessionConfiguration sessionConfiguration = loginManager.getSessionConfiguration();
        boolean redirectForAuthorizationCode = loginManager.isRedirectForAuthorizationCode();

        String generatedRedirectUri = activity.getPackageName().concat(".uberauth://redirect");
        String setRedirectUri = sessionConfiguration.getRedirectUri();

        if (redirectForAuthorizationCode) {
            mode = Mode.MISCONFIGURED_AUTH_CODE_FLOW;
        } else if (sessionConfiguration.getRedirectUri() == null) {
            mode = Mode.MISSING_REDIRECT;
        } else if (!generatedRedirectUri.equals(setRedirectUri) &&
                !AuthUtils.isRedirectUriRegistered(activity, Uri.parse(setRedirectUri)) &&
                !loginManager.isAuthCodeFlowEnabled() && !loginManager .isForceAuthCodeFlowEnabled()) {
            mode = Mode.MISMATCHING_URI;
        } else {
            mode = Mode.OFF;
        }

    }



    private Pair<String, String> getLegacyModeMessage(@NonNull Context context, @NonNull
            LoginManager
            loginManager) {

        final Pair<String, String> titleAndMessage;
        switch (mode) {
            case MISCONFIGURED_AUTH_CODE_FLOW:
                titleAndMessage = new Pair<>("Misconfigured Redirect URI - See log.",
                        "The Uber Authentication Flow for the Authorization Code Flow has "
                                + "been upgraded in 0.8.0 and a redirect URI must now be supplied to the application. "
                                + "You are seeing this error because the use of deprecated method "
                                + "LoginManager.setRedirectForAuthorizationCode() indicates your flow may not "
                                + "support the recent changes. See https://github"
                                + ".com/uber/rides-android-sdk#authentication-migration-version"
                                + "-08-and-above for resolution steps"
                                + "to insure your setup is correct and then migrate to the non-deprecate "
                                + "method LoginManager.setAuthCodeFlowEnabled()");
                break;
            case MISSING_REDIRECT:
                titleAndMessage = new Pair<>("Null Redirect URI - See log.",
                        "Redirect URI must be set in "
                        + "Session Configuration.");
                break;
            case MISMATCHING_URI:
                String generatedRedirectUri = context.getPackageName().concat(""
                        + ".uberauth://redirect");
                String setRedirectUri = loginManager.getSessionConfiguration().getRedirectUri();
                titleAndMessage = new Pair<>("Misconfigured Redirect URI - See log.",
                        "Misconfigured redirect_uri. See https://github"
                                + ".com/uber/rides-android-sdk#authentication-migration-version-08-and-above"
                                + "for more info. Either 1) Register " + generatedRedirectUri + " as a "
                                + "redirect uri for the app at https://developer.uber.com/dashboard/ and "
                                + "specify this in your SessionConfiguration or 2) Override the default "
                                + "redirect_uri with the current one set (" + setRedirectUri + ") in the "
                                + "AndroidManifest.");
                break;
            default:
                titleAndMessage = new Pair<>("Unknown URI Redirect Issue", "Unknown issue, see "
                        + "log");
        }

        return titleAndMessage;

    }

    private int getLegacyModeErrorMessage() {
        switch (mode) {
            case MISCONFIGURED_AUTH_CODE_FLOW:
                return R.string.ub__misconfigured_auth_code_flow_log;
            case MISSING_REDIRECT:
                return R.string.ub__missing_redirect_uri_log;
            case MISMATCHING_URI:
                return R.string.ub__mismatching_redirect_uri_log;
            default:
                return 0;
        }
    }
}
