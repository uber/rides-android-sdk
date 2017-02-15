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

package com.uber.sdk.android.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.uber.sdk.rides.client.SessionConfiguration;

public class SdkPreferences {

    private static final String SERVER_TOKEN_KEY = "serverToken";
    private static final String SDK_PREFERENCES_NAME = "uberSdkConfig";
    private static final String SANDBOX_MODE_KEY = "sandboxMode";
    private static final String REDIRECT_URI_KEY = "redirectUri";
    private static final String REGION_KEY = "region";

    @NonNull private SharedPreferences sharedPreferences;

    public SdkPreferences(@NonNull Context context) {
        sharedPreferences = context.getSharedPreferences(SDK_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public boolean isSandboxMode() {
        return sharedPreferences.getBoolean(SANDBOX_MODE_KEY, false);
    }

    @Nullable
    public String getRedirectUri() {
        return sharedPreferences.getString(REDIRECT_URI_KEY, null);
    }

    @NonNull
    public SessionConfiguration.EndpointRegion getRegion() {
        return SessionConfiguration.EndpointRegion.valueOf(sharedPreferences.getString(REGION_KEY, SessionConfiguration.EndpointRegion.DEFAULT.name()));
    }

    @Nullable
    public String getServerToken() {
        return sharedPreferences.getString(SERVER_TOKEN_KEY, null);
    }

    public void setServerToken(@NonNull String serverToken) {
        sharedPreferences.edit().putString(SERVER_TOKEN_KEY, serverToken).apply();
    }

    public void setSandboxMode(boolean isSandboxMode) {
        sharedPreferences.edit().putBoolean(SANDBOX_MODE_KEY, isSandboxMode).apply();
    }

    public void setRedirectUri(@NonNull String redirectUri) {
        sharedPreferences.edit().putString(REDIRECT_URI_KEY, redirectUri).apply();
    }

    public void setRegion(@NonNull SessionConfiguration.EndpointRegion region) {
        sharedPreferences.edit().putString(REGION_KEY, region.name()).apply();
    }
}
