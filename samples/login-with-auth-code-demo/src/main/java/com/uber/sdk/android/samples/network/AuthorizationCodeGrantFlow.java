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

package com.uber.sdk.android.samples.network;

import androidx.annotation.NonNull;

import com.uber.sdk.android.samples.model.AccessToken;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class AuthorizationCodeGrantFlow {
    private final static String GRANT_TYPE = "authorization_code";

    private final AuthService authService;
    private final String clientId;
    private final String redirectUri;
    private final String authCode;
    private final String codeVerifier;

    /**
     * @param baseUrl domain/authority to send the oauth token request
     * @param clientId oauth clientId of the app
     * @param redirectUri redirectUri configured as part of the oauth flow
     * @param authCode authCode that was delivered as part of redirectUri when user was authenticated
     * @param codeVerifier code verifier that was generated as part of code challenge-verifier pair
     */
    public AuthorizationCodeGrantFlow(
            String baseUrl,
            String clientId,
            String redirectUri,
            String authCode,
            String codeVerifier
    ) {
        this.authService = createOAuthService(baseUrl);
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.authCode = authCode;
        this.codeVerifier = codeVerifier;
    }

    public void execute(TokenRequestFlowCallback callback) {
        authService.token(
                clientId,
                codeVerifier,
                GRANT_TYPE,
                redirectUri,
                authCode
        ).enqueue(
                new Callback<AccessToken>() {
                    @Override
                    public void onResponse(
                            @NonNull Call<AccessToken> call,
                            @NonNull Response<AccessToken> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess(response.body());
                        } else {
                            onFailure(call, new RuntimeException("Token request failed with code " + response.code()));
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<AccessToken> call,
                            @NonNull Throwable t) {
                        callback.onFailure(t);
                    }
                }
        );
    }

    private static AuthService createOAuthService(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(AuthService.class);
    }
}
