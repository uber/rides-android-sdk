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
import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.robolectric.Robolectric;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests {@link RideRequestButton}
 */
public class RequestButtonTest extends RobolectricTestBase {

    private Activity mActivity;
    private RideRequestButton mRequestButton;

    @Captor
    private ArgumentCaptor<Context> mContextArguementCaptor;
    @Captor
    private ArgumentCaptor<RideParameters> mRideParamsArguementCaptor;

    @Before
    public void setup() {
        mActivity = Robolectric.setupActivity(Activity.class);
        mRequestButton = new RideRequestButton(mActivity);
    }

    @Test
    public void onClick_whenHasSetRequestBehaviorAndRideParams_shouldCallRequestRideAndAddUserAgent() {
        RideParameters rideParameters = new RideParameters.Builder().build();
        mRequestButton.setRideParameters(rideParameters);
        RideRequestBehavior rideRequestBehavior = mock(RideRequestBehavior.class);
        mRequestButton.setRequestBehavior(rideRequestBehavior);
        mRequestButton.performClick();

        verify(rideRequestBehavior, times(1)).requestRide(mActivity, rideParameters);
        assertEquals(rideParameters.getUserAgent(), "rides-android-v0.3.1-button");
    }
}
