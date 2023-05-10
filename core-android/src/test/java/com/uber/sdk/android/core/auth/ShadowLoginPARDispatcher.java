package com.uber.sdk.android.core.auth;

import com.uber.sdk.core.client.SessionConfiguration;
import com.uber.sdk.core.client.internal.LoginPushedAuthorizationRequest;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(LoginPARDispatcher.class)
public class ShadowLoginPARDispatcher {
    @Implementation
    public static void dispatchPAR(SessionConfiguration sessionConfiguration,
                                   ResponseType responseType,
                                   LoginActivity.LoginPARCallback callback) {
        callback.onSuccess("requestUri");
    }
}