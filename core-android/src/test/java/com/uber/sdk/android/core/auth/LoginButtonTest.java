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
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.util.AttributeSet;

import com.google.common.collect.Sets;
import com.uber.sdk.android.core.R;
import com.uber.sdk.android.core.RobolectricTestBase;
import com.uber.sdk.core.auth.AccessTokenStorage;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.core.client.SessionConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class LoginButtonTest extends RobolectricTestBase {

    private static final HashSet<Scope> SCOPES = Sets.newHashSet(Scope.HISTORY, Scope.REQUEST_RECEIPT);
    private static final int REQUEST_CODE = 11133;
    private Activity activity;

    @Mock
    LoginManager loginManager;

    @Mock
    LoginCallback loginCallback;

    @Mock
    AccessTokenStorage accessTokenStorage;

    private LoginButton loginButton;

    @Before
    public void setup() {
        activity = Robolectric.buildActivity(Activity.class).create().get();
    }

    @Test
    public void testButtonClickWithAllParametersSetFromJava_shouldTriggerLogin() {
        loginButton = new TestLoginButton(activity, loginManager);
        loginButton.setCallback(loginCallback);
        loginButton.setScopes(SCOPES);
        loginButton.setRequestCode(REQUEST_CODE);

        loginButton.callOnClick();

        verify(loginManager).login(eq(activity));
    }

    @Test
    public void testButtonClickWithoutRequestCode_shouldUseDefaultCode() {
        loginButton = new TestLoginButton(activity, loginManager);
        loginButton.setCallback(loginCallback);
        loginButton.setScopes(SCOPES);

        loginButton.callOnClick();

        verify(loginManager).login(eq(activity));
    }

    @Test
    public void testButtonClickWithScopesFromXml_shouldUseParseScopes() {
        AttributeSet attributeSet = Robolectric.buildAttributeSet()
                .addAttribute(R.attr.ub__scopes, "history|request_receipt")
                .build();

        loginButton = new TestLoginButton(activity, attributeSet, loginManager);
        loginButton.setCallback(loginCallback);

        loginButton.callOnClick();

        verify(loginManager).login(eq(activity));
    }

    @Test
    public void testButtonClickWithScopesRequestCodeFromXml_shouldUseParseAll() {
        AttributeSet attributeSet = Robolectric.buildAttributeSet()
                .addAttribute(R.attr.ub__scopes, "history|request_receipt")
                .addAttribute(R.attr.ub__request_code, String.valueOf(REQUEST_CODE))
                .build();

        loginButton = new TestLoginButton(activity, attributeSet, loginManager);
        loginButton.setCallback(loginCallback);

        loginButton.callOnClick();

        verify(loginManager).login(eq(activity));
    }

    @Test
    public void testButtonClickWithoutLoginManager_shouldCreateNew() {
        AttributeSet attributeSet = Robolectric.buildAttributeSet()
                .addAttribute(R.attr.ub__scopes, "history|request_receipt")
                .addAttribute(R.attr.ub__request_code, String.valueOf(REQUEST_CODE))
                .build();

        loginButton = new LoginButton(activity, attributeSet);
        loginButton.setSessionConfiguration(new SessionConfiguration.Builder()
                .setRedirectUri("com.example://redirect")
                .setClientId("clientId").build());
        loginButton.setCallback(loginCallback);
        loginButton.setScopes(SCOPES);
        loginButton.setAccessTokenStorage(accessTokenStorage);
        loginButton.callOnClick();

        assertThat(loginButton.getLoginManager()).isNotNull();
        assertThat(loginButton.getLoginManager().getAccessTokenStorage())
                .isEqualTo(accessTokenStorage);
    }

    @Test
    public void testOnActivityResult_shouldCascadeLoginManager() {
        loginButton = new TestLoginButton(activity, loginManager);
        loginButton.setCallback(loginCallback);

        final Intent intent = mock(Intent.class);

        loginButton.onActivityResult(LoginManager.REQUEST_CODE_LOGIN_DEFAULT, 1, intent);

        verify(loginManager).onActivityResult(eq(activity), eq(LoginManager.REQUEST_CODE_LOGIN_DEFAULT), eq(1),
                eq(intent));
    }

    @Test
    public void testOnActivityResultDifferentResultCode_shouldNotCallLoginManager() {
        loginButton = new TestLoginButton(activity, loginManager);
        loginButton.setCallback(loginCallback);

        final Intent intent = mock(Intent.class);

        loginButton.onActivityResult(REQUEST_CODE, 1, intent);

        verify(loginManager, never()).onActivityResult(eq(activity), anyInt(), anyInt(), any(Intent.class));

    }

    private static class TestLoginButton extends LoginButton {
        private LoginManager manager;

        public TestLoginButton(Context context, LoginManager loginManager) {
            super(context);
            this.manager = loginManager;
        }

        public TestLoginButton(Context context, AttributeSet attrs, LoginManager manager) {
            super(context, attrs);
            this.manager = manager;
        }

        @NonNull
        @Override
        protected LoginManager getOrCreateLoginManager() {
            return manager;
        }
    }
}