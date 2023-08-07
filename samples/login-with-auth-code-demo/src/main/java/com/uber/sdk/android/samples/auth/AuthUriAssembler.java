/*
 * Copyright (c) 2023 Uber Technologies, Inc.
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

package com.uber.sdk.android.samples.auth;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.uber.sdk.android.rides.samples.BuildConfig;

import java.util.Locale;

public class AuthUriAssembler {
    static final String CLIENT_ID_PARAM = "client_id";
    static final String HTTPS = "https";
    static final String PATH = "oauth/v2/universal/authorize";
    static final String REDIRECT_PARAM = "redirect_uri";
    static final String RESPONSE_TYPE_PARAM = "response_type";
    static final String SCOPE_PARAM = "scope";
    static final String PLATFORM_PARAM = "sdk";
    static final String SDK_VERSION_PARAM = "sdk_version";
    static final String CODE_CHALLENGE_PARAM = "code_challenge";
    public static Uri assemble(
            @NonNull String clientId,
            @NonNull String scopes,
            @NonNull String responseType,
            @NonNull String codeChallenge,
            @NonNull String redirectUri) {

        Uri.Builder builder = new Uri.Builder();
        builder.scheme(HTTPS)
                .authority("auth.uber.com")
                .appendEncodedPath(PATH)
                .appendQueryParameter(CLIENT_ID_PARAM, clientId)
                .appendQueryParameter(RESPONSE_TYPE_PARAM, responseType.toLowerCase(Locale.US))
                .appendQueryParameter(PLATFORM_PARAM, "android")
                .appendQueryParameter(REDIRECT_PARAM, redirectUri)
                .appendQueryParameter(SDK_VERSION_PARAM, BuildConfig.VERSION_NAME)
                .appendQueryParameter(SCOPE_PARAM, scopes)
                .appendQueryParameter(CODE_CHALLENGE_PARAM, codeChallenge);
        return builder.build();
    }
}
