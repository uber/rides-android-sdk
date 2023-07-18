package com.uber.sdk.android.core.auth;

import static com.uber.sdk.android.core.auth.AuthUriAssembler.CLIENT_ID_PARAM;
import static com.uber.sdk.android.core.auth.AuthUriAssembler.CODE_CHALLENGE_PARAM;
import static com.uber.sdk.android.core.auth.AuthUriAssembler.PLATFORM_PARAM;
import static com.uber.sdk.android.core.auth.AuthUriAssembler.REDIRECT_PARAM;
import static com.uber.sdk.android.core.auth.AuthUriAssembler.REQUEST_URI_PARAM;
import static com.uber.sdk.android.core.auth.AuthUriAssembler.RESPONSE_TYPE_PARAM;
import static com.uber.sdk.android.core.auth.AuthUriAssembler.SCOPE_PARAM;
import static org.assertj.core.api.Assertions.assertThat;

import android.net.Uri;

import com.uber.sdk.android.core.RobolectricTestBase;
import com.uber.sdk.android.core.utils.AppProtocol;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.core.client.SessionConfiguration;

import org.junit.Test;

import java.util.Arrays;
import java.util.Locale;

public class AuthUriAssemblerTest extends RobolectricTestBase {

    private static final String REDIRECT_URI  = "https://redirectUri";
    private static final String CLIENT_ID  = "client_id";

    private SessionConfiguration sessionConfiguration = new SessionConfiguration.Builder()
            .setClientId(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .setScopes(Arrays.asList(Scope.PROFILE))
            .build();

    @Test
    public void assemble_shouldContainAllQueryParams() {
        Uri uri = AuthUriAssembler.assemble(
                ResponseType.CODE,
                sessionConfiguration,
                "challenge",
                null
        );

        assertThat(uri.getQueryParameter(CLIENT_ID_PARAM)).isEqualTo(CLIENT_ID);
        assertThat(uri.getQueryParameter(REDIRECT_PARAM)).isEqualTo(REDIRECT_URI);
        assertThat(uri.getQueryParameter(RESPONSE_TYPE_PARAM)).isEqualTo(ResponseType.CODE.name().toLowerCase(Locale.ROOT));
        assertThat(uri.getQueryParameter(SCOPE_PARAM)).isEqualTo(AuthUtils.getScopes(sessionConfiguration));
        assertThat(uri.getQueryParameter(CODE_CHALLENGE_PARAM)).isEqualTo("challenge");
        assertThat(uri.getQueryParameter(PLATFORM_PARAM)).isEqualTo(AppProtocol.PLATFORM);
    }

    @Test
    public void assemble_whenRequestUriParamIsPresent_shouldContainAllQueryParams() {
        Uri uri = AuthUriAssembler.assemble(
                ResponseType.CODE,
                sessionConfiguration,
                "challenge",
                "request_uri"
        );

        assertThat(uri.getQueryParameter(CLIENT_ID_PARAM)).isEqualTo(CLIENT_ID);
        assertThat(uri.getQueryParameter(REDIRECT_PARAM)).isEqualTo(REDIRECT_URI);
        assertThat(uri.getQueryParameter(RESPONSE_TYPE_PARAM)).isEqualTo(ResponseType.CODE.name().toLowerCase(Locale.ROOT));
        assertThat(uri.getQueryParameter(SCOPE_PARAM)).isEqualTo(AuthUtils.getScopes(sessionConfiguration));
        assertThat(uri.getQueryParameter(CODE_CHALLENGE_PARAM)).isEqualTo("challenge");
        assertThat(uri.getQueryParameter(PLATFORM_PARAM)).isEqualTo(AppProtocol.PLATFORM);
        assertThat(uri.getQueryParameter(REQUEST_URI_PARAM)).isEqualTo("request_uri");
    }
}
