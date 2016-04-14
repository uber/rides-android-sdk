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

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.AttributeSet;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.uber.sdk.android.rides.R;
import com.uber.sdk.android.rides.RideRequestViewError;
import com.uber.sdk.android.rides.UberSdk;

import java.util.Collection;
import java.util.HashSet;

/**
 * A reusable view that shows the Uber login screen and processes authorization to generate an {@link AccessToken}.
 */
public class LoginView extends LinearLayout {

    @NonNull private Collection<Scope> mScopes = new HashSet<>();
    @Nullable private LoginCallback mLoginCallback;

    private WebView mWebView;

    public LoginView(Context context) {
        this(context, null);
    }

    public LoginView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(context);
    }

    /**
     * Stops all current loading and brings up a blank page.
     */
    public void cancelLoad() {
        mWebView.stopLoading();
        mWebView.loadUrl("about:blank");
    }

    /**
     * Sets the {@link LoginCallback} for the {@link LoginView} to pass back success or error events.
     */
    public void setLoginCallback(@NonNull LoginCallback callback) {
        mLoginCallback = callback;
    }

    /**
     * Sets the {@link Scope}s that the {@link LoginView} should request access for.
     */
    public void setScopes(@NonNull Collection<Scope> scopes) {
        mScopes = scopes;
    }

    /**
     * Load the login view.
     * Requires that the redirect URI has been set in {@link UberSdk}.
     */
    public void load() {
        String redirectUri = UberSdk.getRedirectUri();
        if (redirectUri == null) {
            if (mLoginCallback != null) {
                mLoginCallback.onLoginError(AuthenticationError.INVALID_REDIRECT_URI);
            }

            return;
        }

        mWebView.setWebViewClient(new OAuthWebViewClient(redirectUri, mLoginCallback));
        mWebView.loadUrl(LoginView.buildUrl(redirectUri, mScopes));
    }

    /**
     * Builds a URL {@link String} using the necessary parameters to load in the {@link WebView}.
     *
     * @return the URL to load in the {@link WebView}
     */
    @NonNull
    @VisibleForTesting static String buildUrl(@NonNull String redirectUri, @NonNull Collection<Scope> scopes) {
        final String CLIENT_ID_PARAM = "client_id";
        final String ENDPOINT = "login";
        final String HTTPS = "https";
        final String PATH = "oauth/v2/authorize";
        final String REDIRECT_PARAM = "redirect_uri";
        final String RESPONSE_TYPE_PARAM = "response_type";
        final String SCOPE_PARAM = "scope";
        final String SHOW_FB_PARAM = "show_fb";

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(HTTPS)
                .authority(ENDPOINT + "." + UberSdk.getRegion().domain)
                .appendEncodedPath(PATH)
                .appendQueryParameter(CLIENT_ID_PARAM, UberSdk.getClientId())
                .appendQueryParameter(REDIRECT_PARAM, redirectUri)
                .appendQueryParameter(RESPONSE_TYPE_PARAM, "token")
                .appendQueryParameter(SCOPE_PARAM, AuthUtils.scopeCollectionToString(scopes))
                .appendQueryParameter(SHOW_FB_PARAM, "false");

        return builder.build().toString();
    }

    /**
     * Sets up the inner web view layout and adds it to the layout.
     */
    private void init(@NonNull Context context) {
        inflate(context, R.layout.ub__login_view, this);
        mWebView = (WebView) findViewById(R.id.ub__login_webview);
    }

    /**
     * Custom {@link WebViewClient} for authorization.
     */
    @VisibleForTesting
    class OAuthWebViewClient extends WebViewClient {

        private static final String ERRORS = "errors";

        @Nullable private LoginCallback mCallback;
        @NonNull private String mRedirectUri;

        /**
         * Initialize the {@link WebView} client.
         *
         * @param redirectUri the redirect URI {@link String} that will contain the access token information
         * @param callback {@link LoginCallback} to use to notify caller of success/failure events
         */
        public OAuthWebViewClient(@NonNull String redirectUri, @Nullable LoginCallback callback) {
            mCallback = callback;
            mRedirectUri = redirectUri;
        }

        @Override
        public void onReceivedError(
                WebView view, WebResourceRequest request, WebResourceError error) {
            if (mCallback != null) {
                mCallback.onLoginError(AuthenticationError.CONNECTIVITY_ISSUE);
            }
        }

        @Override
        public void onReceivedHttpError(
                WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            if (mCallback != null) {
                mCallback.onLoginError(AuthenticationError.CONNECTIVITY_ISSUE);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith(mRedirectUri)) {
                Uri uri = Uri.parse(url);
                try {
                    AccessToken accessToken = AuthUtils.generateAccessTokenFromUrl(uri);
                    if (mCallback != null) {
                        mCallback.onLoginSuccess(accessToken);
                    }
                } catch (LoginAuthenticationException e) {
                    if (mCallback != null) {
                        mCallback.onLoginError(e.getAuthenticationError());
                    }
                }

                return true;
            }

            if (url.contains(ERRORS)) {
                if (mCallback != null) {
                    mCallback.onLoginError(AuthenticationError.MISMATCHING_REDIRECT_URI);
                }

                return true;
            }

            return super.shouldOverrideUrlLoading(view, url);
        }
    }
}
