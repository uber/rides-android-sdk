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

package com.uber.sdk.android.core.auth;

import androidx.annotation.NonNull;

import com.uber.sdk.core.auth.AccessToken;

/**
 * Interface for {@link LoginActivity} to notify listeners of events.
 */
public interface LoginCallback {

    /**
     * Called when the login process is cancelled by user.
     */
    void onLoginCancel();

    /**
     * Called when there is an {@link AuthenticationError} when extracting the {@link AccessToken}
     * from the authorization process.
     *
     * @param error the {@link AuthenticationError} that occurred.
     */
    void onLoginError(@NonNull AuthenticationError error);

    /**
     * Called when an {@link AccessToken} string has been extracted from the authorization process.
     *
     * @param accessToken the {@link AccessToken} that was received.
     */
    void onLoginSuccess(@NonNull AccessToken accessToken);

    /**
     * Called when authorization code has been returned to the redirect uri. {@link AccessToken} must be retrieved using
     * Client Secret, see https://developer.uber.com/docs/authentication#section-step-two-receive-redirect
     *
     * @param authorizationCode the authorizationCode that can be used to retrieve {@link AccessToken}
     * @deprecated Should use Custom Tab flow with registered Redirect URI
     */
    @Deprecated
    void onAuthorizationCodeReceived(@NonNull String authorizationCode);
}
