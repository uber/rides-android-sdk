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

import android.content.Context;
import androidx.annotation.NonNull;

import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.core.client.SessionConfiguration;

/**
 * The {@link RideRequestBehavior} to pass to the {@link RideRequestButton} to have it execute a {@link RideRequestDeeplink}.
 */
@Deprecated
public class RequestDeeplinkBehavior implements RideRequestBehavior {

    final SessionConfiguration sessionConfiguration;

    /**
     * Create a {@link RequestDeeplinkBehavior} using the default {@link SessionConfiguration} from {@link UberSdk}
     */
    public RequestDeeplinkBehavior() {
        this.sessionConfiguration = UberSdk.getDefaultSessionConfiguration();
    }

    /**
     * Create a {@link RequestDeeplinkBehavior} with required {@link SessionConfiguration}
     * @param configuration to use for signing with Client Id
     */
    public RequestDeeplinkBehavior(@NonNull SessionConfiguration configuration) {
        this.sessionConfiguration = configuration;
    }

    /**
     * Requests a ride using a {@link RideRequestDeeplink} that is constructed using the provided {@link RideParameters}.
     *
     * @param context {@link Context} to pass to launch the {@link RideRequestDeeplink} from
     * @param params the {@link RideParameters} to use for building and executing the {@link RideRequestDeeplink}
     */
    @Override
    public void requestRide(@NonNull Context context, @NonNull RideParameters params) {
        RideRequestDeeplink deeplink = new RideRequestDeeplink.Builder(context)
                .setSessionConfiguration(sessionConfiguration)
                .setRideParameters(params).build();
        deeplink.execute();
    }
}
