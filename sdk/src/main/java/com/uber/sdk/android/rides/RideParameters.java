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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Represents the parameters for an Uber ride.
 */
public class RideParameters implements Parcelable {

    private final boolean mIsPickupMyLocation;
    @Nullable private final String mProductId;
    @Nullable private final Double mPickupLatitude;
    @Nullable private final Double mPickupLongitude;
    @Nullable private final String mPickupNickname;
    @Nullable private final String mPickupAddress;
    @Nullable private final Double mDropoffLatitude;
    @Nullable private final Double mDropoffLongitude;
    @Nullable private final String mDropoffNickname;
    @Nullable private final String mDropoffAddress;
    @Nullable private String mUserAgent;

    public static final Creator<RideParameters> CREATOR = new Creator<RideParameters>() {
        @Override
        public RideParameters createFromParcel(Parcel in) {
            return new RideParameters(in);
        }

        @Override
        public RideParameters[] newArray(int size) {
            return new RideParameters[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (mIsPickupMyLocation ? 1 : 0));
        dest.writeString(mProductId);
        dest.writeSerializable(mPickupLatitude);
        dest.writeSerializable(mPickupLongitude);
        dest.writeString(mPickupNickname);
        dest.writeString(mPickupAddress);
        dest.writeSerializable(mDropoffLatitude);
        dest.writeSerializable(mDropoffLongitude);
        dest.writeString(mDropoffNickname);
        dest.writeString(mDropoffAddress);
        dest.writeString(mUserAgent);
    }

    protected RideParameters(Parcel in) {
        mIsPickupMyLocation = in.readByte() != 0;
        mProductId = in.readString();
        mPickupLatitude = (Double) in.readSerializable();
        mPickupLongitude = (Double) in.readSerializable();
        mPickupNickname = in.readString();
        mPickupAddress = in.readString();
        mDropoffLatitude = (Double) in.readSerializable();
        mDropoffLongitude = (Double) in.readSerializable();
        mDropoffNickname = in.readString();
        mDropoffAddress = in.readString();
        mUserAgent = in.readString();
    }

    private RideParameters(boolean isPickupMyLocation,
            @Nullable String productId,
            @Nullable Double pickupLatitude,
            @Nullable Double pickupLongitude,
            @Nullable String pickupNickname,
            @Nullable String pickupAddress,
            @Nullable Double dropoffLatitude,
            @Nullable Double dropoffLongitude,
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
    public Double getPickupLatitude() {
        return mPickupLatitude;
    }

    /**
     * Gets the longitude of the pickup location of the ride. Null if no pickup location specified.
     */
    @Nullable
    public Double getPickupLongitude() {
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
    public Double getDropoffLatitude() {
        return mDropoffLatitude;
    }

    /**
     * Gets the longitude of the dropoff location of the ride. Null if no dropoff location specified.
     */
    @Nullable
    public Double getDropoffLongitude() {
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
     * Gets the user agent.
     */
    @Nullable
    String getUserAgent() {
        return mUserAgent; 
    }

    /**
     * Sets the user agent, describing where this {@link RequestDeeplink} came from for analytics.
     */
    void setUserAgent(@NonNull String userAgent) {
        mUserAgent = userAgent;
    }

    /**
     * Builder for {@link RideParameters} objects.
     */
    public static class Builder {

        boolean mIsPickupMyLocation = true;
        @Nullable private String mProductId;
        @Nullable private Double mPickupLatitude;
        @Nullable private Double mPickupLongitude;
        @Nullable private String mPickupNickname;
        @Nullable private String mPickupAddress;
        @Nullable private Double mDropoffLatitude;
        @Nullable private Double mDropoffLongitude;
        @Nullable private String mDropoffNickname;
        @Nullable private String mDropoffAddress;
        @Nullable private String mUserAgent;

        /**
         * Sets the product ID for the ride.
         */
        public RideParameters.Builder setProductId(@NonNull String productId) {
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
        public RideParameters.Builder setPickupLocation(Double latitude, Double longitude, @Nullable String nickname,
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
        public RideParameters.Builder setDropoffLocation(Double latitude, Double longitude, @Nullable String nickname,
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
