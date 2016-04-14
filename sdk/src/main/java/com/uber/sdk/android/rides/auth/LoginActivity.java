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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.uber.sdk.android.rides.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * {@link android.app.Activity} that shows web view for Uber user authentication and authorization.
 */
public class LoginActivity extends Activity implements LoginCallback {

    private LoginView mLoginView;

    /**
     * Create an {@link Intent} to pass to this activity
     *
     * @param context the {@link Context} for the intent
     * @param scopes the {@link Scope}s to request access for.
     * @return an intent that can be passed to this activity
     */
    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull Collection<Scope> scopes) {
        ArrayList<String> scopesList = new ArrayList<>(AuthUtils.scopeCollectionToStringSet(scopes));
        Intent data = new Intent(context, LoginActivity.class).putStringArrayListExtra(LoginManager.SCOPES_KEY, scopesList);

        return data;
    }

    @Override
    public void onLoginCancel() {
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }

    @Override
    public void onLoginError(@NonNull AuthenticationError error) {
        Intent data = new Intent();
        data.putExtra(LoginManager.LOGIN_ERROR_KEY, error);
        setResult(RESULT_CANCELED, data);
        finish();
    }

    @Override
    public void onLoginSuccess(@NonNull AccessToken accessToken) {
        Intent data = new Intent().putExtra(LoginManager.ACCESS_TOKEN_KEY, accessToken);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ub__login_activity);

        mLoginView = (LoginView) findViewById(R.id.ub__login_view);

        Intent intent = getIntent();
        if (getIntent().getStringArrayListExtra(LoginManager.SCOPES_KEY) == null) {
            onLoginError(AuthenticationError.INVALID_SCOPE);
            return;
        }

        Collection<Scope> scopes = AuthUtils.stringSetToScopeCollection(new HashSet<String>(intent
                        .getStringArrayListExtra(LoginManager.SCOPES_KEY)));
        mLoginView.setScopes(scopes);
        mLoginView.setLoginCallback(this);
        mLoginView.load();
    }
}
