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
import com.uber.sdk.android.core.auth.AccessTokenManager;
import com.uber.sdk.android.core.auth.AuthenticationError;
import com.uber.sdk.android.core.auth.LoginManager;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.AccessTokenStorage;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.core.client.SessionConfiguration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowAlertDialog;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

public class RideRequestActivityTest extends RobolectricTestBase {

    private RideRequestActivity activity;

    @Before
    public void setup() {
        Intent data = RideRequestActivity.newIntent(Robolectric.setupActivity(Activity.class), null,
                new SessionConfiguration.Builder().setClientId("clientId").build(),
                null);
        activity = Robolectric.buildActivity(RideRequestActivity.class, data)
                .create()
                .get();
    }

    @Test
    public void onNewIntent_withNullRideParameters_shouldGetDefaultParams() {
        Intent data = RideRequestActivity.newIntent(Robolectric.setupActivity(Activity.class), null,
                new SessionConfiguration.Builder().setClientId("clientId").build(),
                null);
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
        Intent data = RideRequestActivity.newIntent(Robolectric.setupActivity(Activity.class),
                rideParameters,
                new SessionConfiguration.Builder().setClientId("clientId").build(),
                null);
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
        Intent data = RideRequestActivity.newIntent(Robolectric.setupActivity(Activity.class),
                rideParameters,
                new SessionConfiguration.Builder().setClientId("clientId").build(),
                null);
        activity = Robolectric.buildActivity(RideRequestActivity.class, data).create().get();

        String tokenString = "accessToken1234";
        AccessToken accessToken = new AccessToken(2592000, ImmutableList.of(Scope.RIDE_WIDGETS), tokenString,
                "refreshToken", "tokenType");
        activity.onLoginSuccess(accessToken);

        assertEquals(String.format("rides-android-v%s-ride_request_widget", BuildConfig.VERSION_NAME),
                activity.rideRequestView.rideParameters.getUserAgent());
    }

    @Test
    public void onLoad_withUserAgentInRideParametersButton_shouldNotGetOverridden() {
        String userAgent = String.format("rides-android-v%s-button", BuildConfig.VERSION_NAME);
        RideParameters rideParameters = new RideParameters.Builder().build();
        rideParameters.setUserAgent(userAgent);
        Intent data = RideRequestActivity.newIntent(Robolectric.setupActivity(Activity.class),
                rideParameters,
                new SessionConfiguration.Builder().setClientId("clientId").build(),
                null);
        activity = Robolectric.buildActivity(RideRequestActivity.class, data).create().get();
        assertEquals(userAgent, activity.rideRequestView.rideParameters.getUserAgent());
    }

    @Test
    public void onCreate_withNullRideParameters_shouldCreateDefaultParamsAndLoad() {
        ShadowActivity shadowActivity = shadowOf(activity);
        Intent intent = new Intent();
        intent.putExtra(RideRequestActivity.EXTRA_LOGIN_CONFIGURATION, new SessionConfiguration.Builder().setClientId("clientId").build());
        activity = Robolectric.buildActivity(RideRequestActivity.class, intent).create().get();
        assertNull(shadowActivity.getResultIntent());
        assertFalse(shadowActivity.isFinishing());
    }

    @Test
    public void onLoad_whenAccessTokenGeneratedFromLogin_shouldSaveAccessTokenResult() {
        String tokenString = "accessToken1234";
        AccessToken accessToken = new AccessToken(2592000, ImmutableList.of(Scope.RIDE_WIDGETS), tokenString,
                "refreshToken", "tokenType");

        activity.onLoginSuccess(accessToken);

        AccessToken resultAccessToken = new AccessTokenManager(activity).getAccessToken();
        assertNotNull(resultAccessToken);
        assertEquals(resultAccessToken.getExpiresIn(), 2592000);
        assertEquals(resultAccessToken.getToken(), tokenString);
        assertEquals(resultAccessToken.getScopes().size(), 1);
        assertTrue(resultAccessToken.getScopes().contains(Scope.RIDE_WIDGETS));
    }

    @Test
    public void onLoad_whenLoginErrorOccurs_shouldReturnErrorResultIntent() {
        ShadowActivity shadowActivity = shadowOf(activity);
        activity.onLoginError(AuthenticationError.MISMATCHING_REDIRECT_URI);
        ShadowAlertDialog shadowAlertDialog = shadowOf(activity.authenticationErrorDialog);

        String message = "There was a problem authenticating you.";
        assertEquals(message, shadowAlertDialog.getMessage());
        activity.authenticationErrorDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();

        Assert.assertNotNull(shadowActivity.getResultIntent());
        assertEquals(shadowActivity.getResultCode(), Activity.RESULT_CANCELED);
        assertEquals(AuthenticationError.MISMATCHING_REDIRECT_URI, shadowActivity.getResultIntent().getSerializableExtra
                (RideRequestActivity.AUTHENTICATION_ERROR));
    }

    @Test
    public void onLoad_whenLoginCanceled_shouldReturnCanceledResultIntent() {
        ShadowActivity shadowActivity = shadowOf(activity);
        activity.onLoginCancel();

        assertNull(shadowActivity.getResultIntent());
        assertEquals(shadowActivity.getResultCode(), Activity.RESULT_CANCELED);
    }

    @Test
    public void onLoad_whenRideRequestViewAuthorizationErrorOccurs_shouldAttemptLoginLoad() {
        activity.loginManager = mock(LoginManager.class);
        activity.accessTokenStorage = mock(AccessTokenStorage.class);
        ShadowActivity shadowActivity = shadowOf(activity);
        activity.onErrorReceived(RideRequestViewError.UNAUTHORIZED);

        assertNull(shadowActivity.getResultIntent());
        verify(activity.accessTokenStorage, times(1)).removeAccessToken();
        verify(activity.loginManager).login(refEq(activity));
    }

    @Test
    public void onLoad_whenSomeRideRequestViewErrorOccurs_shouldReturnResultIntentError() {
        ShadowActivity shadowActivity = shadowOf(activity);
        activity.onErrorReceived(RideRequestViewError.UNKNOWN);
        ShadowAlertDialog shadowAlertDialog = shadowOf(activity.rideRequestErrorDialog);

        String message = "The Ride Request Widget encountered a problem.";
        assertEquals(message, shadowAlertDialog.getMessage());
        activity.rideRequestErrorDialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();

        assertNotNull(shadowActivity.getResultIntent());
        assertEquals(Activity.RESULT_CANCELED, shadowActivity.getResultCode());
        assertEquals(RideRequestViewError.UNKNOWN, shadowActivity.getResultIntent().getSerializableExtra
                (RideRequestActivity.RIDE_REQUEST_ERROR));
    }

    @Test
    public void onLoad_whenErrorOccursAndUserHitsBackWhenAlertShows_shouldReturnResultIntentError() {
        ShadowActivity shadowActivity = shadowOf(activity);
        activity.onErrorReceived(RideRequestViewError.UNKNOWN);
        ShadowAlertDialog shadowAlertDialog = shadowOf(activity.rideRequestErrorDialog);

        String message = "The Ride Request Widget encountered a problem.";
        assertEquals(message, shadowAlertDialog.getMessage());
        activity.rideRequestErrorDialog.onBackPressed();

        assertNotNull(shadowActivity.getResultIntent());
        assertEquals(Activity.RESULT_CANCELED, shadowActivity.getResultCode());
        assertEquals(RideRequestViewError.UNKNOWN, shadowActivity.getResultIntent().getSerializableExtra
                (RideRequestActivity.RIDE_REQUEST_ERROR));
    }
}
