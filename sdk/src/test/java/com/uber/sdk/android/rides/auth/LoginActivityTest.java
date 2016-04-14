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

package com.uber.sdk.android.rides.auth;

import android.app.Activity;
import android.content.Intent;

import com.google.common.collect.ImmutableList;
import com.uber.sdk.android.rides.RobolectricTestBase;
import com.uber.sdk.android.rides.SdkPreferences;
import com.uber.sdk.android.rides.UberSdk;
import com.uber.sdk.android.rides.UberSdkAccessor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowActivity;

import java.util.Collection;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

/**
 * Tests {@link LoginActivity}
 */
public class LoginActivityTest extends RobolectricTestBase {

    private static final String ACCESS_TOKEN_STRING = "accessToken1234";
    private static final long EXPIRATION_TIME = 1458770906206l;
    private static final String REDIRECT_URI = "http://localhost:1234";
    private static final String REQUEST_URL = "https://login.uber.com/oauth/v2/authorize?client_id=CLIENT_ID&redirect_uri=" + REDIRECT_URI
            + ":1234&response_type=token&scope=profile";

    private Collection<Scope> mScopes = ImmutableList.of(Scope.PROFILE, Scope.HISTORY);
    private LoginActivity mActivity;
    private SdkPreferences mSdkPreferences;

    @Before
    public void setup() {
        mSdkPreferences = new SdkPreferences(RuntimeEnvironment.application);
        mSdkPreferences.setRedirectUri("localHost1234");
        UberSdk.initialize(RuntimeEnvironment.application, "clientId1234");
        
        Intent data = LoginActivity.newIntent(Robolectric.setupActivity(Activity.class), mScopes);
        mActivity = Robolectric.buildActivity(LoginActivity.class).withIntent(data).create().get();
    }

    @Test
    public void onLoginLoad_whenAccessTokenGenerated_shouldReturnAccessTokenResult() {
        Date expirationDate = new Date(1458770906206l);
        String tokenString = "accessToken1234";
        AccessToken accessToken = new AccessToken(expirationDate, mScopes, tokenString);

        ShadowActivity shadowActivity = shadowOf(mActivity);
        mActivity.onLoginSuccess(accessToken);

        assertNotNull(shadowActivity.getResultIntent());
        assertEquals(shadowActivity.getResultCode(), Activity.RESULT_OK);

        AccessToken resultAccessToken = shadowActivity.getResultIntent().getParcelableExtra(
                LoginManager.ACCESS_TOKEN_KEY);
        assertEquals(resultAccessToken.getExpirationTime(), expirationDate);
        assertEquals(resultAccessToken.getToken(), tokenString);
        assertEquals(resultAccessToken.getScopes().size(), 2);
        assertTrue(resultAccessToken.getScopes().contains(Scope.PROFILE));
        assertTrue(resultAccessToken.getScopes().contains(Scope.HISTORY));
    }

    @Test
    public void onLoginLoad_whenErrorOccurs_shouldReturnErrorResultIntent() {
        ShadowActivity shadowActivity = shadowOf(mActivity);
        mActivity.onLoginError(AuthenticationError.MISMATCHING_REDIRECT_URI);

        assertNotNull(shadowActivity.getResultIntent());
        assertEquals(shadowActivity.getResultCode(), Activity.RESULT_CANCELED);
        assertEquals(AuthenticationError.MISMATCHING_REDIRECT_URI, shadowActivity.getResultIntent().getSerializableExtra
                (LoginManager.LOGIN_ERROR_KEY));
    }

    @Test
    public void onLoginLoad_withEmptyScopes_shouldReturnErrorResultIntent() {
        mActivity = Robolectric.buildActivity(LoginActivity.class).withIntent(new Intent()).create().get();
        ShadowActivity shadowActivity = shadowOf(mActivity);

        assertNotNull(shadowActivity.getResultIntent());
        assertEquals(shadowActivity.getResultCode(), Activity.RESULT_CANCELED);
        assertEquals(AuthenticationError.INVALID_SCOPE, shadowActivity.getResultIntent().getSerializableExtra
                (LoginManager.LOGIN_ERROR_KEY));
    }

    @Test
    public void onLoginLoad_ifUserCancels_shouldReturnEmptyIntentCancelled() {
        ShadowActivity shadowActivity = shadowOf(mActivity);
        mActivity.onLoginCancel();

        assertNull(shadowActivity.getResultIntent().getParcelableExtra(LoginManager.ACCESS_TOKEN_KEY));
        assertNull(shadowActivity.getResultIntent().getParcelableExtra(LoginManager.LOGIN_ERROR_KEY));
        assertEquals(shadowActivity.getResultCode(), Activity.RESULT_CANCELED);
    }

    @After
    public void teardown() {
        UberSdkAccessor.clearPrefs();
    }
}
