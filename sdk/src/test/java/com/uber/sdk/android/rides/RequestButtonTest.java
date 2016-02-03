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

package com.uber.sdk.android.rides;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.res.builder.RobolectricPackageManager;
import org.robolectric.shadows.ShadowActivity;

import java.io.IOException;

import static com.uber.sdk.android.rides.TestUtils.readUriResourceWithUserAgentParam;
import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

/**
 * Tests {@link RequestButton}
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RequestButtonTest {

    private static final String CLIENT_ID = "clientId";
    private static final float PICKUP_LAT = 32.1234f;
    private static final float PICKUP_LONG = -122.3456f;
    private static final String PICKUP_NICK = "pickupNick";
    private static final String PICKUP_ADDR = "Pickup Address";
    private static final String UBER_PACKAGE_NAME = "com.ubercab";
    private static final String USER_AGENT_BUTTON = "rides-button-v0.2.0";

    @Rule public ExpectedException exception = ExpectedException.none();

    private Activity mActivity;
    private RequestButton mRequestButton;

    @Before
    public void setup() {
        mActivity = Robolectric.setupActivity(Activity.class);
        mRequestButton = new RequestButton(mActivity);
    }

    @Test
    public void onClick_whenNullClientId_shouldThrowException() {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Client ID required to use RequestButton.");

        mRequestButton.performClick();
    }

    @Test
    public void onClick_whenClientIdProvidedAndNoUberApp_shouldStartMobileSite() throws IOException {
        String expectedUri = readUriResourceWithUserAgentParam("src/test/resources/deeplinkuris/no_app_installed",
                USER_AGENT_BUTTON);

        ShadowActivity shadowActivity = setupShadowActivityWithUber(false);

        mRequestButton.setClientId(CLIENT_ID);
        mRequestButton.performClick();

        Intent shadowedIntent = shadowActivity.getNextStartedActivity();
        assertEquals(expectedUri, shadowedIntent.getData().toString());
    }

    @Test
    public void onClick_whenClientIdProvidedAndUberAppInstalled_shouldStartUberApp() throws IOException {
        String expectedUri = readUriResourceWithUserAgentParam("src/test/resources/deeplinkuris/just_client_provided",
                USER_AGENT_BUTTON);

        ShadowActivity shadowActivity = setupShadowActivityWithUber(true);

        mRequestButton.setClientId(CLIENT_ID);
        mRequestButton.performClick();

        Intent shadowedIntent = shadowActivity.getNextStartedActivity();
        assertEquals(expectedUri, shadowedIntent.getDataString());
    }

    @Test
    public void onClick_whenClientIdAndPickupProvidedAndUberAppInstalled_shouldStartUberAppWithParams()
            throws IOException {
        String path = "src/test/resources/deeplinkuris/pickup_and_client_provided";
        String expectedUri = readUriResourceWithUserAgentParam(path, USER_AGENT_BUTTON);

        ShadowActivity shadowActivity = setupShadowActivityWithUber(true);

        RideParameters rideParameters = new RideParameters.Builder()
                .setPickupLocation(PICKUP_LAT, PICKUP_LONG, PICKUP_NICK, PICKUP_ADDR)
                .build();
        mRequestButton.setClientId(CLIENT_ID);
        mRequestButton.setRideParameters(rideParameters);
        mRequestButton.performClick();

        Intent shadowedIntent = shadowActivity.getNextStartedActivity();
        assertEquals(expectedUri, shadowedIntent.getData().toString());
    }

    @NonNull
    private ShadowActivity setupShadowActivityWithUber(boolean isUberInstalled) {
        ShadowActivity shadowActivity = shadowOf(mActivity);
        if (isUberInstalled) {
            RobolectricPackageManager packageManager = (RobolectricPackageManager) shadowActivity.getPackageManager();

            PackageInfo uberPackage = new PackageInfo();
            uberPackage.packageName = UBER_PACKAGE_NAME;
            packageManager.addPackage(uberPackage);
        }
        return shadowActivity;
    }
}
