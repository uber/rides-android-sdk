package com.uber.sdk.android.core.auth;

import android.app.Activity;
import com.uber.sdk.core.client.SessionConfiguration;

public class SsoDeeplinkFactory {

    SsoDeeplink getSsoDeeplink(Activity activity, SessionConfiguration sessionConfiguration) {
        return new SsoDeeplink.Builder(activity)
                .clientId(sessionConfiguration.getClientId())
                .scopes(sessionConfiguration.getScopes())
                .customScopes(sessionConfiguration.getCustomScopes())
                .redirectUri(sessionConfiguration.getRedirectUri())
                .build();
    }
}
