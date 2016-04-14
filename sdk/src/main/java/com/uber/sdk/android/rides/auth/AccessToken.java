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

package com.uber.sdk.android.rides.auth;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An access token for making requests to the Uber API.
 */
public class AccessToken implements Parcelable {

    static final String KEY_EXPIRATION_TIME = "expires_in";
    static final String KEY_SCOPES = "scope";
    static final String KEY_TOKEN = "access_token";

    @NonNull private final Date mExpirationTime;
    @NonNull private final Set<Scope> mScopes;
    @NonNull private final String mToken;

    /**
     * @param scopes the {@link Scope}s this access token works for.
     * @param expirationTime the time that the access token expires.
     * @param token the Uber API access token.
     */
    public AccessToken(@NonNull Date expirationTime, @NonNull Collection<Scope> scopes, @NonNull String token) {
        mExpirationTime = expirationTime;
        mScopes = new HashSet<>(scopes);
        mToken = token;
    }

    protected AccessToken(@NonNull Parcel in) {
        mExpirationTime = new Date(in.readLong());
        List<Scope> scopeList = new ArrayList<>();
        in.readList(scopeList, null);
        mScopes = new HashSet<>(scopeList);
        mToken = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mExpirationTime.getTime());
        dest.writeList(new ArrayList<>(mScopes));
        dest.writeString(mToken);
    }

    /**
     * Gets the time the {@link AccessToken} expires at.
     *
     * @return the expiration time.
     */
    @NonNull
    public Date getExpirationTime() {
        return mExpirationTime;
    }

    /**
     * Gets the {@link Scope}s the access token works for.
     *
     * @return the scopes.
     */
    @NonNull
    public Collection<Scope> getScopes() {
        return Collections.unmodifiableCollection(mScopes);
    }

    /**
     * Gets the raw token used to make API requests
     *
     * @return the raw token.
     */
    @NonNull
    public String getToken() {
        return mToken;
    }

    public static final Creator<AccessToken> CREATOR = new Creator<AccessToken>() {
        @Override
        public AccessToken createFromParcel(Parcel in) {
            return new AccessToken(in);
        }

        @Override
        public AccessToken[] newArray(int size) {
            return new AccessToken[size];
        }
    };
}
