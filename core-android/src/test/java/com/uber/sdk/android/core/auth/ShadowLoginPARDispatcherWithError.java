package com.uber.sdk.android.core.auth;

import com.uber.sdk.core.client.SessionConfiguration;
import com.uber.sdk.core.client.internal.LoginPARRequestException;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(LoginPARDispatcher.class)
public class ShadowLoginPARDispatcherWithError {
    @Implementation
    public static void dispatchPAR(SessionConfiguration sessionConfiguration,
                                   ResponseType responseType,
                                   LoginActivity.LoginPARCallback callback) {
        callback.onError(null);
    }
}