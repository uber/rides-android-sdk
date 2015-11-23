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

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RequestButton;

/**
 * Activity that demonstrates how to use a {@link RequestButton}.
 */
public class SampleActivity extends AppCompatActivity {

    private static final String DROPOFF_ADDR = "One Embarcadero Center, San Francisco";
    private static final float DROPOFF_LAT = 37.795079f;
    private static final float DROPOFF_LONG = -122.397805f;
    private static final String DROPOFF_NICK = "Embarcadero";
    private static final String PICKUP_ADDR = "1455 Market Street, San Francisco";
    private static final float PICKUP_LAT = 37.775304f;
    private static final float PICKUP_LONG = -122.417522f;
    private static final String PICKUP_NICK = "Uber HQ";
    private static final String UBERX_PRODUCT_ID = "a1111c8c-c720-46c3-8534-2fcdd730040d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        String clientId = getString(R.string.client_id);
        if (clientId.equals("insert_your_client_id_here")) {
            throw new IllegalArgumentException("Please enter your client ID in client_id in res/values/strings.xml");
        }

        RequestButton uberButtonBlack = (RequestButton) findViewById(R.id.uber_button_black);
        RequestButton uberButtonWhite = (RequestButton) findViewById(R.id.uber_button_white);

        RideParameters rideParameters = new RideParameters.Builder()
                .setProductId(UBERX_PRODUCT_ID)
                .setPickupLocation(PICKUP_LAT, PICKUP_LONG, PICKUP_NICK, PICKUP_ADDR)
                .setDropoffLocation(DROPOFF_LAT, DROPOFF_LONG, DROPOFF_NICK, DROPOFF_ADDR)
                .build();

        uberButtonBlack.setRideParameters(rideParameters);
        uberButtonWhite.setRideParameters(rideParameters);
    }
}
