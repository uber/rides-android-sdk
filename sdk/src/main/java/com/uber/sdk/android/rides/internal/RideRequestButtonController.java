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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButtonCallback;
import com.uber.sdk.rides.client.Session;
import com.uber.sdk.rides.client.UberRidesApi;
import com.uber.sdk.rides.client.error.ApiError;
import com.uber.sdk.rides.client.error.ErrorParser;
import com.uber.sdk.rides.client.model.PriceEstimate;
import com.uber.sdk.rides.client.model.PriceEstimatesResponse;
import com.uber.sdk.rides.client.model.TimeEstimate;
import com.uber.sdk.rides.client.model.TimeEstimatesResponse;
import com.uber.sdk.rides.client.services.RidesService;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.uber.sdk.rides.client.utils.Preconditions.checkNotNull;


public class RideRequestButtonController {
    private final RidesService ridesService;

    @Nullable
    private RideRequestButtonView rideRequestButtonView;

    private RideRequestButtonCallback rideRequestButtonCallback;

    private Call<PriceEstimatesResponse> priceEstimateCall;

    private Call<TimeEstimatesResponse> timeEstimateCall;

    private AtomicBoolean timesEstimatePending = new AtomicBoolean(false);
    private AtomicBoolean pricesEstimatePending = new AtomicBoolean(false);

    public RideRequestButtonController(@NonNull RideRequestButtonView rideRequestButtonView,
                                       @NonNull Session session,
                                       @Nullable RideRequestButtonCallback callback) {
        this.rideRequestButtonView = rideRequestButtonView;
        this.rideRequestButtonCallback = callback;
        this.ridesService = UberRidesApi.with(session)
                .build()
                .createService();
    }

    public void loadRideInformation(@NonNull RideParameters rideParameters) {

        checkNotNull(rideParameters.getProductId(), "Must set product Id in RideParameters.");

        checkNotNull(rideParameters.getPickupLatitude(), "Must set pick up point latitude in " +
                "RideParameters.");

        checkNotNull(rideParameters.getPickupLongitude(), "Must set pick up point longitude in " +
                "RideParameters.");

        if (rideParameters.getDropoffLatitude() != null) {
            checkNotNull(rideParameters.getDropoffLongitude(), "Dropoff point latitude is set in " +
                    "RideParameters but not the longitude.");
        }

        if (rideParameters.getDropoffLongitude() != null) {
            checkNotNull(rideParameters.getDropoffLongitude(), "Dropoff point longitude is set in" +
                    " RideParameters but not the latitude.");
        }

        cancel(timeEstimateCall);
        cancel(priceEstimateCall);

        timesEstimatePending.set(true);
        pricesEstimatePending.set(true);

        loadPickupTimesEstimate(rideParameters.getProductId(),
                rideParameters.getPickupLatitude().floatValue(),
                rideParameters.getPickupLongitude().floatValue());

        if (rideParameters.getDropoffLatitude() != null) {
            loadPriceEstimate(rideParameters.getProductId(),
                    rideParameters.getPickupLatitude().floatValue(),
                    rideParameters.getPickupLongitude().floatValue(),
                    rideParameters.getDropoffLatitude().floatValue(),
                    rideParameters.getDropoffLongitude().floatValue());
        }
    }

    private void loadPickupTimesEstimate(final @NonNull String productId, final float latitude,
                                         final float longitude) {

        timeEstimateCall = ridesService.getPickupTimeEstimate(latitude, longitude, productId);

        timeEstimateCall.enqueue(new Callback<TimeEstimatesResponse>() {
            @Override
            public void onResponse(Call<TimeEstimatesResponse> call,
                                   Response<TimeEstimatesResponse> response) {

                onTimeResponse(response, productId);
            }

            @Override
            public void onFailure(Call<TimeEstimatesResponse> call, Throwable throwable) {
                clearTimeEstimate();
                onApiFailure(throwable);
            }
        });
    }

    private void loadPriceEstimate(final @NonNull String productId, final float startLatitude,
                                   final float startLongitude, final float endLatitude,
                                   final float endLongitude) {
        priceEstimateCall = ridesService.getPriceEstimates(startLatitude, startLongitude,
                endLatitude, endLongitude);

        priceEstimateCall.enqueue(new Callback<PriceEstimatesResponse>() {
            @Override
            public void onResponse(Call<PriceEstimatesResponse> call,
                                   Response<PriceEstimatesResponse> response) {

                onPriceResponse(response, productId);
            }

            @Override
            public void onFailure(Call<PriceEstimatesResponse> call, Throwable throwable) {
                clearPriceEstimate();
                onApiFailure(throwable);
            }
        });
    }

    private void onApiError(ApiError error) {
        if (rideRequestButtonCallback != null) {
            rideRequestButtonCallback.onError(error);
        }
    }

    private void onApiFailure(Throwable throwable) {
        if (rideRequestButtonCallback != null) {
            rideRequestButtonCallback.onError(throwable);
        }
    }

    private void onContentRefreshed() {
        if (rideRequestButtonCallback != null) {
            rideRequestButtonCallback.onRideInformationLoaded();
        }
    }

    private void clearPriceEstimate() {
        if (rideRequestButtonView != null) {
            rideRequestButtonView.clearPriceEstimate();
        }
    }

    private void clearTimeEstimate() {
        if (rideRequestButtonView != null) {
            rideRequestButtonView.clearTimeEstimate();
        }
    }

    private void onTimeResponse(Response<TimeEstimatesResponse> response, String productId) {
        ApiError apiError = ErrorParser.parseError(response);
        if (apiError != null) {
            clearTimeEstimate();
            onApiError(apiError);
            return;
        }

        showTimeEstimate(response.body().getTimes(), productId);
        timesEstimatePending.set(false);

        if (!pricesEstimatePending.get()) {
            onContentRefreshed();
        }
    }

    private void onPriceResponse(Response<PriceEstimatesResponse> response, String productId) {
        ApiError apiError =  ErrorParser.parseError(response);
        if (apiError != null) {
            clearPriceEstimate();
            onApiError(apiError);
            return;
        }

        showPriceEstimate(response.body().getPrices(), productId);
        pricesEstimatePending.set(false);

        if (!timesEstimatePending.get()) {
            onContentRefreshed();
        }
    }

    private void showTimeEstimate(@NonNull List<TimeEstimate> timeEstimates,
                                  @NonNull String productId) {
        if (rideRequestButtonView != null) {
            if (timeEstimates.size() < 1) {
                return;
            }

            for (int i = 0; i < timeEstimates.size(); i++) {
                TimeEstimate timeEstimate = timeEstimates.get(i);
                if (timeEstimate.getProductId().equals(productId)) {
                    rideRequestButtonView.showTimeEstimate(timeEstimate);
                }
            }
        }
    }

    private void showPriceEstimate(@NonNull List<PriceEstimate> priceEstimates,
                                   @NonNull String productId) {
        if (rideRequestButtonView != null) {
            if (priceEstimates.size() < 1) {
                return;
            }

            for (int i = 0; i < priceEstimates.size(); i++) {
                PriceEstimate priceEstimate = priceEstimates.get(i);
                if (priceEstimate.getProductId().equals(productId)) {
                    rideRequestButtonView.showPriceEstimate(priceEstimate);
                }
            }
        }
    }

    /**
     * Mark this class as no longer required. Any in-flight operation will be cancelled.
     */
    public void destroy() {
        this.rideRequestButtonView = null;
        this.rideRequestButtonCallback = null;

        cancel(timeEstimateCall);
        cancel(priceEstimateCall);
    }

    private static void cancel(@Nullable Call call) {
        if (call != null) {
            call.cancel();
        }
    }
}
