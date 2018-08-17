package com.uber.sdk.android.core;

import android.content.Context;

/**
 * List of Apps that may support functionality of the SDK.  Not all functionality is supported by all apps.
 * <p>
 * To check if this app is installed use
 * {@link com.uber.sdk.android.core.utils.AppProtocol#isInstalled(Context, SupportedAppType)}.
 */
public enum SupportedAppType {
    UBER,
    UBER_EATS,
}
