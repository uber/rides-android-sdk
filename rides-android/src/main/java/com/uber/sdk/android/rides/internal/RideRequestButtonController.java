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
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButtonCallback;
import com.uber.sdk.core.client.Session;
import com.uber.sdk.rides.client.UberRidesApi;
import com.uber.sdk.rides.client.error.ApiError;
import com.uber.sdk.rides.client.error.ClientError;
import com.uber.sdk.rides.client.error.ErrorParser;
import com.uber.sdk.rides.client.model.PriceEstimate;
import com.uber.sdk.rides.client.model.PriceEstimatesResponse;
import com.uber.sdk.rides.client.model.TimeEstimate;
import com.uber.sdk.rides.client.model.TimeEstimatesResponse;
import com.uber.sdk.rides.client.services.RidesService;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.uber.sdk.core.client.utils.Preconditions.checkNotNull;


public class RideRequestButtonController {

    @NonNull
    private final RidesService ridesService;

    @VisibleForTesting
    @NonNull
    TimeDelegate pendingDelegate;

    private Call<PriceEstimatesResponse> priceEstimateCall;
    private Call<TimeEstimatesResponse> timeEstimateCall;
    private RideRequestButtonView rideRequestButtonView;
    private RideRequestButtonCallback rideRequestButtonCallback;

    @VisibleForTesting
    RideRequestButtonController(
            @NonNull RideRequestButtonView rideRequestButtonView,
            @NonNull RidesService ridesService,
            @Nullable RideRequestButtonCallback callback) {
        this.rideRequestButtonView = rideRequestButtonView;
        this.rideRequestButtonCallback = callback;
        this.ridesService = ridesService;
        this.pendingDelegate = new TimeDelegate(rideRequestButtonView, callback);
    }

    public RideRequestButtonController(
            @NonNull RideRequestButtonView rideRequestButtonView,
            @NonNull Session session,
            @Nullable RideRequestButtonCallback callback) {
        this.rideRequestButtonView = rideRequestButtonView;
        this.rideRequestButtonCallback = callback;
        this.ridesService = UberRidesApi.with(session)
                .build()
                .createService();
        this.pendingDelegate = new TimeDelegate(rideRequestButtonView, callback);
    }

    public void loadRideInformation(@NonNull RideParameters rideParameters) {
        if (rideParameters.getPickupLatitude() != null) {
            checkNotNull(rideParameters.getPickupLongitude(), "Pickup point latitude is set in " +
                    "RideParameters but not the longitude.");
        }

        if (rideParameters.getPickupLongitude() != null) {
            checkNotNull(rideParameters.getPickupLatitude(), "Pickup point longitude is set in " +
                    "RideParameters but not the latitude.");
        }

        if (rideParameters.getDropoffLatitude() != null) {
            checkNotNull(rideParameters.getDropoffLongitude(), "Dropoff point latitude is set in " +
                    "RideParameters but not the longitude.");
        }

        if (rideParameters.getDropoffLongitude() != null) {
            checkNotNull(rideParameters.getDropoffLatitude(), "Dropoff point longitude is set in" +
                    " RideParameters but not the latitude.");
        }

        cancelAllPending();

        if (rideParameters.getPickupLatitude() != null) {
            if (rideParameters.getDropoffLatitude() != null) {
                TimePriceDelegate pendingDelegate = new TimePriceDelegate(rideRequestButtonView, rideRequestButtonCallback);

                loadPriceEstimate(
                        rideParameters.getPickupLatitude().floatValue(),
                        rideParameters.getPickupLongitude().floatValue(),
                        rideParameters.getDropoffLatitude().floatValue(),
                        rideParameters.getDropoffLongitude().floatValue(),
                        rideParameters.getProductId(),
                        pendingDelegate);

                this.pendingDelegate = pendingDelegate;
            } else {
                pendingDelegate = new TimeDelegate(rideRequestButtonView, rideRequestButtonCallback);
            }

            loadTimeEstimate(
                    pendingDelegate,
                    rideParameters.getPickupLatitude().floatValue(),
                    rideParameters.getPickupLongitude().floatValue(),
                    rideParameters.getProductId());
        } else {
            rideRequestButtonView.showDefaultView();
        }
    }

    private void loadTimeEstimate(
            @NonNull final TimeDelegate delegate,
            final float latitude,
            final float longitude,
            @Nullable final String productId) {

        timeEstimateCall = ridesService.getPickupTimeEstimate(latitude, longitude, productId);

        timeEstimateCall.enqueue(new Callback<TimeEstimatesResponse>() {
            @Override
            public void onResponse(Call<TimeEstimatesResponse> call, Response<TimeEstimatesResponse> response) {
                final ApiError apiError = ErrorParser.parseError(response);
                if (apiError != null) {
                    delegate.finishWithError(apiError);
                    return;
                }

                final List<TimeEstimate> estimates = response.body().getTimes();
                if (estimates == null || estimates.size() < 1) {
                    delegate.finishWithError(createProductNoFoundError());
                    return;
                }

                final TimeEstimate timeEstimate =
                        (productId == null) ? estimates.get(0) : findTimeEstimate(productId, estimates);

                if (timeEstimate == null) {
                    delegate.finishWithError(createProductNoFoundError());
                    return;
                }

                delegate.onTimeReceived(timeEstimate);
            }

            @Override
            public void onFailure(Call<TimeEstimatesResponse> call, Throwable throwable) {
                delegate.finishWithError(throwable);
            }
        });
    }

    private void loadPriceEstimate(
            final float startLatitude,
            final float startLongitude,
            final float endLatitude,
            final float endLongitude,
            final @Nullable String productId,
            final TimePriceDelegate delegate) {

        priceEstimateCall = ridesService.getPriceEstimates(startLatitude, startLongitude,
                endLatitude, endLongitude);

        priceEstimateCall.enqueue(new Callback<PriceEstimatesResponse>() {
            @Override
            public void onResponse(
                    Call<PriceEstimatesResponse> call,
                    Response<PriceEstimatesResponse> response) {

                ApiError apiError = ErrorParser.parseError(response);
                if (apiError != null) {
                    delegate.finishWithError(apiError);
                    return;
                }

                final List<PriceEstimate> estimates = response.body().getPrices();
                if (estimates == null || estimates.size() < 1) {
                    delegate.finishWithError(createProductNoFoundError());
                    return;
                }

                final PriceEstimate priceEstimate =
                        (productId == null) ? estimates.get(0) : findPriceEstimate(productId, estimates);

                if (priceEstimate == null) {
                    delegate.finishWithError(createProductNoFoundError());
                    return;
                }

                delegate.onPriceReceived(priceEstimate);
            }

            @Override
            public void onFailure(Call<PriceEstimatesResponse> call, Throwable throwable) {
                delegate.finishWithError(throwable);
            }
        });
    }

    /**
     * Mark this class as no longer required. Any in-flight operation will be cancelled.
     */
    public void destroy() {
        this.rideRequestButtonView = null;
        this.rideRequestButtonCallback = null;

        cancelAllPending();
    }

    private void cancelAllPending() {
        pendingDelegate.finish();
        if (timeEstimateCall != null) {
            timeEstimateCall.cancel();
        }

        if (priceEstimateCall != null) {
            priceEstimateCall.cancel();
        }
    }

    private static ApiError createProductNoFoundError() {
        return new ApiError(null, Arrays.asList(new ClientError(null, 404, "Product Id requested not found.")));
    }

    private static TimeEstimate findTimeEstimate(@NonNull String productId, @NonNull List<TimeEstimate> estimates) {
        for (TimeEstimate estimate : estimates) {
            if (productId.equals(estimate.getProductId())) {
                return estimate;
            }
        }
        return null;
    }

    private static PriceEstimate findPriceEstimate(@NonNull String productId, @NonNull List<PriceEstimate> estimates) {
        for (PriceEstimate estimate : estimates) {
            if (productId.equals(estimate.getProductId())) {
                return estimate;
            }
        }
        return null;
    }
}
