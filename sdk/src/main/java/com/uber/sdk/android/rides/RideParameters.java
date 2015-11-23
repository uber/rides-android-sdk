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

import android.support.annotation.Nullable;

/**
 * Represents the parameters for an Uber ride.
 */
public class RideParameters {

    private final boolean mIsPickupMyLocation;
    private final String mProductId;
    private final Float mPickupLatitude;
    private final Float mPickupLongitude;
    private final String mPickupNickname;
    private final String mPickupAddress;
    private final Float mDropoffLatitude;
    private final Float mDropoffLongitude;
    private final String mDropoffNickname;
    private final String mDropoffAddress;

    private RideParameters(boolean isPickupMyLocation,
            @Nullable String productId,
            @Nullable Float pickupLatitude,
            @Nullable Float pickupLongitude,
            @Nullable String pickupNickname,
            @Nullable String pickupAddress,
            @Nullable Float dropoffLatitude,
            @Nullable Float dropoffLongitude,
            @Nullable String dropoffNickname,
            @Nullable String dropoffAddress) {
        mIsPickupMyLocation = isPickupMyLocation;
        mProductId = productId;
        mPickupLatitude = pickupLatitude;
        mPickupLongitude = pickupLongitude;
        mPickupNickname = pickupNickname;
        mPickupAddress = pickupAddress;
        mDropoffLatitude = dropoffLatitude;
        mDropoffLongitude = dropoffLongitude;
        mDropoffNickname = dropoffNickname;
        mDropoffAddress = dropoffAddress;
    }

    /**
     * @return True if the pickup location of the ride is set to be the device's location, false if a
     * specific pickup location has been set.
     */
    public boolean isPickupMyLocation() {
        return mIsPickupMyLocation;
    }

    /**
     * Gets the product ID for the ride.
     */
    @Nullable
    public String getProductId() {
        return mProductId;
    }

    /**
     * Gets the latitude of the pickup location of the ride. Null if no pickup location specified.
     */
    @Nullable
    public Float getPickupLatitude() {
        return mPickupLatitude;
    }

    /**
     * Gets the longitude of the pickup location of the ride. Null if no pickup location specified.
     */
    @Nullable
    public Float getPickupLongitude() {
        return mPickupLongitude;
    }

    /**
     * Gets the nickname of the pickup location of the ride. Null if no pickup location specified.
     */
    @Nullable
    public String getPickupNickname() {
        return mPickupNickname;
    }

    /**
     * Gets the address of the pickup location of the ride. Null if no pickup location specified.
     */
    @Nullable
    public String getPickupAddress() {
        return mPickupAddress;
    }

    /**
     * Gets the latitude of the dropoff location of the ride. Null if no dropoff location specified.
     */
    @Nullable
    public Float getDropoffLatitude() {
        return mDropoffLatitude;
    }

    /**
     * Gets the longitude of the dropoff location of the ride. Null if no dropoff location specified.
     */
    @Nullable
    public Float getDropoffLongitude() {
        return mDropoffLongitude;
    }

    /**
     * Gets the nickname of the dropoff location of the ride. Null if no dropoff location specified.
     */
    @Nullable
    public String getDropoffNickname() {
        return mDropoffNickname;
    }

    /**
     * Gets the address of the dropoff location of the ride. Null if no dropoff location specified.
     */
    @Nullable
    public String getDropoffAddress() {
        return mDropoffAddress;
    }

    /**
     * Builder for {@link RideParameters} objects.
     */
    public static class Builder {

        private boolean mIsPickupMyLocation = true;
        private String mProductId;
        private Float mPickupLatitude;
        private Float mPickupLongitude;
        private String mPickupNickname;
        private String mPickupAddress;
        private Float mDropoffLatitude;
        private Float mDropoffLongitude;
        private String mDropoffNickname;
        private String mDropoffAddress;

        /**
         * Sets the product ID for the ride.
         */
        public RideParameters.Builder setProductId(String productId) {
            mProductId = productId;
            return this;
        }

        /**
         * Sets the pickup location for the ride.  If no pickup is supplied then it defaults to the device's location.
         *
         * @param latitude The latitude of the pickup.
         * @param longitude The longitude of the pickup.
         * @param nickname This will show up as the text name at the request a ride screen in the Uber app. If not
         * supplied will just show address.
         * @param address The address of the pickup location.  If not supplied the bar will read 'Go to pin'.
         */
        public RideParameters.Builder setPickupLocation(float latitude, float longitude, @Nullable String nickname,
                @Nullable String address) {
            mPickupLatitude = latitude;
            mPickupLongitude = longitude;
            mPickupNickname = nickname;
            mPickupAddress = address;
            mIsPickupMyLocation = false;
            return this;
        }

        /**
         * Sets the dropoff location for the ride.
         *
         * @param latitude The latitude of the dropoff.
         * @param longitude The longitude of the dropoff.
         * @param nickname This will show up as the text name at the request a ride screen in the Uber app. If not
         * supplied will just show address.
         * @param address The address of the dropoff location.  If not supplied will read 'Destination'.
         */
        public RideParameters.Builder setDropoffLocation(float latitude, float longitude, @Nullable String nickname,
                @Nullable String address) {
            mDropoffLatitude = latitude;
            mDropoffLongitude = longitude;
            mDropoffNickname = nickname;
            mDropoffAddress = address;
            return this;
        }

        /**
         * Sets the pickup location for the ride to be the device's current location.
         */
        public RideParameters.Builder setPickupToMyLocation() {
            mIsPickupMyLocation = true;
            mPickupLatitude = null;
            mPickupLongitude = null;
            mPickupNickname = null;
            mPickupAddress = null;
            return this;
        }

        /**
         * Builds an {@link RideParameters} object.
         */
        public RideParameters build() {
            return new RideParameters(mIsPickupMyLocation, mProductId, mPickupLatitude, mPickupLongitude,
                    mPickupNickname, mPickupAddress, mDropoffLatitude, mDropoffLongitude, mDropoffNickname,
                    mDropoffAddress);
        }
    }
}
