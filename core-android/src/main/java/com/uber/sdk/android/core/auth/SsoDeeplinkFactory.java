package com.uber.sdk.android.core.auth;

import android.app.Activity;
import com.uber.sdk.android.core.SupportedAppType;
import com.uber.sdk.core.client.SessionConfiguration;

import java.util.ArrayList;

public class SsoDeeplinkFactory {

    SsoDeeplink getSsoDeeplink(
            Activity activity,
            ArrayList<SupportedAppType> productPriority,
            SessionConfiguration sessionConfiguration) {
        return new SsoDeeplink.Builder(activity)
                .clientId(sessionConfiguration.getClientId())
                .scopes(sessionConfiguration.getScopes())
                .customScopes(sessionConfiguration.getCustomScopes())
                .redirectUri(sessionConfiguration.getRedirectUri())
                .productFlowPriority(productPriority)
                .build();
    }
}
