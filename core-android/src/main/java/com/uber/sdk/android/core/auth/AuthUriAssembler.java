package com.uber.sdk.android.core.auth;

import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.uber.sdk.android.core.BuildConfig;
import com.uber.sdk.android.core.utils.AppProtocol;
import com.uber.sdk.core.client.SessionConfiguration;

import java.util.Locale;

public class AuthUriAssembler {
    static final String CLIENT_ID_PARAM = "client_id";
    static final String ENDPOINT = "auth";
    static final String HTTPS = "https";
    static final String PATH = "oauth/v2/universal/authorize";
    static final String REDIRECT_PARAM = "redirect_uri";
    static final String RESPONSE_TYPE_PARAM = "response_type";
    static final String SCOPE_PARAM = "scope";
    static final String REQUEST_URI_PARAM = "request_uri";
    static final String PLATFORM_PARAM = "sdk";
    static final String SDK_VERSION_PARAM = "sdk_version";
    static final String FLOW_TYPE_PARAM = "flow_type";
    static final String CODE_CHALLENGE_PARAM = "code_challenge";
    public static Uri assemble(
            @NonNull ResponseType responseType,
            @NonNull SessionConfiguration configuration,
            String codeChallenge,
            String requestUri) {

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(HTTPS)
                .authority(ENDPOINT + "." + configuration.getEndpointRegion().getDomain())
                .appendEncodedPath(PATH)
                .appendQueryParameter(CLIENT_ID_PARAM, configuration.getClientId())
                .appendQueryParameter(RESPONSE_TYPE_PARAM, responseType.name().toLowerCase(
                        Locale.US))
                .appendQueryParameter(PLATFORM_PARAM, AppProtocol.PLATFORM)
                .appendQueryParameter(REDIRECT_PARAM, configuration.getRedirectUri())
                .appendQueryParameter(SDK_VERSION_PARAM, BuildConfig.VERSION_NAME)
                .appendQueryParameter(SCOPE_PARAM, AuthUtils.getScopes(configuration))
                .appendQueryParameter(CODE_CHALLENGE_PARAM, codeChallenge);
        if (!TextUtils.isEmpty(requestUri)) {
            builder.appendQueryParameter(REQUEST_URI_PARAM, requestUri);
        }

        return builder.build();
    }
}
