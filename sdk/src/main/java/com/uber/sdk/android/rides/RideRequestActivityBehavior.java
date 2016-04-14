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
import android.support.annotation.NonNull;

/**
 * The {@link RideRequestBehavior} to pass to the {@link RideRequestButton} to have it launch a {@link RideRequestActivity}.
 */
public class RideRequestActivityBehavior implements RideRequestBehavior {

    @NonNull private Activity mActivity; 
    private int mRequestCode;

    /**
     * Construct a new {@link RideRequestActivityBehavior}.
     *
     * @param activity the {@link Activity} to launch the {@link RideRequestActivity} from
     * @param requestCode the request code to use for the {@link Activity} result
     */
    public RideRequestActivityBehavior(@NonNull Activity activity, int requestCode) {
        mActivity = activity;
        mRequestCode = requestCode;
    }

    @Override
    public void requestRide(Context context, RideParameters params) {
        Intent data = RideRequestActivity.newIntent(context, params);
        mActivity.startActivityForResult(data, mRequestCode);
    }
}
