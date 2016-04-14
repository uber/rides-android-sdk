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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.uber.sdk.android.rides.auth.AccessToken;
import com.uber.sdk.android.rides.auth.AccessTokenManager;
import com.uber.sdk.android.rides.auth.AuthenticationError;
import com.uber.sdk.android.rides.auth.LoginCallback;
import com.uber.sdk.android.rides.auth.LoginView;
import com.uber.sdk.android.rides.auth.Scope;

import java.util.ArrayList;
import java.util.Collection;

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

    private static final int REQUEST_FINE_LOCATION_PERMISSION_CODE = 1002;
    private static final String USER_AGENT_RIDE_WIDGET = "rides-android-v0.3.0-ride_request_widget";

    @VisibleForTesting static final String RIDE_PARAMETERS = "ride_parameters";

    @VisibleForTesting AccessTokenManager mAccessTokenManager;
    @Nullable @VisibleForTesting AlertDialog mAuthenticationErrorDialog;
    @Nullable @VisibleForTesting AlertDialog mRideRequestErrorDialog;
    @VisibleForTesting LoginView mLoginView;
    @VisibleForTesting RideRequestView mRideRequestView;

    /**
     * Creates a new {@link Intent} to be passed in to this activity with all the required information.
     *
     * @param context the {@link Context} that will be launching this activity
     * @param rideParameters the optional {@link RideParameters} containing information to populate the {@link RideRequestView}
     * @return new {@link Intent} with the necessary parameters for this activity
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context, @Nullable RideParameters rideParameters) {
        Intent data = new Intent(context, RideRequestActivity.class);

        if (rideParameters == null) {
            rideParameters = new RideParameters.Builder().build();
        }

        data.putExtra(RIDE_PARAMETERS, rideParameters);

        return data;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ub__ride_request_activity);
        mLoginView = (LoginView) findViewById(R.id.ub__login_view);
        mRideRequestView = (RideRequestView) findViewById(R.id.ub__ride_request_view);
        mAccessTokenManager = new AccessTokenManager(this);

        RideParameters rideParameters = getIntent().getParcelableExtra(RIDE_PARAMETERS);
        if (rideParameters == null) {
            rideParameters = new RideParameters.Builder().build();
        }

        if (rideParameters.getUserAgent() == null) {
            rideParameters.setUserAgent(USER_AGENT_RIDE_WIDGET);
        }

        Collection<Scope> scopes = new ArrayList<>();
        scopes.add(Scope.RIDE_WIDGETS);

        mLoginView.setScopes(scopes);
        mLoginView.setLoginCallback(this);
        mRideRequestView.setRideParameters(rideParameters);
        mRideRequestView.setRideRequestViewCallback(this);

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_FINE_LOCATION_PERMISSION_CODE);
        } else {
            load();
        }
    }

    @Override
    public void onErrorReceived(@NonNull RideRequestViewError error) {
        mRideRequestView.cancelLoad();
        Intent data = new Intent();
        data.putExtra(RIDE_REQUEST_ERROR, error);
        switch(error) {
            case CONNECTIVITY_ISSUE:
                mRideRequestErrorDialog = buildRetryAlert(
                        R.string.ride_request_activity_widget_error,
                        R.string.ride_error_try_again,
                        android.R.string.cancel,
                        data);
                mRideRequestErrorDialog.show();
                break;
            case NO_ACCESS_TOKEN:
            case UNAUTHORIZED:
                mAccessTokenManager.removeAccessToken();
                loadLoginView();
                break;
            default:
                mRideRequestErrorDialog = buildErrorAlert(R.string.ride_request_activity_widget_error,
                        android.R.string.ok,
                        data);
                mRideRequestErrorDialog.show();
        }
    }

    @Override
    public void onLoginCancel() {
        setResult(RESULT_CANCELED, null);
        finish();
    }

    @Override
    public void onLoginError(@NonNull AuthenticationError error) {
        mLoginView.cancelLoad();
        Intent data = new Intent();
        data.putExtra(AUTHENTICATION_ERROR, error);
        if (AuthenticationError.CONNECTIVITY_ISSUE.equals(error)) {
            mAuthenticationErrorDialog = buildRetryAlert(
                    R.string.ride_request_activity_authentication_error,
                    R.string.ride_error_try_again,
                    android.R.string.cancel,
                    data);
        } else {
            mAuthenticationErrorDialog = buildErrorAlert(
                    R.string.ride_request_activity_authentication_error,
                    android.R.string.ok,
                    data);
        }
        mAuthenticationErrorDialog.show();
    }

    @Override
    public void onLoginSuccess(@NonNull AccessToken accessToken) {
        mAccessTokenManager.setAccessToken(accessToken);
        load();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
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
        AccessToken accessToken = mAccessTokenManager.getAccessToken();
        
        if (accessToken != null) {
            mRideRequestView.setAccessToken(accessToken);
            loadRideRequestView();
        } else {
            loadLoginView();
        }
    }

    /**
     * Loads the {@link LoginView}.
     */
    private void loadLoginView() {
        mLoginView.setVisibility(View.VISIBLE);
        mRideRequestView.setVisibility(View.INVISIBLE);
        mLoginView.load();
    }

    /**
     * Loads the {@link RideRequestView}.
     */
    private void loadRideRequestView() {
        mRideRequestView.setVisibility(View.VISIBLE);
        mLoginView.setVisibility(View.INVISIBLE);
        mRideRequestView.load();
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
    private AlertDialog buildErrorAlert(@StringRes int messageTextId,
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
    private AlertDialog buildRetryAlert(@StringRes int messageTextId,
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
