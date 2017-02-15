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
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.uber.sdk.android.core.R;
import com.uber.sdk.rides.client.SessionConfiguration;

import java.util.Locale;

/**
 * {@link android.app.Activity} that shows web view for Uber user authentication and authorization.
 */
public class LoginActivity extends Activity {

    static final String EXTRA_RESPONSE_TYPE = "RESPONSE_TYPE";
    static final String EXTRA_SESSION_CONFIGURATION = "SESSION_CONFIGURATION";

    private WebView webView;
    private ResponseType responseType;
    private SessionConfiguration sessionConfiguration;

    /**
     * Create an {@link Intent} to pass to this activity
     *
     * @param context the {@link Context} for the intent
     * @param sessionConfiguration to be used for gather clientId
     * @param responseType that is expected
     * @return an intent that can be passed to this activity
     */
    @NonNull
    public static Intent newIntent(
            @NonNull Context context,
            @NonNull SessionConfiguration sessionConfiguration,
            @NonNull ResponseType responseType) {

        final Intent data = new Intent(context, LoginActivity.class)
                .putExtra(EXTRA_SESSION_CONFIGURATION, sessionConfiguration)
                .putExtra(EXTRA_RESPONSE_TYPE, responseType);

        return data;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ub__login_activity);

        webView = (WebView) findViewById(R.id.ub__login_webview);

        sessionConfiguration = (SessionConfiguration) getIntent().getSerializableExtra(EXTRA_SESSION_CONFIGURATION);
        if (sessionConfiguration == null) {
            onError(AuthenticationError.UNAVAILABLE);
            return;
        }

        if ((sessionConfiguration.getScopes() == null || sessionConfiguration.getScopes().isEmpty())
                && (sessionConfiguration.getCustomScopes() == null  || sessionConfiguration.getCustomScopes().isEmpty())) {
            onError(AuthenticationError.INVALID_SCOPE);
            return;
        }

        responseType = (ResponseType) getIntent().getSerializableExtra(EXTRA_RESPONSE_TYPE);
        if (responseType == null) {
            onError(AuthenticationError.UNAVAILABLE);
        }

        final String redirectUri = sessionConfiguration.getRedirectUri();
        if (redirectUri == null) {
            onError(AuthenticationError.INVALID_REDIRECT_URI);
            return;
        }

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(createOAuthClient(redirectUri));
        webView.loadUrl(buildUrl(redirectUri, responseType, sessionConfiguration));
    }

    protected OAuthWebViewClient createOAuthClient(String redirectUri) {
        if (responseType == ResponseType.TOKEN) {
            return new AccessTokenClient(redirectUri);
        } else {
            return new AuthorizationCodeClient(redirectUri);
        }
    }

    void onError(@NonNull AuthenticationError error) {
        Intent data = new Intent();
        data.putExtra(LoginManager.EXTRA_ERROR, error.toStandardString());
        setResult(RESULT_CANCELED, data);
        finish();
    }

    void onTokenReceived(@NonNull Uri uri) {
        try {
            Intent data = AuthUtils.parseTokenUri(uri);

            setResult(RESULT_OK, data);
            finish();
        } catch (LoginAuthenticationException loginException) {
            onError(loginException.getAuthenticationError());
            return;
        }
    }

    void onCodeReceived(Uri uri) {
        try {
            String code = AuthUtils.parseAuthorizationCode(uri);

            setResult(RESULT_OK, new Intent().putExtra(LoginManager.EXTRA_CODE_RECEIVED, code));
            finish();
        } catch (LoginAuthenticationException loginException) {
            onError(loginException.getAuthenticationError());
            return;
        }
    }

    /**
     * Builds a URL {@link String} using the necessary parameters to load in the {@link WebView}.
     *
     * @return the URL to load in the {@link WebView}
     */
    @NonNull
    @VisibleForTesting
    String buildUrl(
            @NonNull String redirectUri,
            @NonNull ResponseType responseType,
            @NonNull SessionConfiguration configuration) {

        final String CLIENT_ID_PARAM = "client_id";
        final String ENDPOINT = "login";
        final String HTTPS = "https";
        final String PATH = "oauth/v2/authorize";
        final String REDIRECT_PARAM = "redirect_uri";
        final String RESPONSE_TYPE_PARAM = "response_type";
        final String SCOPE_PARAM = "scope";
        final String SHOW_FB_PARAM = "show_fb";
        final String SIGNUP_PARAMS = "signup_params";
        final String REDIRECT_LOGIN = "{\"redirect_to_login\":true}";



        Uri.Builder builder = new Uri.Builder();
        builder.scheme(HTTPS)
                .authority(ENDPOINT + "." + configuration.getEndpointRegion().getDomain())
                .appendEncodedPath(PATH)
                .appendQueryParameter(CLIENT_ID_PARAM, configuration.getClientId())
                .appendQueryParameter(REDIRECT_PARAM, redirectUri)
                .appendQueryParameter(RESPONSE_TYPE_PARAM, responseType.toString().toLowerCase(Locale.US))
                .appendQueryParameter(SCOPE_PARAM, getScopes(configuration))
                .appendQueryParameter(SHOW_FB_PARAM, "false")
                .appendQueryParameter(SIGNUP_PARAMS, AuthUtils.createEncodedParam(REDIRECT_LOGIN));

        return builder.build().toString();
    }

    private String getScopes(SessionConfiguration configuration) {
        String scopes = AuthUtils.scopeCollectionToString(configuration.getScopes());
        if (!configuration.getCustomScopes().isEmpty()) {
            scopes =  AuthUtils.mergeScopeStrings(scopes,
                    AuthUtils.customScopeCollectionToString(configuration.getCustomScopes()));
        }
        return scopes;
    }

    /**
     * Custom {@link WebViewClient} for authorization.
     */
    @VisibleForTesting
    abstract class OAuthWebViewClient extends WebViewClient {

        protected static final String ERROR = "error";

        @NonNull
        protected final String redirectUri;

        /**
         * Initialize the {@link WebView} client.
         *
         * @param redirectUri the redirect URI {@link String} that will contain the access token information
         */
        public OAuthWebViewClient(@NonNull String redirectUri) {
            this.redirectUri = redirectUri;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            onError(AuthenticationError.CONNECTIVITY_ISSUE);
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            onError(AuthenticationError.CONNECTIVITY_ISSUE);
        }
    }

    class AuthorizationCodeClient extends OAuthWebViewClient {

        public AuthorizationCodeClient(@NonNull String redirectUri) {
            super(redirectUri);
        }


        @Override
        public void onPageFinished(WebView view, String url) {
            if (url.startsWith(redirectUri)) {
                onCodeReceived(Uri.parse(url));
            }
        }
    }

    class AccessTokenClient extends OAuthWebViewClient {

        public AccessTokenClient(@NonNull String redirectUri) {
            super(redirectUri);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // First thing to do is check for error, because the url argument here can be the
            // redirect URL or in the case of user passing in the wrong redirect URL, url will be
            // our Uber URL.
            final Uri uri = Uri.parse(url);
            final String errorParam = uri.getQueryParameter(ERROR);
            if (!TextUtils.isEmpty(errorParam)) {
                onError(AuthenticationError.fromString(errorParam));
                return true;
            }

            if (url.startsWith(redirectUri)) {

                //OAuth 2 spec requires the access token in URL Fragment instead of query parameters.
                //Swap Fragment with Query to facilitate parsing.
                final String fragment = uri.getFragment();

                if (fragment == null) {
                    onError(AuthenticationError.INVALID_RESPONSE);
                    return true;
                }

                final Uri fragmentUri = new Uri.Builder().encodedQuery(fragment).build();

                // In case fragment contains error, we want to handle that too.
                final String error = fragmentUri.getQueryParameter(ERROR);
                if (!TextUtils.isEmpty(error)) {
                    onError(AuthenticationError.fromString(error));
                    return true;
                }

                onTokenReceived(fragmentUri);
                return true;
            }

            return super.shouldOverrideUrlLoading(view, url);
        }
    }
}
