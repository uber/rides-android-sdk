/*
 * Copyright (c) 2016 Uber Technologies, Inc.
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

package com.uber.sdk.android.core.auth;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.uber.sdk.android.core.RobolectricTestBase;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.rides.client.SessionConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.util.ActivityController;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.robolectric.Shadows.shadowOf;

/**
 * Tests {@link LoginActivity}
 */
public class LoginActivityTest extends RobolectricTestBase {

    private static final String REDIRECT_URI = "localHost1234";
    private static final String CLIENT_ID = "clientId1234";
    private static final String CODE = "auth123Code";
    private LoginActivity loginActivity;

    @Before
    public void setup() {
        SessionConfiguration loginConfiguration = new SessionConfiguration.Builder().setClientId(CLIENT_ID).setRedirectUri(REDIRECT_URI).build();

        Intent data = LoginActivity.newIntent(Robolectric.setupActivity(Activity.class), loginConfiguration, ResponseType.TOKEN);
        loginActivity = Robolectric.buildActivity(LoginActivity.class).withIntent(data).create().get();
    }

    @Test
    public void onLoginLoad_whenAccessTokenGenerated_shouldReturnAccessTokenResult() {
        String tokenString = "accessToken1234";

        String redirectUrl = REDIRECT_URI + "?access_token=accessToken1234&expires_in=" + 2592000
                + "&scope=history&refresh_token=refreshToken1234&token_type=bearer";

        ShadowActivity shadowActivity = shadowOf(loginActivity);
        loginActivity.onTokenReceived(Uri.parse(redirectUrl));

        assertNotNull(shadowActivity.getResultIntent());
        assertEquals(shadowActivity.getResultCode(), Activity.RESULT_OK);

        AccessToken resultAccessToken =
                AuthUtils.createAccessToken(shadowActivity.getResultIntent());

        assertEquals(resultAccessToken.getExpiresIn(), 2592000);
        assertEquals(resultAccessToken.getToken(), tokenString);
        assertEquals(resultAccessToken.getScopes().size(), 1);
        assertTrue(resultAccessToken.getScopes().contains(Scope.HISTORY));
        assertThat(shadowActivity.isFinishing()).isTrue();
    }

    @Test
    public void onLoginLoad_whenErrorOccurs_shouldReturnErrorResultIntent() {
        ShadowActivity shadowActivity = shadowOf(loginActivity);
        loginActivity.onError(AuthenticationError.MISMATCHING_REDIRECT_URI);

        assertThat(shadowActivity.getResultIntent()).isNotNull();

        assertThat(shadowActivity.getResultCode()).isEqualTo(Activity.RESULT_CANCELED);
        assertThat(getScopeFromIntent(shadowActivity.getResultIntent()))
                .isEqualTo(AuthenticationError.MISMATCHING_REDIRECT_URI);
        assertThat(shadowActivity.isFinishing()).isTrue();
    }

    @Test
    public void onLoginLoad_whenCodeReturned_shouldReturnErrorResultIntent() {
        ShadowActivity shadowActivity = shadowOf(loginActivity);

        String redirectUrl = REDIRECT_URI + "?code=" + CODE;

        loginActivity.onCodeReceived(Uri.parse(redirectUrl));

        assertThat(shadowActivity.getResultIntent()).isNotNull();

        assertThat(shadowActivity.getResultCode()).isEqualTo(Activity.RESULT_OK);
        assertThat(shadowActivity.getResultIntent().getStringExtra(LoginManager.EXTRA_CODE_RECEIVED)).isEqualTo(CODE);
        assertThat(shadowActivity.isFinishing()).isTrue();
    }

    @Test
    public void onLoginLoad_withEmptySessionConfiguration_shouldReturnErrorResultIntent() {
        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class);
        controller.create();

        ShadowActivity shadowActivity = shadowOf(controller.get());

        assertThat(shadowActivity.getResultCode()).isEqualTo(Activity.RESULT_CANCELED);

        assertThat(shadowActivity.getResultIntent()).isNotNull();

        assertThat(getScopeFromIntent(shadowActivity.getResultIntent()))
                .isEqualTo(AuthenticationError.UNAVAILABLE);
        assertThat(shadowActivity.isFinishing()).isTrue();
    }

    @Test
    public void onLoginLoad_withEmptyScopes_shouldReturnErrorResultIntent() {

        Intent intent = new Intent();
        intent.putExtra(LoginActivity.EXTRA_SESSION_CONFIGURATION, new SessionConfiguration.Builder().setClientId("clientId").build());

        ActivityController<LoginActivity> controller = Robolectric.buildActivity(LoginActivity.class)
                .withIntent(intent)
                .create();

        ShadowActivity shadowActivity = shadowOf(controller.get());

        assertThat(shadowActivity.getResultCode()).isEqualTo(Activity.RESULT_CANCELED);

        assertThat(shadowActivity.getResultIntent()).isNotNull();

        assertThat(getScopeFromIntent(shadowActivity.getResultIntent()))
                .isEqualTo(AuthenticationError.INVALID_SCOPE);
        assertThat(shadowActivity.isFinishing()).isTrue();
    }

    private AuthenticationError getScopeFromIntent(Intent intent) {
        return AuthenticationError.fromString(intent.getStringExtra(LoginManager.EXTRA_ERROR));
    }
}
