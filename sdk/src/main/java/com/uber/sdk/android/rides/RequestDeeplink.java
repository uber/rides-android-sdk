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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.base.Preconditions;


/**
 * A deeplink for requesting rides in the Uber application.
 *
 * @see <a href="https://developer.uber.com/v1/deep-linking/">Uber deeplink documentation</a>
 */
public class RequestDeeplink {

    private static final String UBER_PACKAGE_NAME = "com.ubercab";
    private static final String UBER_SDK_LOG_TAG = "UberSDK";
    private static final String USER_AGENT_DEEPLINK = "rides-deeplink-v0.1.0";

    @NonNull private final Uri mUri;

    private RequestDeeplink(@NonNull Uri uri) {
        mUri = uri;
    }

    /**
     * Executes the deeplink to launch the Uber app.  If the app is not installed redirects to the play store.
     *
     * @param context The {@link Context} the deeplink will be executed from, used to start a new {@link Activity}.
     * If not a windowed context will error.
     */
    public void execute(@NonNull Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(UBER_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            Intent intent = new Intent(Intent.ACTION_VIEW, mUri);
            context.startActivity(intent);
        } catch (PackageManager.NameNotFoundException e) {
            Log.i(UBER_SDK_LOG_TAG, "Uber app not installed, redirecting to mobile sign up.");
            String redirect = context.getResources().getString(R.string.mobile_redirect);
            String url = String.format(redirect,
                    mUri.getQueryParameter(Builder.CLIENT_ID), mUri.getQueryParameter(Builder.USER_AGENT));
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        }
    }

    /**
     * The {@link Uri} for the deeplink.
     */
    @NonNull
    public Uri getUri() {
        return mUri;
    }

    /**
     * Builder for {@link RequestDeeplink} objects.
     */
    public static class Builder {

        public static final String ACTION = "action";
        public static final String SET_PICKUP = "setPickup";
        public static final String CLIENT_ID = "client_id";
        public static final String PRODUCT_ID = "product_id";
        public static final String MY_LOCATION = "my_location";
        public static final String LATITUDE = "[latitude]";
        public static final String LONGITUDE = "[longitude]";
        public static final String NICKNAME = "[nickname]";
        public static final String FORMATTED_ADDRESS = "[formatted_address]";
        public static final String SCHEME = "uber";
        public static final String USER_AGENT = "user-agent";

        private String mClientId;
        private String mUserAgent = USER_AGENT_DEEPLINK;
        private RideParameters mRideParameters;

        /**
         * Sets the client ID for the app the deeplink is being started from.
         */
        public RequestDeeplink.Builder setClientId(@NonNull String cliendId) {
            mClientId = cliendId;
            return this;
        }

        /**
         * Sets the {@link RideParameters} for the deeplink.
         */
        public RequestDeeplink.Builder setRideParameters(@NonNull RideParameters rideParameters) {
            mRideParameters = rideParameters;
            return this;
        }

        /**
         * Builds an {@link RequestDeeplink} object.
         */
        @NonNull
        public RequestDeeplink build() {
            validate();

            Uri.Builder builder = new Uri.Builder();
            builder.scheme(SCHEME);
            builder.appendQueryParameter(ACTION, SET_PICKUP);
            builder.appendQueryParameter(CLIENT_ID, mClientId);
            if (mRideParameters.getProductId() != null) {
                builder.appendQueryParameter(PRODUCT_ID, mRideParameters.getProductId());
            }
            if (mRideParameters.getPickupLatitude() != null && mRideParameters.getPickupLongitude() != null) {
                addLocation(LocationType.PICKUP, Float.toString(mRideParameters.getPickupLatitude()),
                        Float.toString(mRideParameters.getPickupLongitude()), mRideParameters.getPickupNickname(),
                        mRideParameters.getPickupAddress(), builder);
            }
            if (mRideParameters.isPickupMyLocation()) {
                builder.appendQueryParameter(LocationType.PICKUP.getUriQueryKey(), MY_LOCATION);
            }
            if (mRideParameters.getDropoffLatitude() != null && mRideParameters.getDropoffLongitude() != null) {
                addLocation(LocationType.DROPOFF, Float.toString(mRideParameters.getDropoffLatitude()),
                        Float.toString(mRideParameters.getDropoffLongitude()), mRideParameters.getDropoffNickname(),
                        mRideParameters.getDropoffAddress(), builder);
            }
            if (mUserAgent == null) {
                mUserAgent = USER_AGENT_DEEPLINK;
            }
            builder.appendQueryParameter(USER_AGENT, mUserAgent);
            return new RequestDeeplink(builder.build());
        }

        /**
         * Sets the user agent, describing where this {@link RequestDeeplink} came from for analytics.
         */
        RequestDeeplink.Builder setUserAgent(@NonNull String userAgent) {
            mUserAgent = userAgent;
            return this;
        }

        private void addLocation(@NonNull LocationType locationType, @NonNull String latitude,
                @NonNull String longitude, @Nullable String nickname, @Nullable String address, Uri.Builder builder) {
            String typeQueryKey = locationType.getUriQueryKey();
            builder.appendQueryParameter(typeQueryKey + LATITUDE, latitude);
            builder.appendQueryParameter(typeQueryKey + LONGITUDE, longitude);
            if (nickname != null) {
                builder.appendQueryParameter(typeQueryKey + NICKNAME, nickname);
            }
            if (address != null) {
                builder.appendQueryParameter(typeQueryKey + FORMATTED_ADDRESS, address);
            }
        }

        private void validate() {
            Preconditions.checkState(mClientId != null, "Must supply a client ID.");
            Preconditions.checkState(mRideParameters != null, "Must supply ride parameters.");
        }

        private enum LocationType {
            PICKUP,
            DROPOFF;

            private String getUriQueryKey() {
                return name().toLowerCase();
            }
        }
    }
}
