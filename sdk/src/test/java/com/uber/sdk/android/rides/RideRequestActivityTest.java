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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;

import com.google.common.collect.ImmutableList;
import com.uber.sdk.android.rides.auth.AccessToken;
import com.uber.sdk.android.rides.auth.AccessTokenManager;
import com.uber.sdk.android.rides.auth.AuthenticationError;
import com.uber.sdk.android.rides.auth.LoginView;
import com.uber.sdk.android.rides.auth.Scope;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;

import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf; 

public class RideRequestActivityTest extends RobolectricTestBase {

    private RideRequestActivity mActivity;

    @Before
    public void setup() {
        UberSdk.initialize(RuntimeEnvironment.application, "clientId");
        UberSdk.setRedirectUri("localHost1324");
        Intent data = RideRequestActivity.newIntent(Robolectric.setupActivity(Activity.class), null);
        mActivity = Robolectric.buildActivity(RideRequestActivity.class).withIntent(data).create()
                .get();
    }

    @Test
    public void onNewIntent_withNullRideParameters_shouldGetDefaultParams() {
        Intent data = RideRequestActivity.newIntent(Robolectric.setupActivity(Activity.class), null);
        assertNotNull(data);

        RideParameters rideParameters = data.getParcelableExtra(RideRequestActivity.RIDE_PARAMETERS);
        assertNotNull(rideParameters);
    }

    @Test
    public void onNewIntent_withRideParameters_shouldGetRideParams() {
        Double PICKUP_LAT = 32.1234;
        Double PICKUP_LONG = -122.3456;
        Double DROPOFF_LAT = 32.5678;
        Double DROPOFF_LONG = -122.6789;
        RideParameters rideParameters = new RideParameters.Builder()
                .setPickupLocation(PICKUP_LAT, PICKUP_LONG, null, null)
                .setDropoffLocation(DROPOFF_LAT, DROPOFF_LONG, null, null)
                .build();
        Intent data = RideRequestActivity.newIntent(Robolectric.setupActivity(Activity.class), rideParameters);
        assertNotNull(data);

        RideParameters resultParameters = data.getParcelableExtra(RideRequestActivity.RIDE_PARAMETERS);
        assertNotNull(resultParameters);
        assertEquals(rideParameters.getPickupLatitude(), resultParameters.getPickupLatitude());
        assertEquals(rideParameters.getPickupLongitude(), resultParameters.getPickupLongitude());
        assertEquals(rideParameters.getDropoffLatitude(), resultParameters.getDropoffLatitude());
        assertEquals(rideParameters.getDropoffLongitude(), resultParameters.getDropoffLongitude());
    }

    @Test
    public void onLoad_whenNullUserAgent_shouldAddRideWidgetUserAgent() {
        Double PICKUP_LAT = 32.1234;
        Double PICKUP_LONG = -122.3456;
        Double DROPOFF_LAT = 32.5678;
        Double DROPOFF_LONG = -122.6789;
        RideParameters rideParameters = new RideParameters.Builder()
                .setPickupLocation(PICKUP_LAT, PICKUP_LONG, null, null)
                .setDropoffLocation(DROPOFF_LAT, DROPOFF_LONG, null, null)
                .build();
        Intent data = RideRequestActivity.newIntent(Robolectric.setupActivity(Activity.class), rideParameters);
        mActivity = Robolectric.buildActivity(RideRequestActivity.class).withIntent(data).create().get();

        Date expirationDate = new Date(1458770906206l);
        String tokenString = "accessToken1234";
        AccessToken accessToken = new AccessToken(expirationDate, ImmutableList.of(Scope.RIDE_WIDGETS), tokenString);
        mActivity.onLoginSuccess(accessToken);

        assertEquals("rides-android-v0.3.0-ride_request_widget", mActivity.mRideRequestView.mRideParameters.getUserAgent());
    }

    @Test
    public void onLoad_withUserAgentInRideParametersButton_shouldNotGetOverridden() {
        String userAgent = "rides-android-v0.3.0-button";
        RideParameters rideParameters = new RideParameters.Builder().build();
        rideParameters.setUserAgent(userAgent);
        Intent data = RideRequestActivity.newIntent(Robolectric.setupActivity(Activity.class), rideParameters);
        mActivity = Robolectric.buildActivity(RideRequestActivity.class).withIntent(data).create().get();
        assertEquals(userAgent, mActivity.mRideRequestView.mRideParameters.getUserAgent());
    }

    @Test
    public void onCreate_withNullRideParameters_shouldCreateDefaultParamsAndLoad() {
        ShadowActivity shadowActivity = shadowOf(mActivity);
        mActivity = Robolectric.buildActivity(RideRequestActivity.class).withIntent(new Intent()).create().get();
        assertNull(shadowActivity.getResultIntent());
        assertFalse(shadowActivity.isFinishing());
    }

    @Test
    public void onLoad_whenAccessTokenGeneratedFromLogin_shouldSaveAccessTokenResult() {
        Date expirationDate = new Date(1458770906206l);
        String tokenString = "accessToken1234";
        AccessToken accessToken = new AccessToken(expirationDate, ImmutableList.of(Scope.RIDE_WIDGETS), tokenString);

        mActivity.onLoginSuccess(accessToken);

        AccessToken resultAccessToken = new AccessTokenManager(mActivity).getAccessToken();
        assertNotNull(resultAccessToken);
        assertEquals(resultAccessToken.getExpirationTime(), expirationDate);
        assertEquals(resultAccessToken.getToken(), tokenString);
        assertEquals(resultAccessToken.getScopes().size(), 1);
        assertTrue(resultAccessToken.getScopes().contains(Scope.RIDE_WIDGETS));
    }

    @Test
    public void onLoad_whenLoginErrorOccurs_shouldReturnErrorResultIntent() {
        ShadowActivity shadowActivity = shadowOf(mActivity);
        mActivity.onLoginError(AuthenticationError.MISMATCHING_REDIRECT_URI);
        ShadowAlertDialog shadowAlertDialog = shadowOf(mActivity.mAuthenticationErrorDialog);

        String message = "There was a problem authenticating you.";
        assertEquals(message, shadowAlertDialog.getMessage());
        mActivity.mAuthenticationErrorDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();

        Assert.assertNotNull(shadowActivity.getResultIntent());
        assertEquals(shadowActivity.getResultCode(), Activity.RESULT_CANCELED);
        assertEquals(AuthenticationError.MISMATCHING_REDIRECT_URI, shadowActivity.getResultIntent().getSerializableExtra
                (RideRequestActivity.AUTHENTICATION_ERROR));
    }

    @Test
    public void onLoad_whenLoginCanceled_shouldReturnCanceledResultIntent() {
        ShadowActivity shadowActivity = shadowOf(mActivity);
        mActivity.onLoginCancel();

        assertNull(shadowActivity.getResultIntent());
        assertEquals(shadowActivity.getResultCode(), Activity.RESULT_CANCELED);
    }

    @Test
    public void onLoad_whenRideRequestViewAuthorizationErrorOccurs_shouldAttemptLoginLoad() {
        mActivity.mLoginView = mock(LoginView.class);
        mActivity.mAccessTokenManager = mock(AccessTokenManager.class);
        ShadowActivity shadowActivity = shadowOf(mActivity);
        mActivity.onErrorReceived(RideRequestViewError.UNAUTHORIZED);

        assertNull(shadowActivity.getResultIntent());
        verify(mActivity.mAccessTokenManager, times(1)).removeAccessToken();
        verify(mActivity.mLoginView, times(1)).load();
    }

    @Test
    public void onLoad_whenSomeRideRequestViewErrorOccurs_shouldReturnResultIntentError() {
        ShadowActivity shadowActivity = shadowOf(mActivity);
        mActivity.onErrorReceived(RideRequestViewError.UNKNOWN);
        ShadowAlertDialog shadowAlertDialog = shadowOf(mActivity.mRideRequestErrorDialog);

        String message = "The Ride Request Widget encountered a problem.";
        assertEquals(message, shadowAlertDialog.getMessage());
        mActivity.mRideRequestErrorDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();

        assertNotNull(shadowActivity.getResultIntent());
        assertEquals(Activity.RESULT_CANCELED, shadowActivity.getResultCode());
        assertEquals(RideRequestViewError.UNKNOWN, shadowActivity.getResultIntent().getSerializableExtra
                (RideRequestActivity.RIDE_REQUEST_ERROR));
    }

    @Test
    public void onLoad_whenErrorOccursAndUserHitsBackWhenAlertShows_shouldReturnResultIntentError() {
        ShadowActivity shadowActivity = shadowOf(mActivity);
        mActivity.onErrorReceived(RideRequestViewError.UNKNOWN);
        ShadowAlertDialog shadowAlertDialog = shadowOf(mActivity.mRideRequestErrorDialog);

        String message = "The Ride Request Widget encountered a problem.";
        assertEquals(message, shadowAlertDialog.getMessage());
        mActivity.mRideRequestErrorDialog.onBackPressed();

        assertNotNull(shadowActivity.getResultIntent());
        assertEquals(Activity.RESULT_CANCELED, shadowActivity.getResultCode());
        assertEquals(RideRequestViewError.UNKNOWN, shadowActivity.getResultIntent().getSerializableExtra
                (RideRequestActivity.RIDE_REQUEST_ERROR));
    }
}
