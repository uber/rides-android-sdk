/*
 * Copyright (c) 2015 Uber Technologies, Inc.
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

package com.uber.sdk.android.rides.samples;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestActivity;
import com.uber.sdk.android.rides.RideRequestActivityBehavior;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.android.rides.RideRequestViewError;
import com.uber.sdk.android.rides.UberSdk;
import com.uber.sdk.android.rides.auth.AccessToken;
import com.uber.sdk.android.rides.auth.AccessTokenManager;
import com.uber.sdk.android.rides.auth.AuthenticationError;

/**
 * Activity that demonstrates how to use a {@link RideRequestButton}.
 */
public class SampleActivity extends AppCompatActivity {

    private static final String DROPOFF_ADDR = "One Embarcadero Center, San Francisco";
    private static final Double DROPOFF_LAT = 37.795079;
    private static final Double DROPOFF_LONG = -122.397805;
    private static final String DROPOFF_NICK = "Embarcadero";
    private static final String ERROR_LOG_TAG = "UberSDK-SampleActivity";
    private static final String PICKUP_ADDR = "1455 Market Street, San Francisco";
    private static final Double PICKUP_LAT = 37.775304;
    private static final Double PICKUP_LONG = -122.417522;
    private static final String PICKUP_NICK = "Uber HQ";
    private static final String UBERX_PRODUCT_ID = "a1111c8c-c720-46c3-8534-2fcdd730040d";
    private static final int WIDGET_REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        UberSdk.initialize(this, "insert_your_client_id_here");
        UberSdk.setRedirectUri("insert_your_redirect_uri_here");

        // Optional: to use the SDK in China, set the region property
        // See https://developer.uber.com/docs/china for more details.
        // UberSdk.setRegion(UberSdk.Region.CHINA);

        if (UberSdk.getClientId().equals("insert_your_client_id_here")) {
            throw new IllegalArgumentException("Please insert your client ID for UberSdk initialization in "
                    + "SampleActivity.");
        }

        String redirectUri = UberSdk.getRedirectUri();
        if (redirectUri != null && redirectUri.equals("insert_your_redirect_uri_here")) {
            throw new IllegalArgumentException("Please insert your redirect URI for UberSdk implicit grant in "
                    + "SampleActivity.");
        }

        RideParameters rideParameters = new RideParameters.Builder()
                .setProductId(UBERX_PRODUCT_ID)
                .setPickupLocation(PICKUP_LAT, PICKUP_LONG, PICKUP_NICK, PICKUP_ADDR)
                .setDropoffLocation(DROPOFF_LAT, DROPOFF_LONG, DROPOFF_NICK, DROPOFF_ADDR)
                .build();

        // This button demonstrates deep-linking to the Uber app (default button behavior).
        RideRequestButton uberButtonBlack = (RideRequestButton) findViewById(R.id.uber_button_black);
        uberButtonBlack.setRideParameters(rideParameters);

        // This button demonstrates launching the RideRequestActivity (customized button behavior).
        // You can optionally setRideParameters for pre-filled pickup and dropoff locations.
        RideRequestButton uberButtonWhite = (RideRequestButton) findViewById(R.id.uber_button_white);
        RideRequestActivityBehavior rideRequestActivityBehavior = new RideRequestActivityBehavior(this,
                WIDGET_REQUEST_CODE);
        uberButtonWhite.setRequestBehavior(rideRequestActivityBehavior);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WIDGET_REQUEST_CODE && resultCode == Activity.RESULT_CANCELED && data != null) {
            if (data.getSerializableExtra(RideRequestActivity.AUTHENTICATION_ERROR) != null) {
                AuthenticationError error = (AuthenticationError) data.getSerializableExtra(RideRequestActivity
                        .AUTHENTICATION_ERROR);
                Toast.makeText(SampleActivity.this, "Auth error " + error.name(), Toast.LENGTH_SHORT).show();
                Log.d(ERROR_LOG_TAG, "Error occurred during authentication: " + error.toString
                        ().toLowerCase());
            } else if (data.getSerializableExtra(RideRequestActivity.RIDE_REQUEST_ERROR) != null) {
                RideRequestViewError error = (RideRequestViewError) data.getSerializableExtra(RideRequestActivity
                        .RIDE_REQUEST_ERROR);
                Toast.makeText(SampleActivity.this, "RideRequest error " + error.name(), Toast.LENGTH_SHORT).show();
                Log.d(ERROR_LOG_TAG, "Error occurred in the Ride Request Widget: " + error.toString().toLowerCase());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        AccessTokenManager accessTokenManager = new AccessTokenManager(this);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear) {
            accessTokenManager.removeAccessToken();
            Toast.makeText(this, "AccessToken cleared", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_copy) {
            AccessToken accessToken = accessTokenManager.getAccessToken();

            String message = accessToken == null ? "No AccessToken stored" : "AccessToken copied to clipboard";
            if (accessToken != null) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("UberSampleAccessToken", accessToken.getToken());
                clipboard.setPrimaryClip(clip);
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
