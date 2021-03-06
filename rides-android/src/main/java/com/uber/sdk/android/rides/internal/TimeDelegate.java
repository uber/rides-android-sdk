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

package com.uber.sdk.android.rides.internal;

import androidx.annotation.NonNull;

import com.uber.sdk.android.rides.RideRequestButtonCallback;
import com.uber.sdk.rides.client.error.ApiError;
import com.uber.sdk.rides.client.model.TimeEstimate;

class TimeDelegate {

    RideRequestButtonView view;
    RideRequestButtonCallback callback;

    TimeDelegate(RideRequestButtonView view, RideRequestButtonCallback callback) {
        this.view = view;
        this.callback = callback;
    }

    void finish() {
        view = null;
        callback = null;
    }

    public void finishWithError(ApiError error) {
        if (callback != null) {
            callback.onError(error);
        }
        showDefaultView();
    }

    void finishWithError(Throwable throwable) {
        if (callback != null) {
            callback.onError(throwable);
        }
        showDefaultView();
    }

    void showDefaultView() {
        if (view != null) {
            view.showDefaultView();
        }
        finish();
    }

    void onTimeReceived(@NonNull TimeEstimate timeEstimate) {
        if (view != null) {
            view.showEstimate(timeEstimate);
        }

        if (callback != null) {
            callback.onRideInformationLoaded();
        }
        finish();
    }
}
