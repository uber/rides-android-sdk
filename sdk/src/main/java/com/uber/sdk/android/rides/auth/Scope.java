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

import android.support.annotation.NonNull;

import static com.uber.sdk.android.rides.auth.Scope.ScopeType.GENERAL;
import static com.uber.sdk.android.rides.auth.Scope.ScopeType.PRIVILEGED;

/**
 * An Uber API scope. See
 * <a href="https://developer.uber.com/v1/api-reference/#scopes">Scopes</a> for more
 * information.
 */
public enum Scope {

    /**
     * Pull trip data of a user's historical pickups and drop-offs.
     */
    HISTORY(GENERAL),

    /**
     * Same as History without city information.
     */
    HISTORY_LITE(GENERAL),

    /**
     * Retrieve user's available registered payment methods.
     */
    PAYMENT_METHODS(GENERAL),

    /**
     * Save and retrieve user's favorite places.
     */
    PLACES(GENERAL),

    /**
     * Access basic profile information on a user's Uber account.
     */
    PROFILE(GENERAL),

    /**
     * Access the Ride Request Widget.
     */
    RIDE_WIDGETS(GENERAL);

    @NonNull
    private ScopeType mScopeType;

    Scope(@NonNull ScopeType scopeType) {
        this.mScopeType = scopeType;
    }

    /**
     * Gets the {@link ScopeType} associated with this {@link Scope}.
     *
     * @return the type of scope.
     */
    @NonNull
    public ScopeType getScopeType() {
        return mScopeType;
    }

    /**
     * Category of {@link Scope} that describes its level of access.
     */
    public enum ScopeType {

        /**
         * {@link Scope}s that can be used without review.
         */
        GENERAL,

        /**
         * {@link Scope}s that require approval before opened to your users in production.
         */
        PRIVILEGED
    }
}
