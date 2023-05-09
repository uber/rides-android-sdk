package com.uber.sdk.android.core.auth;

import com.uber.sdk.core.auth.internal.OAuth2Service;
import com.uber.sdk.core.client.SessionConfiguration;
import com.uber.sdk.core.client.internal.LoginPushedAuthorizationRequest;

import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class LoginPARDispatcher {

    public static void dispatchPAR(SessionConfiguration sessionConfiguration,
                                   ResponseType responseType,
                                   LoginActivity.LoginPARCallback callback) {
        new LoginPushedAuthorizationRequest(
                new Retrofit.Builder()
                        .baseUrl(sessionConfiguration.getLoginHost())
                        .addConverterFactory(MoshiConverterFactory.create())
                        .build()
                        .create(OAuth2Service.class),
                sessionConfiguration.getProfileHint(),
                sessionConfiguration.getClientId(),
                responseType.name(),
                callback
        ).execute();
    }
}
