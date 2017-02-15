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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.uber.sdk.android.core.BuildConfig;
import com.uber.sdk.android.core.Deeplink;
import com.uber.sdk.android.core.utils.AppProtocol;
import com.uber.sdk.android.core.utils.PackageManagers;
import com.uber.sdk.core.auth.Scope;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static com.uber.sdk.android.core.UberSdk.UBER_SDK_LOG_TAG;
import static com.uber.sdk.android.core.utils.Preconditions.checkNotEmpty;
import static com.uber.sdk.android.core.utils.Preconditions.checkNotNull;
import static com.uber.sdk.android.core.utils.Preconditions.checkState;

/**
 * Provides deep link to login into the installed Uber app. For a simpler integration see
 * {@link LoginButton} or {@link LoginManager#login(Activity)}.
 */
public class SsoDeeplink implements Deeplink {

    public static final int DEFAULT_REQUEST_CODE = LoginManager.REQUEST_CODE_LOGIN_DEFAULT;

    @VisibleForTesting
    static final int MIN_VERSION_SUPPORTED = 31302;

    private static final String URI_QUERY_CLIENT_ID = "client_id";
    private static final String URI_QUERY_SCOPE = "scope";
    private static final String URI_QUERY_PLATFORM = "sdk";
    private static final String URI_QUERY_SDK_VERSION = "sdk_version";
    private static final String URI_HOST = "connect";

    private final Activity activity;
    private final String clientId;
    private final Collection<Scope> requestedScopes;
    private final Collection<String> requestedCustomScopes;
    private final int requestCode;

    AppProtocol appProtocol;

    SsoDeeplink(
            @NonNull Activity activity,
            @NonNull String clientId,
            @NonNull Collection<Scope> requestedScopes,
            @NonNull Collection<String> requestedCustomScopes,
            int requestCode) {
        this.activity = activity;
        this.clientId = clientId;
        this.requestCode = requestCode;
        this.requestedScopes = requestedScopes;
        this.requestedCustomScopes = requestedCustomScopes;
        appProtocol = new AppProtocol();
    }

    /**
     * Start {@link Activity#startActivityForResult(Intent, int)} with the right configurations. Use {@link Builder}
     * to instantiate the object.
     *
     * @throws IllegalStateException if compatible Uber app is not installed. Use {@link #isSupported()} to check.
     */
    @Override
    public void execute() {
        checkState(isSupported(), "Single sign on is not supported on the device. " +
                "Please install or update to the latest version of Uber app.");


        Intent intent = new Intent(Intent.ACTION_VIEW);
        final Uri deepLinkUri = createSsoUri();
        intent.setData(deepLinkUri);

        for (String installedPackage : AppProtocol.UBER_PACKAGE_NAMES) {
            if (PackageManagers.isPackageAvailable(activity, installedPackage)) {
                intent.setPackage(installedPackage);
                break;
            }
        }
        activity.startActivityForResult(intent, requestCode);
    }

    private Uri createSsoUri() {
        String scopes = AuthUtils.scopeCollectionToString(requestedScopes);
        if (!requestedCustomScopes.isEmpty()) {
           scopes =  AuthUtils.mergeScopeStrings(scopes,
                   AuthUtils.customScopeCollectionToString(requestedCustomScopes));
        }
        return new Uri.Builder().scheme(AppProtocol.DEEPLINK_SCHEME)
                .authority(URI_HOST)
                .appendQueryParameter(URI_QUERY_CLIENT_ID, clientId)
                .appendQueryParameter(URI_QUERY_SCOPE, scopes)
                .appendQueryParameter(URI_QUERY_PLATFORM, AppProtocol.PLATFORM)
                .appendQueryParameter(URI_QUERY_SDK_VERSION, BuildConfig.VERSION_NAME)
                .build();
    }

    /**
     * Check if SSO deep linking is supported in this device.
     *
     * @return
     */
    @Override
    public boolean isSupported() {

        PackageInfo packageInfo = null;
        for (String installedPackage : AppProtocol.UBER_PACKAGE_NAMES) {
            if (PackageManagers.isPackageAvailable(activity, installedPackage)) {
                packageInfo = PackageManagers.getPackageInfo(activity, installedPackage);
                break;
            }
        }

        return (packageInfo != null)
                && appProtocol.validateMinimumVersion(activity, packageInfo, MIN_VERSION_SUPPORTED)
                && appProtocol.validateSignature(activity, packageInfo.packageName);
    }

    public static class Builder {

        private final Activity activity;

        private String clientId;
        private Collection<Scope> requestedScopes;
        private Collection<String> requestedCustomScopes;
        private int requestCode = DEFAULT_REQUEST_CODE;

        public Builder(@NonNull Activity activity) {
            this.activity = activity;
        }

        public Builder clientId(@NonNull String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder scopes(@NonNull Scope... scopes) {
            this.requestedScopes = Arrays.asList(scopes);
            return this;
        }

        public Builder scopes(@NonNull Collection<Scope> scopes) {
            this.requestedScopes = scopes;
            return this;
        }

        public Builder customScopes(@NonNull Collection<String> customScopes) {
            this.requestedCustomScopes = customScopes;
            return this;
        }

        public Builder activityRequestCode(int requestCode) {
            this.requestCode = requestCode;
            return this;
        }

        public SsoDeeplink build() {
            checkNotNull(clientId, "Client Id must be set");

            checkNotEmpty(requestedScopes, "Scopes must be set.");

            if (requestedCustomScopes == null) {
                requestedCustomScopes = new ArrayList<>();
            }

            if (requestCode == DEFAULT_REQUEST_CODE) {
                Log.i(UBER_SDK_LOG_TAG, "Request code is not set, using default request code");
            }
            return new SsoDeeplink(activity, clientId, requestedScopes, requestedCustomScopes, requestCode);
        }
    }
}
