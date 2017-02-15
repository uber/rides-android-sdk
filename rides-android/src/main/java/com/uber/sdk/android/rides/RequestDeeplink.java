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
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.uber.sdk.android.core.Deeplink;
import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.android.core.install.SignupDeeplink;
import com.uber.sdk.android.core.utils.AppProtocol;
import com.uber.sdk.android.core.utils.PackageManagers;
import com.uber.sdk.rides.client.SessionConfiguration;

import static com.uber.sdk.android.core.utils.Preconditions.checkNotNull;


/**
 * A deeplink for requesting rides in the Uber application.
 *
 * @see <a href="https://developer.uber.com/v1/deep-linking/">Uber deeplink documentation</a>
 */
public class RequestDeeplink implements Deeplink {

    private static final String USER_AGENT_DEEPLINK = "rides-android-v0.6.0-deeplink";

    @NonNull
    private final Uri uri;

    @NonNull
    private final Context context;

    private RequestDeeplink(@NonNull Context context, @NonNull Uri uri) {
        this.uri = uri;
        this.context = context;
    }

    /**
     * Executes the deeplink to launch the Uber app.  If the app is not installed redirects to the play store.
     */
    public void execute() {
        if (isSupported()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        } else {
            Log.i(UberSdk.UBER_SDK_LOG_TAG, "Uber app not installed, redirecting to mobile sign up.");
            final String clientId = uri.getQueryParameter(Builder.CLIENT_ID);
            final String userAgent = uri.getQueryParameter(Builder.USER_AGENT);

            new SignupDeeplink(context, clientId, userAgent)
                    .execute();
        }
    }

    @Override
    public boolean isSupported() {
        for (String packageName : AppProtocol.UBER_PACKAGE_NAMES) {
            if(PackageManagers.isPackageAvailable(context, packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return The {@link Uri} for the deeplink.
     */
    @NonNull
    public Uri getUri() {
        return uri;
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
        public static final String USER_AGENT = "user-agent";

        private RideParameters rideParameters;
        private SessionConfiguration sessionConfiguration;
        private final Context context;

        /**
         * @param context to execute the deeplink.
         */
        public Builder(Context context) {
            this.context = context;
        }

        /**
         * Sets the {@link RideParameters} for the deeplink.
         *
         * @return this instance of {@link Builder}
         */
        public RequestDeeplink.Builder setRideParameters(@NonNull RideParameters rideParameters) {
            this.rideParameters = rideParameters;
            return this;
        }

        /**
         * Sets the client Id
         *
         * @return this instance of {@link Builder}
         */
        public RequestDeeplink.Builder setSessionConfiguration(@NonNull SessionConfiguration loginConfiguration) {
            sessionConfiguration = loginConfiguration;
            return this;
        }

        /**
         * Builds an {@link RequestDeeplink} object.
         *
         * @return {@link RequestDeeplink} generated from parameters
         */
        @NonNull
        public RequestDeeplink build() {
            checkNotNull(rideParameters, "Must supply ride parameters.");
            checkNotNull(sessionConfiguration, "Must supply a Login Configuration");
            checkNotNull(sessionConfiguration.getClientId(), "Must supply client Id on Login Configuration");


            Uri.Builder builder = new Uri.Builder();
            builder.scheme(AppProtocol.DEEPLINK_SCHEME);
            builder.appendQueryParameter(ACTION, SET_PICKUP);
            builder.appendQueryParameter(CLIENT_ID, sessionConfiguration.getClientId());
            if (rideParameters.getProductId() != null) {
                builder.appendQueryParameter(PRODUCT_ID, rideParameters.getProductId());
            }
            if (rideParameters.getPickupLatitude() != null && rideParameters.getPickupLongitude() != null) {
                addLocation(LocationType.PICKUP, Double.toString(rideParameters.getPickupLatitude()),
                        Double.toString(rideParameters.getPickupLongitude()), rideParameters.getPickupNickname(),
                        rideParameters.getPickupAddress(), builder);
            }
            if (rideParameters.isPickupMyLocation()) {
                builder.appendQueryParameter(LocationType.PICKUP.getUriQueryKey(), MY_LOCATION);
            }
            if (rideParameters.getDropoffLatitude() != null && rideParameters.getDropoffLongitude() != null) {
                addLocation(LocationType.DROPOFF, Double.toString(rideParameters.getDropoffLatitude()),
                        Double.toString(rideParameters.getDropoffLongitude()), rideParameters.getDropoffNickname(),
                        rideParameters.getDropoffAddress(), builder);
            }

            String userAgent = rideParameters.getUserAgent();
            if (userAgent == null) {
                userAgent = USER_AGENT_DEEPLINK;
            }
            builder.appendQueryParameter(USER_AGENT, userAgent);

            return new RequestDeeplink(context, builder.build());
        }

        private void addLocation(
                @NonNull LocationType locationType, @NonNull String latitude,
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
    }

    private enum LocationType {
        PICKUP,
        DROPOFF;

        private String getUriQueryKey() {
            return name().toLowerCase();
        }
    }
}
