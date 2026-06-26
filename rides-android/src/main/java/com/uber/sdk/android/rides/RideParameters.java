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

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Represents the parameters for an Uber ride.
 */
public class RideParameters implements Parcelable {

    private final boolean isPickupMyLocation;
    @Nullable private final String productId;
    @Nullable private final Double pickupLatitude;
    @Nullable private final Double pickupLongitude;
    @Nullable private final String pickupNickname;
    @Nullable private final String pickupAddress;
    @Nullable private final Double dropoffLatitude;
    @Nullable private final Double dropoffLongitude;
    @Nullable private final String dropoffNickname;
    @Nullable private final String dropoffAddress;
    @Nullable private String userAgent;

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
        dest.writeByte((byte) (isPickupMyLocation ? 1 : 0));
        dest.writeString(productId);
        dest.writeSerializable(pickupLatitude);
        dest.writeSerializable(pickupLongitude);
        dest.writeString(pickupNickname);
        dest.writeString(pickupAddress);
        dest.writeSerializable(dropoffLatitude);
        dest.writeSerializable(dropoffLongitude);
        dest.writeString(dropoffNickname);
        dest.writeString(dropoffAddress);
        dest.writeString(userAgent);
    }

    protected RideParameters(Parcel in) {
        isPickupMyLocation = in.readByte() != 0;
        productId = in.readString();
        pickupLatitude = (Double) in.readSerializable();
        pickupLongitude = (Double) in.readSerializable();
        pickupNickname = in.readString();
        pickupAddress = in.readString();
        dropoffLatitude = (Double) in.readSerializable();
        dropoffLongitude = (Double) in.readSerializable();
        dropoffNickname = in.readString();
        dropoffAddress = in.readString();
        userAgent = in.readString();
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
        this.isPickupMyLocation = isPickupMyLocation;
        this.productId = productId;
        this.pickupLatitude = pickupLatitude;
        this.pickupLongitude = pickupLongitude;
        this.pickupNickname = pickupNickname;
        this.pickupAddress = pickupAddress;
        this.dropoffLatitude = dropoffLatitude;
        this.dropoffLongitude = dropoffLongitude;
        this.dropoffNickname = dropoffNickname;
        this.dropoffAddress = dropoffAddress;
    }

    /**
     * @return True if the pickup location of the ride is set to be the device's location, false if a
     * specific pickup location has been set.
     */
    public boolean isPickupMyLocation() {
        return isPickupMyLocation;
    }

    /**
     * Gets the product ID for the ride.
     */
    @Nullable
    public String getProductId() {
        return productId;
    }

    /**
     * @return the latitude of the pickup location of the ride. Null if no pickup location specified.
     */
    @Nullable
    public Double getPickupLatitude() {
        return pickupLatitude;
    }

    /**
     * @return the longitude of the pickup location of the ride. Null if no pickup location specified.
     */
    @Nullable
    public Double getPickupLongitude() {
        return pickupLongitude;
    }

    /**
     * @return the nickname of the pickup location of the ride. Null if no pickup location specified.
     */
    @Nullable
    public String getPickupNickname() {
        return pickupNickname;
    }

    /**
     * @return the address of the pickup location of the ride. Null if no pickup location specified.
     */
    @Nullable
    public String getPickupAddress() {
        return pickupAddress;
    }

    /**
     * @return the latitude of the dropoff location of the ride. Null if no dropoff location specified.
     */
    @Nullable
    public Double getDropoffLatitude() {
        return dropoffLatitude;
    }

    /**
     * @return the longitude of the dropoff location of the ride. Null if no dropoff location specified.
     */
    @Nullable
    public Double getDropoffLongitude() {
        return dropoffLongitude;
    }

    /**
     * @return the nickname of the dropoff location of the ride. Null if no dropoff location specified.
     */
    @Nullable
    public String getDropoffNickname() {
        return dropoffNickname;
    }

    /**
     * @return the address of the dropoff location of the ride. Null if no dropoff location specified.
     */
    @Nullable
    public String getDropoffAddress() {
        return dropoffAddress;
    }

    /**
     * @return the user agent.
     */
    @Nullable
    String getUserAgent() {
        return userAgent;
    }

    /**
     * Sets the user agent, describing where this {@link RideRequestDeeplink} came from for analytics.
     * @param userAgent to set
     */
    void setUserAgent(@NonNull String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Builder for {@link RideParameters} objects.
     */
    public static class Builder {

        boolean isPickupMyLocation = true;
        @Nullable private String productId;
        @Nullable private Double pickupLatitude;
        @Nullable private Double pickupLongitude;
        @Nullable private String pickupNickname;
        @Nullable private String pickupAddress;
        @Nullable private Double dropoffLatitude;
        @Nullable private Double dropoffLongitude;
        @Nullable private String dropoffNickname;
        @Nullable private String dropoffAddress;
        @Nullable private String userAgent;

        /**
         * Sets the product ID for the ride.
         *
         * @return this {@link Builder} instance
         */
        public RideParameters.Builder setProductId(@NonNull String productId) {
            this.productId = productId;
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
         * @return this {@link Builder} instance
         */
        public RideParameters.Builder setPickupLocation(Double latitude, Double longitude, @Nullable String nickname,
                @Nullable String address) {
            pickupLatitude = latitude;
            pickupLongitude = longitude;
            pickupNickname = nickname;
            pickupAddress = address;
            isPickupMyLocation = false;
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
         * @return this {@link Builder} instance
         */
        public RideParameters.Builder setDropoffLocation(Double latitude, Double longitude, @Nullable String nickname,
                @Nullable String address) {
            dropoffLatitude = latitude;
            dropoffLongitude = longitude;
            dropoffNickname = nickname;
            dropoffAddress = address;
            return this;
        }

        /**
         * Sets the pickup location for the ride to be the device's current location.
         * @return this {@link Builder} instance
         */
        public RideParameters.Builder setPickupToMyLocation() {
            isPickupMyLocation = true;
            pickupLatitude = null;
            pickupLongitude = null;
            pickupNickname = null;
            pickupAddress = null;
            return this;
        }

        /**
         * Builds an {@link RideParameters} object.
         * @return the {@link RideParameters} generated from the parameters
         */
        public RideParameters build() {
            return new RideParameters(isPickupMyLocation, productId, pickupLatitude, pickupLongitude,
                    pickupNickname, pickupAddress, dropoffLatitude, dropoffLongitude, dropoffNickname,
                    dropoffAddress);
        }
    }
}
