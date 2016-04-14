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
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

/**
 * Uber SDK management class. Uber SDK Classes behavior is undefined if the SDK is not initialized.
 */
public final class UberSdk {

    private static final String CLIENT_ID_KEY = "clientId";
    private static final String SERVER_TOKEN_KEY = "serverToken";
    private static final String SDK_PREFERENCES_NAME = "uberSdkConfig";
    private static final String SANDBOX_MODE_KEY = "sandboxMode";
    private static final String REDIRECT_URI_KEY = "redirectUri";
    private static final String REGION_KEY = "region";

    @Nullable
    private static SharedPreferences sSdkPreferences;

    private UberSdk() { }

    @VisibleForTesting
    static void clearPrefs() {
        if (sSdkPreferences != null) {
            sSdkPreferences.edit().clear().apply();
        }
        sSdkPreferences = null;
    }

    /**
     * Initializes the Uber SDK. Uber SDK Classes behavior is undefined if the SDK is not initialized
     * prior to their use.
     *
     * @param context The application {@link Context}.
     */
    public static void initialize(@NonNull Context context, @NonNull String clientId) {
        sSdkPreferences = context.getSharedPreferences(SDK_PREFERENCES_NAME, Context.MODE_PRIVATE);
        sSdkPreferences.edit().putString(CLIENT_ID_KEY, clientId).apply();
    }

    /**
     * Gets the Client ID to be used by the SDK for requests. Will return the client ID or throw an {@link
     * IllegalStateException} if the SDK has not been initialized yet.
     *
     * @return The Client ID.
     * @throws IllegalStateException if the Uber SDK has not been initialized yet.
     */
    @NonNull
    public static String getClientId() {
        return getPreferences().getString(CLIENT_ID_KEY, "");
    }

    /**
     * Gets the Redirect URI to be used for implicit grant.
     *
     * @return The Redirect URI.
     * @throws IllegalStateException if the Uber SDK has not been initialized yet.
     */
    @Nullable
    public static String getRedirectUri() {
        return getPreferences().getString(REDIRECT_URI_KEY, null);
    }

    /**
     * Gets the current {@link Region} the SDK is using.
     * Defaults to {@link Region#WORLD}.
     *
     * @return The {@link Region} the SDK is using.
     * @throws IllegalStateException if the Uber SDK has not been initialized yet.
     */
    @NonNull
    public static Region getRegion() {
        return Region.valueOf(getPreferences().getString(REGION_KEY, Region.WORLD.name()));
    }

    /**
     * Gets the Server Token to be used by the SDK for requests.
     *
     * @return The Server Token.
     * @throws IllegalStateException if the Uber SDK has not been initialized yet.
     */
    @Nullable
    public static String getServerToken() {
        return getPreferences().getString(SERVER_TOKEN_KEY, null);
    }

    /**
     * Gets whether the SDK is configured for the Sandbox environment.
     *
     * @return {@code true} if SDK is in Sandbox mode, {@code false} otherwise.
     * @throws IllegalStateException if the Uber SDK has not been initialized yet.
     */
    public static boolean isSandboxMode() {
        return getPreferences().getBoolean(SANDBOX_MODE_KEY, false);
    }

    /**
     * Sets the redirect URI that is registered for this application.
     *
     * @param redirectUri the redirect URI {@link String} for this application.
     * @throws IllegalStateException if the Uber SDK has not been initialized yet.
     */
    public static void setRedirectUri(@NonNull String redirectUri) {
        getPreferences().edit().putString(REDIRECT_URI_KEY, redirectUri).apply();
    }

    /**
     * Set the {@link com.uber.sdk.android.rides.UberSdk.Region} your app is registered in.
     * Used to determine what endpoints to send requests to.
     *
     * @param region The {@link com.uber.sdk.android.rides.UberSdk.Region} the SDK should use
     * @throws IllegalStateException if the Uber SDK has not been initialized yet.
     */
    public static void setRegion(@NonNull Region region) {
        getPreferences().edit().putString(REGION_KEY, region.name()).apply();
    }

    /**
     * Configures the SDK for Sandbox or Production. Defaults to {@code false} for production.
     *
     * @param sandboxMode {@code true} for configuring to Sandbox environment.
     * @throws IllegalStateException if the Uber SDK has not been initialized yet.
     */
    public static void setSandboxMode(boolean sandboxMode) {
        getPreferences().edit().putBoolean(SANDBOX_MODE_KEY, sandboxMode).apply();
    }

    /**
     * Sets the server token that is registered for this application.
     *
     * @param serverToken the server token for this application.
     * @throws IllegalStateException if the Uber SDK has not been initialized yet.
     */
    public static void setServerToken(@NonNull String serverToken) {
        getPreferences().edit().putString(SERVER_TOKEN_KEY, serverToken).apply();
    }

    /**
     * Get the {@link SharedPreferences} powering the {@link UberSdk}.
     *
     * @throws IllegalStateException if the Uber SDK has not been initialized yet.
     */
    @NonNull
    private static SharedPreferences getPreferences() {
        if (sSdkPreferences == null) {
            throw new IllegalStateException("The SDK has not been initialized yet, call "
                    + UberSdk.class.getSimpleName() + ".initialize early.");
        }
        return sSdkPreferences;
    }

    /**
     * Region that the SDK should use for making requests.
     */
    public enum Region {
        /**
         * The default region.
         */
        WORLD("uber.com"),

        /**
         * China, for apps that are based in China.
         */
        CHINA("uber.com.cn");

        public final String domain;

        Region(@NonNull String domain) {
            this.domain = domain;
        }
    }
}
