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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.uber.sdk.android.core.SupportedAppType;
import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.android.core.utils.AppProtocol;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.AccessTokenStorage;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.core.client.AccessTokenSession;
import com.uber.sdk.core.client.ServerTokenSession;
import com.uber.sdk.core.client.Session;
import com.uber.sdk.core.client.SessionConfiguration;

import java.util.ArrayList;
import java.util.Collection;

import static com.uber.sdk.android.core.utils.Preconditions.checkState;
import static com.uber.sdk.core.client.utils.Preconditions.checkNotNull;

/**
 * Manages user login via OAuth 2.0 Implicit Grant.  Be sure to call
 * {@link LoginManager#onActivityResult(Activity, int, int, Intent)} in your
 * {@link Activity#onActivityResult(int, int, Intent)} to forward along LoginResults.
 */
public class LoginManager {

    /**
     * Used to retrieve the {@link AuthenticationError} from an {@link Intent}.
     */
    static final String EXTRA_ERROR = "ERROR";

    /**
     * Used to retrieve the Access Token from an {@link Intent}.
     */
    static final String EXTRA_ACCESS_TOKEN = "ACCESS_TOKEN";

    /**
     * Used to retrieve the Refresh Token from an {@link Intent}.
     */
    static final String EXTRA_REFRESH_TOKEN = "REFRESH_TOKEN";

    /**
     * Used retrieve the {@link Scope}s granted from from an {@link Intent}.
     */
    static final String EXTRA_SCOPE = "SCOPE";

    /**
     * Used to retrieve the token expiry from an {@link Intent}.
     */
    static final String EXTRA_EXPIRES_IN = "EXPIRES_IN";

    /**
     * Used to retrieve the token type from an {@link Intent}.
     */
    static final String EXTRA_TOKEN_TYPE = "TOKEN_TYPE";

    /**
     * Used to indicate that an authorization code has been received from an {@link Intent}.
     */
    static final String EXTRA_CODE_RECEIVED = "CODE_RECEIVED";

    static final int REQUEST_CODE_LOGIN_DEFAULT = 1001;

    private final AccessTokenStorage accessTokenStorage;
    private final LoginCallback callback;
    private final SessionConfiguration sessionConfiguration;
    private final int requestCode;
    private final LegacyUriRedirectHandler legacyUriRedirectHandler;

    private ArrayList<SupportedAppType> productFlowPriority;
    private boolean authCodeFlowEnabled = false;
    @Deprecated
    private boolean redirectForAuthorizationCode = false;

    /**
     * @param accessTokenStorage to store access token.
     * @param loginCallback      callback to be called when {@link LoginManager#onActivityResult(Activity, int, int, Intent)}
     *                           is called.
     */
    public LoginManager(
            @NonNull AccessTokenStorage accessTokenStorage,
            @NonNull LoginCallback loginCallback) {
        this(accessTokenStorage, loginCallback, UberSdk.getDefaultSessionConfiguration());
    }

    /**
     * @param accessTokenStorage to store access token.
     * @param loginCallback      callback to be called when {@link LoginManager#onActivityResult(Activity, int, int, Intent)} is called.
     * @param configuration      to provide authentication information
     */
    public LoginManager(
            @NonNull AccessTokenStorage accessTokenStorage,
            @NonNull LoginCallback loginCallback,
            @NonNull SessionConfiguration configuration) {
        this(accessTokenStorage, loginCallback, configuration, REQUEST_CODE_LOGIN_DEFAULT);
    }

    /**
     * @param accessTokenStorage to store access token.
     * @param loginCallback      callback to be called when {@link LoginManager#onActivityResult(Activity, int, int, Intent)} is called.
     * @param configuration      to provide authentication information
     * @param requestCode        custom code to use for Activity communication
     */
    public LoginManager(
            @NonNull AccessTokenStorage accessTokenStorage,
            @NonNull LoginCallback loginCallback,
            @NonNull SessionConfiguration configuration,
            int requestCode) {
        this(accessTokenStorage, loginCallback, configuration, requestCode, new LegacyUriRedirectHandler());
    }

    /**
     * @param accessTokenStorage       to store access token.
     * @param loginCallback            callback to be called when {@link LoginManager#onActivityResult(Activity, int, int, Intent)} is called.
     * @param configuration            to provide authentication information
     * @param requestCode              custom code to use for Activity communication
     * @param legacyUriRedirectHandler Used to handle URI Redirect Migration
     */
    LoginManager(
            @NonNull AccessTokenStorage accessTokenStorage,
            @NonNull LoginCallback loginCallback,
            @NonNull SessionConfiguration configuration,
            int requestCode,
            @NonNull LegacyUriRedirectHandler legacyUriRedirectHandler) {
        this.accessTokenStorage = accessTokenStorage;
        this.callback = loginCallback;
        this.productFlowPriority = new ArrayList<>();
        this.sessionConfiguration = configuration;
        this.requestCode = requestCode;
        this.legacyUriRedirectHandler = legacyUriRedirectHandler;
    }

    /**
     * Logs a user in, requesting approval for specified {@link Scope}s.
     *
     * @param activity the activity used to start the {@link LoginActivity}.
     */
    public void login(final @NonNull Activity activity) {
        boolean hasScopes = (sessionConfiguration.getScopes() != null && !sessionConfiguration.getScopes().isEmpty())
                || (sessionConfiguration.getCustomScopes() != null && !sessionConfiguration.getCustomScopes().isEmpty());
        checkState(hasScopes, "Scopes must be set in the Session Configuration.");
        checkNotNull(sessionConfiguration.getRedirectUri(),
                "Redirect URI must be set in Session Configuration.");

        if (!legacyUriRedirectHandler.checkValidState(activity, this)) {
            return;
        }

        SsoDeeplink ssoDeeplink = getSsoDeeplink(activity);

        if (ssoDeeplink.isSupported(SsoDeeplink.FlowVersion.REDIRECT_TO_SDK)) {
            Intent intent = LoginActivity.newIntent(
                    activity,
                    productFlowPriority,
                    sessionConfiguration,
                    ResponseType.TOKEN,
                    false,
                    true,
                    true);
            activity.startActivityForResult(intent, requestCode);
        } else if (ssoDeeplink.isSupported(SsoDeeplink.FlowVersion.DEFAULT)) {
            ssoDeeplink.execute(SsoDeeplink.FlowVersion.DEFAULT);
        } else if (isAuthCodeFlowEnabled()) {
            loginForAuthorizationCode(activity);
        } else {
            loginForImplicitGrantWithFallback(activity);
        }
    }

    /**
     * Login using Implicit Grant (Webview)
     *
     * @param activity to start Activity on.
     */
    @Deprecated
    public void loginForImplicitGrant(@NonNull Activity activity) {

        if (!legacyUriRedirectHandler.checkValidState(activity, this)) {
            return;
        }

        Intent intent = LoginActivity.newIntent(activity, sessionConfiguration,
                ResponseType.TOKEN, legacyUriRedirectHandler.isLegacyMode());
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * login with Authorization Code mode.
     *
     * @param activity to start Activity on.
     */
    public void loginForAuthorizationCode(@NonNull Activity activity) {
        if (!legacyUriRedirectHandler.checkValidState(activity, this)) {
            return;
        }

        Intent intent = LoginActivity.newIntent(activity, sessionConfiguration,
                ResponseType.CODE, legacyUriRedirectHandler.isLegacyMode());
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Login using Implicit Grant, but if requesting privileged scopes, fallback to redirecting the user to the play
     * store to install the app.
     *
     * @param activity to start Activity on.
     */
    private void loginForImplicitGrantWithFallback(@NonNull Activity activity) {
        if (!legacyUriRedirectHandler.checkValidState(activity, this)) {
            return;
        }

        Intent intent = LoginActivity.newIntent(
                activity,
                new ArrayList<SupportedAppType>(),
                sessionConfiguration,
                ResponseType.TOKEN,
                legacyUriRedirectHandler.isLegacyMode(),
                false,
                true);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * @return {@link AccessTokenStorage} that is used.
     * @deprecated Use {@link LoginManager#getAccessTokenStorage()}
     */
    @Deprecated
    @NonNull
    @SuppressWarnings("unchecked")
    public <T extends AccessTokenStorage> T getAccessTokenManager() {
        return (T) accessTokenStorage;
    }

    /**
     * @return {@link AccessTokenStorage} that is used.
     */
    @NonNull
    public AccessTokenStorage getAccessTokenStorage() {
        return accessTokenStorage;
    }

    /**
     * @return {@link LoginCallback} that is used.
     */
    public LoginCallback getLoginCallback() {
        return callback;
    }

    @NonNull
    public SessionConfiguration getSessionConfiguration() {
        return sessionConfiguration;
    }

    /**
     * Gets session based on current {@link SessionConfiguration} and {@link AccessTokenStorage}.
     *
     * @return Session to use with API requests.
     * @throws IllegalStateException when not logged in
     */
    @NonNull
    public Session<?> getSession() {
        if (sessionConfiguration.getServerToken() != null) {
            return new ServerTokenSession(sessionConfiguration);
        } else if (accessTokenStorage.getAccessToken() != null) {
            return new AccessTokenSession(sessionConfiguration, accessTokenStorage);
        } else {
            throw new IllegalStateException("Tried to call getSession but not logged in or server token set.");
        }
    }

    /**
     * Determines if the Login Manager is authenticated based on set {@link SessionConfiguration}
     * and {@link AccessTokenStorage}
     *
     * @return true if authenticated, otherwise false;
     */
    public boolean isAuthenticated() {
        return (sessionConfiguration.getServerToken() != null || accessTokenStorage.getAccessToken() != null);
    }

    /**
     * By default, login will try to authenticate using Uber App, however if Uber app is unavailable and
     * {@link Scope}s requested are of type {@link Scope.ScopeType#PRIVILEGED}. User
     * will be redirected to the install or update the Uber app.
     * <p>
     * Set to true to redirect to be redirected to login Authorization Code instead,
     * see https://developer.uber.com/docs/authentication#section-step-one-authorize.
     * <p>
     *
     * @param redirectForAuthorizationCode true if should redirect, otherwise false
     * @return this instance of {@link LoginManager}
     * @deprecated, See Authorization Migration guide https://github.com/uber/rides-android-sdk#authentication-migration-version-08-and-above
     */
    @Deprecated
    public LoginManager setRedirectForAuthorizationCode(boolean redirectForAuthorizationCode) {
        this.redirectForAuthorizationCode = redirectForAuthorizationCode;
        setAuthCodeFlowEnabled(true);
        return this;
    }

    /**
     * By default, login will try to authenticate using Uber App, however if Uber app is unavailable and
     * {@link Scope}s requested are of type {@link Scope.ScopeType#PRIVILEGED}. User
     * will be redirected to login Authorization Code,
     * see https://developer.uber.com/docs/authentication#section-step-one-authorize.
     * <p>
     * If your redirect uri does not handle that flow, this flag indicates that the users will be asked to install or
     * update Uber app instead.
     *
     * @return true if redirect by default, otherwise false
     * @deprecated, See Authorization Migration guide https://github.com/uber/rides-android-sdk#authentication-migration-version-08-and-above
     */
    @Deprecated
    public boolean isRedirectForAuthorizationCode() {
        return redirectForAuthorizationCode;
    }


    /**
     * Enable the use of the Authorization Code Flow
     * (See <a href="https://developer.uber.com/docs/authentication#section-step-one-authorize">
     * https://developer.uber.com/docs/authentication#section-step-one-authorize</a>) instead of an
     * installation prompt for the Uber app  or Implicit Grant (WebView) as a login fallback mechanism.
     * <p>
     * Requires that the app's backend system is configured to support this flow and the redirect
     * URI is pointed correctly.
     *
     * @param authCodeFlowEnabled true for use of auth code flow, false to fallback to Uber app
     *                            installation
     * @return this instance of {@link LoginManager}.
     */
    public LoginManager setAuthCodeFlowEnabled(boolean authCodeFlowEnabled) {
        this.authCodeFlowEnabled = authCodeFlowEnabled;
        return this;
    }

    /**
     * Dictates the order of which Uber applications should be used for SSO.
     * This can be used to order Eats then Rides or vice-versa.
     * Only specified applications will be used, so specifying only Rides or Eats will ignore other apps if installed.
     * The default behavior (for backward compatibility) is Rides only.
     *
     * @param productFlowPriority A Collection of SupportedAppType indicating priority of SSO applications.
     * @return this instance of {@link LoginManager}.
     */
    public LoginManager setProductFlowPriority(@NonNull Collection<SupportedAppType> productFlowPriority) {
        this.productFlowPriority = new ArrayList<>(productFlowPriority);
        return this;
    }

    /**
     * Indicates the use of the Authorization Code Flow
     * (See <a href="https://developer.uber.com/docs/authentication#section-step-one-authorize">
     * https://developer.uber.com/docs/authentication#section-step-one-authorize</a>) instead of an
     * installation prompt for the Uber app  or Implicit Grant (WebView) as a login fallback mechanism.
     *
     * @return true if Auth Code Flow is enabled, otherwise false
     */
    public boolean isAuthCodeFlowEnabled() {
        return authCodeFlowEnabled;
    }

    /**
     * {@link Activity} result handler to be called from starting {@link Activity}. Stores {@link AccessToken} and
     * notifies consumer callback of login result.
     *
     * @param activity    Activity used to start login if Uber app is unavailable.
     * @param requestCode request code originally supplied to {@link Activity#startActivityForResult(Intent, int)}.
     * @param resultCode  result code from returning {@link Activity}.
     * @param data        data from returning {@link Activity}.
     */
    public void onActivityResult(
            @NonNull Activity activity,
            int requestCode,
            int resultCode,
            @Nullable Intent data) {
        if (requestCode != this.requestCode) {
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            handleResultOk(data);
        } else if (resultCode == Activity.RESULT_CANCELED) {
            handleResultCancelled(activity, data);
        }
    }

    /**
     * Generates the deeplink required to execute the SSO Flow
     *
     * @param activity the activity to execute the deeplink intent
     * @return the object that executes the deeplink
     */
    @VisibleForTesting
    SsoDeeplink getSsoDeeplink(@NonNull Activity activity) {
        return new SsoDeeplink.Builder(activity).clientId(sessionConfiguration.getClientId())
                .scopes(sessionConfiguration.getScopes())
                .customScopes(sessionConfiguration.getCustomScopes())
                .activityRequestCode(requestCode)
                .redirectUri(sessionConfiguration.getRedirectUri())
                .productFlowPriority(productFlowPriority)
                .build();
    }

    private void handleResultCancelled(
            @NonNull Activity activity,
            @Nullable Intent data) {// An error occurred during login

        if (data == null) {
            // User canceled login
            callback.onLoginCancel();
            return;
        }

        final String error = data.getStringExtra(EXTRA_ERROR);
        final AuthenticationError authenticationError
                = (error != null) ? AuthenticationError.fromString(error) : AuthenticationError.UNKNOWN;

        if (authenticationError.equals(AuthenticationError.CANCELLED)) {
            // User canceled login
            callback.onLoginCancel();
            return;
        } else if (authenticationError.equals(AuthenticationError.UNAVAILABLE) && isAuthCodeFlowEnabled()) {
            loginForAuthorizationCode(activity);
            return;
        } else if (authenticationError.equals(AuthenticationError.UNAVAILABLE) &&
                !AuthUtils.isPrivilegeScopeRequired(sessionConfiguration.getScopes())) {
            loginForImplicitGrant(activity);
            return;
        } else if (AuthenticationError.INVALID_APP_SIGNATURE.equals(authenticationError)) {
            AppProtocol appProtocol = new AppProtocol();
            String appSignature = appProtocol.getAppSignature(activity);
            if (appSignature == null) {
                Log.e(UberSdk.UBER_SDK_LOG_TAG, "There was an error obtaining your Application Signature. Please check "
                        + "your Application Signature and add it to the developer dashboard at https://developer.uber"
                        + ".com/dashboard");
            } else {
                Log.e(UberSdk.UBER_SDK_LOG_TAG, "Your Application Signature, " + appSignature
                        + ", does not match one of the registered Application Signatures on the developer dashboard. "
                        + "Check your settings at https://developer.uber.com/dashboard");
            }
        }
        callback.onLoginError(authenticationError);
    }

    private void handleResultOk(@Nullable Intent data) {
        if (data == null) {
            // Unknown error, should never occur
            callback.onLoginError(AuthenticationError.UNKNOWN);
            return;
        }

        final String authorizationCode = data.getStringExtra(EXTRA_CODE_RECEIVED);
        if (authorizationCode != null) {
            callback.onAuthorizationCodeReceived(authorizationCode);
        } else {
            AccessToken accessToken = AuthUtils.createAccessToken(data);
            accessTokenStorage.setAccessToken(accessToken);

            callback.onLoginSuccess(accessToken);

        }
    }
}
