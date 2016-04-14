package com.uber.sdk.android.rides.auth;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Manages user login via OAuth 2.0 Implicit Grant.  Be sure to call
 * {@link LoginManager#onActivityResult(int, int, Intent, LoginCallback)} in your
 * {@link Activity#onActivityResult(int, int, Intent)} to forward along LoginResults.
 */
public class LoginManager {

    private static final int REQUEST_CODE_LOGIN_DEFAULT = 1001;

    /**
     * Used to retrieve the {@link AccessToken} from a return {@link Intent}.
     */
    public static final String ACCESS_TOKEN_KEY = "access_token_key";

    /**
     * Used to retrieve the {@link AuthenticationError} from a return {@link Intent}.
     */
    public static final String LOGIN_ERROR_KEY = "login_error_key";
    
    /**
     * Used to store and retrieve the {@link Scope}s during a login attempt.
     */
    public static final String SCOPES_KEY = "scopes_key";

    private int mRequestCode = REQUEST_CODE_LOGIN_DEFAULT;

    @NonNull @VisibleForTesting AccessTokenManager mAccessTokenManager;

    public LoginManager(@NonNull AccessTokenManager accessTokenManager) {
        mAccessTokenManager = accessTokenManager;
    }

    /**
     * Logs a user in, requesting approval for specified {@link Scope}s.  Uses a request code of 1001.
     *
     * @param activity the activity used to start the {@link LoginActivity}.
     * @param scopes the requested {@link Scope}s for approval.
     */
    public void loginWithScopes(@NonNull Activity activity, @NonNull Collection<Scope> scopes) {
        loginWithScopes(activity, scopes, REQUEST_CODE_LOGIN_DEFAULT);
    }

    /**
     * Logs a user in, requesting approval for specified {@link Scope}s.
     *
     * @param activity the activity used to start the {@link LoginActivity}.
     * @param scopes the requested {@link Scope}s for approval.
     * @param requestCode the code to be used when starting {@link LoginActivity} for a result.
     */
    public void loginWithScopes(@NonNull Activity activity, @NonNull Collection<Scope> scopes, int requestCode) {
        mRequestCode = requestCode;

        Intent intent = new Intent(activity, LoginActivity.class);
        ArrayList<String> scopesList = new ArrayList<>(AuthUtils.scopeCollectionToStringSet(scopes));
        intent.putExtra(SCOPES_KEY, scopesList);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * {@link Activity} result handler to be called from starting {@link Activity}. Stores {@link AccessToken} and
     * notifies consumer callback of login result.
     *
     * @param requestCode request code originally supplied to {@link Activity#startActivityForResult(Intent, int)}.
     * @param resultCode result code from returning {@link Activity}.
     * @param data data from returning {@link Activity}.
     * @param callback callback to notify of the login result.
     */
    public void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data,
            @Nullable LoginCallback callback) {
        if (requestCode != mRequestCode) {
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                // Access Token received, store and notify callback
                AccessToken accessToken = data.getParcelableExtra(ACCESS_TOKEN_KEY);
                mAccessTokenManager.setAccessToken(accessToken);
                if (callback != null) {
                    callback.onLoginSuccess(accessToken);
                }
            } else if (callback != null) {
                // Unknown error, should never occur
                callback.onLoginError(AuthenticationError.UNKNOWN);
            }
        } else if (resultCode == Activity.RESULT_CANCELED && callback != null) {
            if (data == null) {
                // User canceled login
                callback.onLoginCancel();
            } else {
                // An error occurred during login
                AuthenticationError authenticationError = AuthenticationError.UNKNOWN;
                Serializable serializableError = data.getSerializableExtra(LOGIN_ERROR_KEY);
                if (serializableError instanceof AuthenticationError) {
                    authenticationError = (AuthenticationError) serializableError;
                }
                callback.onLoginError(authenticationError);
            }
        }
    }
}
