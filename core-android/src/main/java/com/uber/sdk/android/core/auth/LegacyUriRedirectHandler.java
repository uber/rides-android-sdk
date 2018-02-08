package com.uber.sdk.android.core.auth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;

import com.uber.sdk.android.core.UberSdk;
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
        OLD_AUTH_CODE_FLOW,
        MISSING_REDIRECT,
        MISCONFIGURED_URI;
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

        if (isLegacyMode()) {
            final Pair<String, String> titleAndMessage = getLegacyModeMessage(activity, loginManager);
            final IllegalStateException exception = new IllegalStateException(titleAndMessage
                    .second);
            Log.e(UberSdk.UBER_SDK_LOG_TAG, titleAndMessage.first,
                    exception);

            if(Utility.isDebugable(activity)) {
                new AlertDialog.Builder(activity)
                        .setTitle(titleAndMessage.first)
                        .setMessage(titleAndMessage.second)
                        .setNeutralButton("Exit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                throw exception;
                            }
                        }).show();
                return false;
            }
        }
        return true;
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
            mode = Mode.OLD_AUTH_CODE_FLOW;
        } else if (sessionConfiguration.getRedirectUri() == null) {
            mode = Mode.MISSING_REDIRECT;
        } else if (!generatedRedirectUri.equals(setRedirectUri) &&
                !AuthUtils.isRedirectUriRegistered(activity, Uri.parse(setRedirectUri))) {
            mode = Mode.MISCONFIGURED_URI;
        } else {
            mode = Mode.OFF;
        }

    }



    private Pair<String, String> getLegacyModeMessage(@NonNull Context context, @NonNull
            LoginManager
            loginManager) {

        final Pair<String, String> titleAndMessage;
        switch (mode) {
            case OLD_AUTH_CODE_FLOW:
                titleAndMessage = new Pair<>("Misconfigured SessionConfiguration, see log.",
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
                titleAndMessage = new Pair<>("Misconfigured SessionConfiguration, see log.", "Redirect URI must be set in "
                        + "Session Configuration.");
                break;
            case MISCONFIGURED_URI:
                String generatedRedirectUri = context.getPackageName().concat(""
                        + ".uberauth://redirect");
                String setRedirectUri = loginManager.getSessionConfiguration().getRedirectUri();
                titleAndMessage = new Pair<>("Misconfigured redirect uri, see log.",
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
}
