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

/**
 * Possible errors that can occur during authentication.
 */
public enum AuthenticationError {

    /**
     * There was a connectivity error while trying to load the widget.
     */
    CONNECTIVITY_ISSUE,

    /**
     * Invalid client ID provided for authentication.
     */
    INVALID_CLIENT_ID,

    /**
     * General case for invalid requests (to be used as a default for errors making a request).
     */
    INVALID_PARAMETERS,

    /**
     * Redirect URI provided was invalid.
     */
    INVALID_REDIRECT_URI,

    /**
     * General case for response from server that is un-parsable.
     */
    INVALID_RESPONSE,

    /**
     * Scopes provided contains an invalid scope.
     */
    INVALID_SCOPE,

    /**
     * Redirect URI provided doesn't match one registered for client ID.
     */
    MISMATCHING_REDIRECT_URI,

    /**
     * A server error occurred during authentication.
     */
    SERVER_ERROR,

    /**
     * Authentication services temporarily unavailable.
     */
    TEMPORARILY_UNAVAILABLE,

    /**
     * Unknown error state occured.
     */
    UNKNOWN
}
