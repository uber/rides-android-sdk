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

package com.uber.sdk.android.rides;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.uber.sdk.android.core.auth.AccessTokenManager;
import com.uber.sdk.android.core.auth.AuthenticationError;
import com.uber.sdk.android.core.auth.LoginCallback;
import com.uber.sdk.android.core.auth.LoginManager;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.AccessTokenStorage;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.core.client.AccessTokenSession;
import com.uber.sdk.core.client.SessionConfiguration;

import java.util.Arrays;

/**
 * The {@link RideRequestActivity} provides the entire flow to have a user authorize with their Uber account
 * and display the {@link RideRequestView} once authorized in one activity.
 */
public class RideRequestActivity extends Activity implements LoginCallback, RideRequestViewCallback {

    /**
     * Key for a {@link AuthenticationError} to be passed back to the calling activity.
     */
    public static final String AUTHENTICATION_ERROR = "authentication_error";

    /**
     * Key for a {@link RideRequestViewError} to be passed back to the calling activity.
     */
    public static final String RIDE_REQUEST_ERROR = "ride_request_error";

    @VisibleForTesting
    static final int LOGIN_REQUEST_CODE = 1112;

    private static final int REQUEST_FINE_LOCATION_PERMISSION_CODE = 1002;
    private static final String USER_AGENT_RIDE_WIDGET = String.format("rides-android-v%s-ride_request_widget",
            BuildConfig.VERSION_NAME);

    @VisibleForTesting static final String RIDE_PARAMETERS = "ride_parameters";
    static final String EXTRA_LOGIN_CONFIGURATION = "login_configuration";
    static final String EXTRA_ACCESS_TOKEN_STORAGE_KEY = "access_token_storage_key";

    @VisibleForTesting AccessTokenStorage accessTokenStorage;
    @Nullable @VisibleForTesting AlertDialog authenticationErrorDialog;
    @Nullable @VisibleForTesting AlertDialog rideRequestErrorDialog;
    @VisibleForTesting RideRequestView rideRequestView;
    @VisibleForTesting LoginManager loginManager;
    SessionConfiguration sessionConfiguration;

    /**
     * Creates a new {@link Intent} to be passed in to this activity with all the required information.
     *
     * @param context the {@link Context} that will be launching this activity
     * @param rideParameters the optional {@link RideParameters} containing information to populate the {@link RideRequestView}
     * @param loginConfiguration required when RideRequestActivity needs to start authentication flow
     * @param accessTokenStorageKey optional key to lookup access token from {@link com.uber.sdk.core.auth.AccessTokenStorage}
     * @return new {@link Intent} with the necessary parameters for this activity
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context,
            @Nullable RideParameters rideParameters,
            @NonNull SessionConfiguration loginConfiguration,
            @Nullable String accessTokenStorageKey) {
        Intent data = new Intent(context, RideRequestActivity.class);

        if (rideParameters == null) {
            rideParameters = new RideParameters.Builder().build();
        }

        data.putExtra(RIDE_PARAMETERS, rideParameters);
        data.putExtra(EXTRA_LOGIN_CONFIGURATION, loginConfiguration);
        data.putExtra(EXTRA_ACCESS_TOKEN_STORAGE_KEY, accessTokenStorageKey);
        return data;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ub__ride_request_activity);

        String accessTokenStorageKey = getIntent()
                .getExtras()
                .getString(EXTRA_ACCESS_TOKEN_STORAGE_KEY, AccessTokenManager.ACCESS_TOKEN_DEFAULT_KEY);

        rideRequestView = (RideRequestView) findViewById(R.id.ub__ride_request_view);
        accessTokenStorage = new AccessTokenManager(this, accessTokenStorageKey);

        RideParameters rideParameters = getIntent().getParcelableExtra(RIDE_PARAMETERS);
        if (rideParameters == null) {
            rideParameters = new RideParameters.Builder().build();
        }

        if (rideParameters.getUserAgent() == null) {
            rideParameters.setUserAgent(USER_AGENT_RIDE_WIDGET);
        }

        SessionConfiguration loginConfiguration = (SessionConfiguration) getIntent().getSerializableExtra(EXTRA_LOGIN_CONFIGURATION);
        sessionConfiguration = loginConfiguration
                .newBuilder()
                .setScopes(Arrays.asList(Scope.RIDE_WIDGETS))
                .build();

        loginManager = new LoginManager(accessTokenStorage, this, sessionConfiguration, LOGIN_REQUEST_CODE);
        rideRequestView.setRideParameters(rideParameters);
        rideRequestView.setRideRequestViewCallback(this);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_FINE_LOCATION_PERMISSION_CODE);
        } else {
            load();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN_REQUEST_CODE) {
            loginManager.onActivityResult(this, requestCode, resultCode, data);
        }
    }

    @Override
    public void onErrorReceived(@NonNull RideRequestViewError error) {
        rideRequestView.cancelLoad();
        Intent data = new Intent();
        data.putExtra(RIDE_REQUEST_ERROR, error);
        switch (error) {
            case CONNECTIVITY_ISSUE:
                rideRequestErrorDialog = buildRetryAlert(
                        R.string.ub__ride_request_activity_widget_error,
                        R.string.ub__ride_error_try_again,
                        android.R.string.cancel,
                        data);
                rideRequestErrorDialog.show();
                break;
            case NO_ACCESS_TOKEN:
            case UNAUTHORIZED:
                accessTokenStorage.removeAccessToken();
                login();
                break;
            default:
                rideRequestErrorDialog = buildErrorAlert(R.string.ub__ride_request_activity_widget_error,
                        android.R.string.ok,
                        data);
                rideRequestErrorDialog.show();
        }
    }

    @Override
    public void onLoginCancel() {
        setResult(RESULT_CANCELED, null);
        finish();
    }

    @Override
    public void onLoginError(@NonNull AuthenticationError error) {
        Intent data = new Intent();
        data.putExtra(AUTHENTICATION_ERROR, error);
        if (AuthenticationError.CONNECTIVITY_ISSUE.equals(error)) {
            authenticationErrorDialog = buildRetryAlert(
                    R.string.ub__ride_request_activity_authentication_error,
                    R.string.ub__ride_error_try_again,
                    android.R.string.cancel,
                    data);
        } else {
            authenticationErrorDialog = buildErrorAlert(
                    R.string.ub__ride_request_activity_authentication_error,
                    android.R.string.ok,
                    data);
        }
        authenticationErrorDialog.show();
    }

    @Override
    public void onLoginSuccess(@NonNull AccessToken accessToken) {
        accessTokenStorage.setAccessToken(accessToken);
        load();
    }

    @Override
    public void onAuthorizationCodeReceived(@NonNull String authorizationCode) {
        //This code should be unreachable, as we request for implicit grant.
        final Intent error = new Intent().putExtra(AUTHENTICATION_ERROR, AuthenticationError.INVALID_FLOW_ERROR);
        authenticationErrorDialog = buildErrorAlert(R.string.ub__ride_request_activity_authentication_error,
                android.R.string.ok, error);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == REQUEST_FINE_LOCATION_PERMISSION_CODE) {
            load();
        }
    }

    /**
     * Loads the appropriate view in the activity based on whether user is successfully authorized or not.
     */
    private void load() {
        AccessToken accessToken = accessTokenStorage.getAccessToken();

        if (accessToken != null) {
            AccessTokenSession session = new AccessTokenSession(sessionConfiguration,
                    accessTokenStorage);
            rideRequestView.setSession(session);

            loadRideRequestView();
        } else {
            login();
        }
    }

    private void login() {
        loginManager.login(this);
    }

    /**
     * Loads the {@link RideRequestView}.
     */
    private void loadRideRequestView() {
        rideRequestView.load();
    }

    /**
     * Builds an {@link AlertDialog} to the user to indicate an error and dismisses activity.
     *
     * @param messageTextId the message content {@link StringRes} text ID
     * @param positiveButtonTextId the positive button {@link StringRes} text ID
     * @param intent the {@link Intent} to pass in the result
     * @return an {@link AlertDialog} to show
     */
    @NonNull
    private AlertDialog buildErrorAlert(
            @StringRes int messageTextId,
            @StringRes int positiveButtonTextId,
            final Intent intent) {
        return new AlertDialog.Builder(this)
                .setMessage(messageTextId)
                .setPositiveButton(positiveButtonTextId, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED, intent);
                        finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        setResult(RESULT_CANCELED, intent);
                        finish();
                    }
                })
                .create();
    }

    /**
     * Builds an {@link AlertDialog} to the user to indicate a {@link RideRequestViewError#CONNECTIVITY_ISSUE}
     * and allow the retry of the button.
     *
     * @param messageTextId the message content {@link StringRes} text ID
     * @param positiveButtonTextId the positive button {@link StringRes} text ID
     * @param negativeButtonTextId the negative button {@link StringRes} text ID
     * @param intent the {@link Intent} to pass in the result
     * @return an {@link AlertDialog} to show
     */
    @NonNull
    private AlertDialog buildRetryAlert(
            @StringRes int messageTextId,
            @StringRes int positiveButtonTextId,
            @StringRes int negativeButtonTextId,
            final Intent intent) {
        return new AlertDialog.Builder(this)
                .setMessage(messageTextId)
                .setPositiveButton(positiveButtonTextId, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        load();
                    }
                })
                .setNegativeButton(negativeButtonTextId, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setResult(RESULT_CANCELED, intent);
                        finish();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        setResult(RESULT_CANCELED, intent);
                        finish();
                    }
                })
                .create();
    }
}
