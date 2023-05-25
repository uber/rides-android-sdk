package com.uber.sdk.android.core.auth;

import com.uber.sdk.core.client.SessionConfiguration;
import com.uber.sdk.core.client.internal.LoginPushedAuthorizationRequest;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(LoginPushedAuthorizationRequest.class)
public class ShadowLoginPARDispatcher {

    private LoginPushedAuthorizationRequest.Callback callback;

    @Implementation
    public void __constructor__ (SessionConfiguration sessionConfiguration,
                                 String responseType,
                                 LoginPushedAuthorizationRequest.Callback callback) {
        this.callback = callback;
    }

    @Implementation
    public void execute() {
        callback.onSuccess("requestUri");
    }
}