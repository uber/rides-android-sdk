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

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.customtabs.CustomTabsIntent;
import android.text.TextUtils;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.uber.sdk.android.core.BuildConfig;
import com.uber.sdk.android.core.R;
import com.uber.sdk.android.core.install.SignupDeeplink;
import com.uber.sdk.android.core.utils.CustomTabsHelper;
import com.uber.sdk.core.client.SessionConfiguration;

/**
 * {@link android.app.Activity} that shows web view for Uber user authentication and authorization.
 */
public class LoginActivity extends Activity {
    @VisibleForTesting
    static final String USER_AGENT = String.format("core-android-v%s-login_manager", BuildConfig.VERSION_NAME);

    static final String EXTRA_RESPONSE_TYPE = "RESPONSE_TYPE";
    static final String EXTRA_SESSION_CONFIGURATION = "SESSION_CONFIGURATION";
    static final String EXTRA_FORCE_WEBVIEW = "FORCE_WEBVIEW";
    static final String EXTRA_SSO_ENABLED = "SSO_ENABLED";
    static final String EXTRA_REDIRECT_TO_PLAY_STORE_ENABLED = "REDIRECT_TO_PLAY_STORE_ENABLED";

    static final String ERROR = "error";

    private ResponseType responseType;
    private SessionConfiguration sessionConfiguration;
    private boolean authStarted;

    @VisibleForTesting
    WebView webView;

    @VisibleForTesting
    SsoDeeplinkFactory ssoDeeplinkFactory = new SsoDeeplinkFactory();

    @VisibleForTesting
    CustomTabsHelper customTabsHelper = new CustomTabsHelper();


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

        return newIntent(context, sessionConfiguration, responseType, false);
    }

    /**
     * Create an {@link Intent} to pass to this activity
     *
     * @param context the {@link Context} for the intent
     * @param sessionConfiguration to be used for gather clientId
     * @param responseType that is expected
     * @param forceWebview Forced to use old webview instead of chrometabs
     * @return an intent that can be passed to this activity
     */
    @NonNull
    public static Intent newIntent(
            @NonNull Context context,
            @NonNull SessionConfiguration sessionConfiguration,
            @NonNull ResponseType responseType,
            boolean forceWebview) {

        return newIntent(context, sessionConfiguration, responseType, forceWebview, false, false);
    }

    /**
     * Create an {@link Intent} to pass to this activity
     *
     * @param context the {@link Context} for the intent
     * @param sessionConfiguration to be used for gather clientId
     * @param responseType that is expected
     * @param forceWebview Forced to use old webview instead of chrometabs
     * @param isSsoEnabled specifies whether to attempt login with SSO
     * @return an intent that can be passed to this activity
     */
    @NonNull
    static Intent newIntent(
            @NonNull Context context,
            @NonNull SessionConfiguration sessionConfiguration,
            @NonNull ResponseType responseType,
            boolean forceWebview,
            boolean isSsoEnabled,
            boolean isRedirectToPlayStoreEnabled) {

        final Intent data = new Intent(context, LoginActivity.class)
                .putExtra(EXTRA_SESSION_CONFIGURATION, sessionConfiguration)
                .putExtra(EXTRA_RESPONSE_TYPE, responseType)
                .putExtra(EXTRA_FORCE_WEBVIEW, forceWebview)
                .putExtra(EXTRA_SSO_ENABLED, isSsoEnabled)
                .putExtra(EXTRA_REDIRECT_TO_PLAY_STORE_ENABLED, isRedirectToPlayStoreEnabled);

        return data;
    }

    /**
     * Used to handle Redirect URI response from customtab or browser
     *
     * @param context
     * @param responseUri
     * @return
     */
    public static Intent newResponseIntent(Context context, Uri responseUri) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setData(responseUri);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(webView == null) {
            if(!authStarted) {
                authStarted = true;
                return;
            }

            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        authStarted = false;
        setIntent(intent);
        init();
    }

    protected void init() {
        if(getIntent().getData() != null) {
            handleResponse(getIntent().getData());
        } else {
            loadUrl();
        }
    }

    protected void loadUrl() {
        Intent intent = getIntent();

        sessionConfiguration = (SessionConfiguration) intent.getSerializableExtra(EXTRA_SESSION_CONFIGURATION);
        responseType = (ResponseType) intent.getSerializableExtra(EXTRA_RESPONSE_TYPE);

        if (!validateRequestParams()) {
            return;
        }

        String redirectUri = sessionConfiguration.getRedirectUri() != null ? sessionConfiguration
                .getRedirectUri() : getApplicationContext().getPackageName() + "uberauth";

        if (intent.getBooleanExtra(EXTRA_SSO_ENABLED, false)) {
            SsoDeeplink ssoDeeplink = ssoDeeplinkFactory.getSsoDeeplink(this, sessionConfiguration);

            if (ssoDeeplink.isSupported(SsoDeeplink.FlowVersion.REDIRECT_TO_SDK)) {
                ssoDeeplink.execute(SsoDeeplink.FlowVersion.REDIRECT_TO_SDK);
            } else {
                onError(AuthenticationError.INVALID_REDIRECT_URI);
            }
            return;
        }

        boolean forceWebview = intent.getBooleanExtra(EXTRA_FORCE_WEBVIEW, false);
        boolean isRedirectToPlayStoreEnabled = intent.getBooleanExtra(EXTRA_REDIRECT_TO_PLAY_STORE_ENABLED, false);
        if (responseType == ResponseType.CODE) {
            loadWebPage(redirectUri, ResponseType.CODE, sessionConfiguration, forceWebview);
        } else if (responseType == ResponseType.TOKEN && !(AuthUtils.isPrivilegeScopeRequired(sessionConfiguration.getScopes())
                && isRedirectToPlayStoreEnabled)) {
            loadWebPage(redirectUri, ResponseType.TOKEN, sessionConfiguration, forceWebview);
        } else {
            redirectToInstallApp(this);
        }
    }

    protected void loadWebPage(String redirectUri, ResponseType responseType, SessionConfiguration sessionConfiguration, boolean forceWebview) {
        String url = AuthUtils.buildUrl(redirectUri, responseType, sessionConfiguration);
        if (forceWebview) {
            loadWebview(url, redirectUri);
        } else {
            loadChrometab(url);
        }
    }

    protected boolean handleResponse(@NonNull Uri uri) {
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

    protected void loadWebview(String url, String redirectUri) {
        setContentView(R.layout.ub__login_activity);
        webView = (WebView) findViewById(R.id.ub__login_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(createOAuthClient(redirectUri));
        webView.loadUrl(url);
    }

    protected void loadChrometab(String url) {
        final CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
        customTabsHelper.openCustomTab(this, intent, Uri.parse(url), new CustomTabsHelper
                .BrowserFallback());
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
            Intent data = AuthUtils.parseTokenUriToIntent(uri);

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

    private boolean validateRequestParams() {
        if (sessionConfiguration == null) {
            onError(AuthenticationError.INVALID_PARAMETERS);
            return false;
        }

        if ((sessionConfiguration.getScopes() == null || sessionConfiguration.getScopes().isEmpty())
                && (sessionConfiguration.getCustomScopes() == null  || sessionConfiguration.getCustomScopes().isEmpty())) {
            onError(AuthenticationError.INVALID_SCOPE);
            return false;
        }

        if (responseType == null) {
            onError(AuthenticationError.INVALID_RESPONSE_TYPE);
            return false;
        }

        return true;
    }

    private void redirectToInstallApp(@NonNull Activity activity) {
        new SignupDeeplink(activity, sessionConfiguration.getClientId(), USER_AGENT).execute();
    }

    /**
     * Custom {@link WebViewClient} for authorization.
     */
    @VisibleForTesting
    abstract class OAuthWebViewClient extends WebViewClient {

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

        /**
         * add deprecated member "onReceivedError" to solve compatibility issue when API level < 23
         * @param view
         * @param errorCode
         * @param description
         * @param failingUrl
         */
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                receivedError();
            }
        }

        @TargetApi(23)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            receivedError();
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            receivedError();
        }

        private void receivedError(){
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
                return handleResponse(uri);
            }

            return super.shouldOverrideUrlLoading(view, url);
        }
    }
}
