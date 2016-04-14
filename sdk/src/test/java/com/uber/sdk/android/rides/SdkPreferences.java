package com.uber.sdk.android.rides;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.uber.sdk.android.rides.UberSdk.Region;

public class SdkPreferences {

    private static final String SERVER_TOKEN_KEY = "serverToken";
    private static final String SDK_PREFERENCES_NAME = "uberSdkConfig";
    private static final String SANDBOX_MODE_KEY = "sandboxMode";
    private static final String REDIRECT_URI_KEY = "redirectUri";
    private static final String REGION_KEY = "region";

    @NonNull private SharedPreferences mSharedPreferences;

    public SdkPreferences(@NonNull Context context) {
        mSharedPreferences = context.getSharedPreferences(SDK_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public boolean isSandboxMode() {
        return mSharedPreferences.getBoolean(SANDBOX_MODE_KEY, false);
    }

    @Nullable
    public String getRedirectUri() {
        return mSharedPreferences.getString(REDIRECT_URI_KEY, null);
    }

    @NonNull
    public Region getRegion() {
        return Region.valueOf(mSharedPreferences.getString(REGION_KEY, Region.WORLD.name()));
    }

    @Nullable
    public String getServerToken() {
        return mSharedPreferences.getString(SERVER_TOKEN_KEY, null);
    }

    public void setServerToken(@NonNull String serverToken) {
        mSharedPreferences.edit().putString(SERVER_TOKEN_KEY, serverToken).apply();
    }

    public void setSandboxMode(boolean isSandboxMode) {
        mSharedPreferences.edit().putBoolean(SANDBOX_MODE_KEY, isSandboxMode).apply();
    }

    public void setRedirectUri(@NonNull String redirectUri) {
        mSharedPreferences.edit().putString(REDIRECT_URI_KEY, redirectUri).apply();
    }

    public void setRegion(@NonNull Region region) {
        mSharedPreferences.edit().putString(REGION_KEY, region.name()).apply();
    }
}
