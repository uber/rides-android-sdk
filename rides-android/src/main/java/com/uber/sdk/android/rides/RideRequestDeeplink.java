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
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.browser.customtabs.CustomTabsIntent;

import com.uber.sdk.android.core.Deeplink;
import com.uber.sdk.android.core.utils.AppProtocol;
import com.uber.sdk.android.core.utils.CustomTabsHelper;
import com.uber.sdk.core.client.SessionConfiguration;

import static com.uber.sdk.android.core.SupportedAppType.UBER;
import static com.uber.sdk.android.core.utils.Preconditions.checkNotNull;


/**
 * A deeplink for requesting rides in the Uber application.
 *
 * @see <a href="https://developer.uber.com/v1/deep-linking/">Uber deeplink documentation</a>
 */
public class RideRequestDeeplink implements Deeplink {

    private static final String USER_AGENT_DEEPLINK = String.format("rides-android-v%s-deeplink",
            BuildConfig.VERSION_NAME);

    @NonNull
    private final Uri uri;
    @NonNull
    private final Context context;
    @NonNull
    private final AppProtocol appProtocol;
    @NonNull
    private final CustomTabsHelper customTabsHelper;

    RideRequestDeeplink(@NonNull Context context,
            @NonNull Uri uri,
            @NonNull AppProtocol appProtocol,
            @NonNull CustomTabsHelper customTabsHelper) {
        this.uri = uri;
        this.context = context;
        this.appProtocol = appProtocol;
        this.customTabsHelper = customTabsHelper;
    }

    /**
     * Executes the deeplink to launch the Uber app.  If the app is not installed redirects to the play store.
     */
    public void execute() {
        final CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
        customTabsHelper.openCustomTab(context, intent, uri, new CustomTabsHelper.BrowserFallback());
    }

    @Override
    public boolean isSupported() {
        return appProtocol.isInstalled(context, UBER);
    }

    /**
     * @return The {@link Uri} for the deeplink.
     */
    @NonNull
    public Uri getUri() {
        return uri;
    }

    /**
     * Builder for {@link RideRequestDeeplink} objects.
     */
    public static class Builder {

        public static final String AUTHORITY = "riderequest";
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
        private Fallback fallback = Fallback.APP_INSTALL;
        private final Context context;
        private AppProtocol appProtocol;
        private CustomTabsHelper customTabsHelper;

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
        public RideRequestDeeplink.Builder setRideParameters(@NonNull RideParameters rideParameters) {
            this.rideParameters = rideParameters;
            return this;
        }

        /**
         * Sets the client Id
         *
         * @return this instance of {@link Builder}
         */
        public RideRequestDeeplink.Builder setSessionConfiguration(@NonNull SessionConfiguration
                sessionConfiguration) {
            this.sessionConfiguration = sessionConfiguration;
            return this;
        }

        /**
         * Sets the fallback to use when the Uber app isn't installed.
         *
         * @return this instance of {@link Builder}
         */
        public RideRequestDeeplink.Builder setFallback(@NonNull  Fallback fallback) {
            this.fallback = fallback;
            return this;
        }

        @VisibleForTesting
        RideRequestDeeplink.Builder setCustomTabsHelper(@NonNull CustomTabsHelper customTabsHelper) {
            this.customTabsHelper = customTabsHelper;
            return this;
        }

        @VisibleForTesting
        RideRequestDeeplink.Builder setAppProtocol(@NonNull AppProtocol appProtocol){
            this.appProtocol = appProtocol;
            return this;
        }

        /**
         * Builds an {@link RideRequestDeeplink} object.
         *
         * @return {@link RideRequestDeeplink} generated from parameters
         */
        @NonNull
        public RideRequestDeeplink build() {
            checkNotNull(rideParameters, "Must supply ride parameters.");
            checkNotNull(sessionConfiguration, "Must supply a Session Configuration");
            checkNotNull(sessionConfiguration.getClientId(), "Must supply client Id on Login Configuration");

            if (appProtocol == null) {
                appProtocol = new AppProtocol();
            }
            if (customTabsHelper == null) {
                customTabsHelper = new CustomTabsHelper();
            }

            final Uri.Builder builder = getUriBuilder(context, fallback);

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

            return new RideRequestDeeplink(context, builder.build(), appProtocol, customTabsHelper);
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

        Uri.Builder getUriBuilder(@NonNull Context context, @NonNull Deeplink.Fallback
                fallback) {
            final Uri.Builder builder;
            if (appProtocol.isInstalled(context, UBER)) {
                if (appProtocol.isAppLinkSupported()) {
                    builder = Uri.parse(Deeplink.APP_LINK_URI).buildUpon();
                } else {
                    builder = new Uri.Builder()
                            .scheme(Deeplink.DEEPLINK_SCHEME);
                }
            } else {
                if (fallback == Deeplink.Fallback.MOBILE_WEB) {
                    builder = Uri.parse(Deeplink.MOBILE_WEB_URI).buildUpon();
                } else {
                    builder = Uri.parse(Deeplink.APP_LINK_URI).buildUpon();
                }
            }
            return builder;
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
