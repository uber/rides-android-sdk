package com.uber.sdk.android.rides;

/**
 * Used to access the otherwise static {@link UberSdk} for testing.
 */
public final class UberSdkAccessor {

    /**
     * Clear the {@link UberSdk}, uninitializing it and removing all stored values.
     */
    public static void clearPrefs(){
        UberSdk.clearPrefs();
    }
}
