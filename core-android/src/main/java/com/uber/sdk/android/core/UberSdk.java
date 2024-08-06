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

import androidx.annotation.NonNull;

import com.uber.sdk.core.client.SessionConfiguration;

import static com.uber.sdk.core.client.utils.Preconditions.checkNotNull;

/**
 * Uber SDK management class. Uber SDK Classes behavior is undefined if the SDK is not initialized.
 */
public final class UberSdk {
    public static final String UBER_SDK_LOG_TAG = "UberSDK";

    static SessionConfiguration defaultSessionConfiguration;

    private UberSdk() {
    }

    /**
     * Initializes the Uber SDK with a default {@link SessionConfiguration}. This is optional and may be removed in the future.
     * This will be used in underlying components of {@link SessionConfiguration} is not passed in directly
     *
     * @param defaultSessionConfiguration The {@link SessionConfiguration} to use for classes that call without specifying directly.
     */
    public synchronized static void initialize(@NonNull SessionConfiguration defaultSessionConfiguration) {
        UberSdk.defaultSessionConfiguration = defaultSessionConfiguration;
    }

    /**
     * Provide the default set {@link SessionConfiguration} for use in default Uber SDK Components that do not specify directly.
     * @return loginConfiguration if set, otherwise null
     */
    public static SessionConfiguration getDefaultSessionConfiguration() {
        validateInstance();
        return defaultSessionConfiguration;
    }

    /**
     * This will return the state of the UberSdk being used to hold a default {@link SessionConfiguration}
     * @return true if default {@link SessionConfiguration} has been set.
     */
    public static boolean isInitialized() {
        return defaultSessionConfiguration != null;
    }

    /**
     * Checks defaultSessionConfiguration for null
     * @throws NullPointerException if null
     */
    static void validateInstance() {
        checkNotNull(defaultSessionConfiguration, "Login Configuration must be set using initialize before use");
    }
}
