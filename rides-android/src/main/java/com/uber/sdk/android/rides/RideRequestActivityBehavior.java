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
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;

import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.android.core.auth.AccessTokenManager;
import com.uber.sdk.core.client.SessionConfiguration;

/**
 * The {@link RideRequestBehavior} to pass to the {@link RideRequestButton} to have it launch a {@link RideRequestActivity}.

 * @deprecated in favor of directly using mobile web directly.
 * See https://developer.uber.com/docs/riders/ride-requests/tutorials/widget/migration-to-muber
 */
@Deprecated
public class RideRequestActivityBehavior implements RideRequestBehavior {

    @NonNull private final Activity activity;
    private final int requestCode;
    private final SessionConfiguration sessionConfiguration;
    private final String accessTokenStorageKey;

    /**
     * Construct a new {@link RideRequestActivityBehavior}.
     *
     * @param activity the {@link Activity} to launch the {@link RideRequestActivity} from
     * @param requestCode the request code to use for the {@link Activity} result
     */
    public RideRequestActivityBehavior(@NonNull Activity activity, int requestCode) {
        this(activity, requestCode, UberSdk.getDefaultSessionConfiguration());
    }

    /**
     * Construct a new {@link RideRequestActivityBehavior}.
     *
     * @param activity the {@link Activity} to launch the {@link RideRequestActivity} from
     * @param requestCode the request code to use for the {@link Activity} result
     * @param loginConfiguration used for login scenarios from ride request screen
     */
    public RideRequestActivityBehavior(@NonNull Activity activity,
                                       int requestCode,
                                       @NonNull SessionConfiguration loginConfiguration) {
        this(activity, requestCode, loginConfiguration, AccessTokenManager.ACCESS_TOKEN_DEFAULT_KEY);
    }

    /**
     * Construct a new {@link RideRequestActivityBehavior}.
     *
     * @param activity the {@link Activity} to launch the {@link RideRequestActivity} from
     * @param requestCode the request code to use for the {@link Activity} result
     * @param loginConfiguration used for login scenarios from ride request screen
     * @param accessTokenStorageKey key to use for looking in {@link com.uber.sdk.core.auth.AccessTokenStorage}
     */
    public RideRequestActivityBehavior(@NonNull Activity activity,
                                       int requestCode,
                                       @NonNull SessionConfiguration loginConfiguration,
                                       @NonNull String accessTokenStorageKey) {
        this.activity = activity;
        this.requestCode = requestCode;
        this.sessionConfiguration = loginConfiguration;
        this.accessTokenStorageKey = accessTokenStorageKey;
    }

    @Override
    public void requestRide(Context context, RideParameters params) {
        Intent data = RideRequestActivity.newIntent(context, params, sessionConfiguration, accessTokenStorageKey);
        activity.startActivityForResult(data, requestCode);
    }
}
