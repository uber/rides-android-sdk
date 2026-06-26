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
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.annotation.VisibleForTesting;
import android.util.AttributeSet;
import android.view.View;

import com.uber.sdk.android.core.R;
import com.uber.sdk.android.core.UberButton;
import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.android.core.UberStyle;
import com.uber.sdk.core.auth.AccessTokenStorage;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.core.client.SessionConfiguration;

import java.util.Collection;

import static com.uber.sdk.android.core.utils.Preconditions.checkNotNull;

/**
 * The {@link LoginButton} is used to initiate the Uber SDK Login flow.
 */
public class LoginButton extends UberButton {
    private static final
    @StyleRes
    int[] STYLES = {R.style.UberButton_Login,
            R.style.UberButton_Login_White};

    private AccessTokenStorage accessTokenStorage;
    private SessionConfiguration sessionConfiguration;
    private LoginManager loginManager;
    private LoginCallback callback;
    private Collection<Scope> scopes;
    private int requestCode = LoginManager.REQUEST_CODE_LOGIN_DEFAULT;

    public LoginButton(Context context) {
        super(context);
    }

    public LoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoginButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LoginButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init(
            @NonNull Context context,
            @StringRes int defaultText,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            @NonNull UberStyle uberStyle) {
        setAllCaps(true);

        int defStyleRes = STYLES[uberStyle.getValue()];

        applyStyle(context, R.string.ub__sign_in, attrs, defStyleAttr, defStyleRes);

        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.LoginButton, 0, 0);

        setRequestCodeFromXml(typedArray);
        setScopesFromXml(typedArray);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    @VisibleForTesting
    void login() {
        final Activity activity = getActivity();

        checkNotNull(callback, "Callback has not been set, call setCallback to assign value.");
        if ((scopes == null || scopes.isEmpty()) && (sessionConfiguration.getScopes() == null ||
                sessionConfiguration.getScopes().isEmpty())) {
            throw new IllegalStateException("Scopes are not yet set.");
        }

        getOrCreateLoginManager().login(activity);
    }

    /**
     * A {@link SessionConfiguration} is required to identify the app being authenticated.
     *
     * @param sessionConfiguration to be identified.
     * @return this instance of {@link LoginButton}
     */
    public LoginButton setSessionConfiguration(@NonNull SessionConfiguration sessionConfiguration) {
        this.sessionConfiguration = sessionConfiguration;
        return this;
    }

    /**
     * Provide {@link LoginCallback}
     *
     * @param callback to be used to notify Success or Failure
     * @return this instance of {@link LoginButton}
     */
    public LoginButton setCallback(@NonNull LoginCallback callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Provide {@link Scope}s requested, not providing this will result in a {@link IllegalStateException} during login.
     *
     * @param scopes
     * @return this instance of {@link LoginButton}
     */
    public LoginButton setScopes(@NonNull Collection<Scope> scopes) {
        this.scopes = scopes;
        return this;
    }

    /**
     * Optionally provide a request code to be used when {@link Activity#onActivityResult(int, int, Intent)} is called,
     * otherwise {@link LoginManager#REQUEST_CODE_LOGIN_DEFAULT} will be used.
     *
     * @param requestCode
     * @return this instance of {@link LoginButton}
     */
    public LoginButton setRequestCode(int requestCode) {
        this.requestCode = requestCode;
        return this;
    }

    /**
     * Optionally provide {@link AccessTokenStorage}. If none provided, one will be created.
     *
     * @param accessTokenStorage
     * @return this instance of {@link LoginButton}
     */
    public LoginButton setAccessTokenStorage(AccessTokenStorage accessTokenStorage) {
        this.accessTokenStorage = accessTokenStorage;
        return this;
    }

    /**
     * Optionally provide {@link AccessTokenStorage}. If none provided, one will be created.
     *
     * @param accessTokenStorage
     * @return this instance of {@link LoginButton}
     * @deprecated use {@link LoginButton#setAccessTokenStorage(AccessTokenStorage)}
     */
    @Deprecated
    public LoginButton setAccessTokenManager(AccessTokenStorage accessTokenStorage) {
        this.accessTokenStorage = accessTokenStorage;
        return this;
    }

    /**
     * @return currently assigned {@link LoginManager}.
     * If none has been set, one will be created when login is invoked.
     */
    @Nullable
    public LoginManager getLoginManager() {
        return loginManager;
    }

    /**
     * @return currently assigned {@link LoginCallback}.
     */
    @Nullable
    public LoginCallback getCallback() {
        return callback;
    }

    /**
     * @return currently assigned {@link Scope}s
     */
    @Nullable
    public Collection<Scope> getScopes() {
        return scopes;
    }

    /**
     * @return request code to be used when {@link Activity#onActivityResult(int, int, Intent)} is called. Default
     * value is {@link LoginManager#REQUEST_CODE_LOGIN_DEFAULT}.
     */
    public int getRequestCode() {
        return requestCode;
    }

    /**
     * {@link Activity} result handler to be called from starting {@link Activity}. Stores
     * {@link com.uber.sdk.core.auth.AccessToken} and notifies consumer callback of login result.
     *
     * @param requestCode request code originally supplied to {@link Activity#startActivityForResult(Intent, int)}.
     * @param resultCode  result code from returning {@link Activity}.
     * @param data        data from returning {@link Activity}.
     */
    public void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data) {
        if (requestCode == this.requestCode) {
            getOrCreateLoginManager().onActivityResult(getActivity(), requestCode, resultCode, data);
        }
    }

    @NonNull
    @VisibleForTesting
    protected synchronized LoginManager getOrCreateLoginManager() {
        if (loginManager == null) {
            loginManager = new LoginManager(getOrCreateAccessTokenStorage(),
                    callback,
                    getOrCreateSessionConfiguration(),
                    requestCode);
        }
        return loginManager;
    }

    @NonNull
    protected synchronized AccessTokenStorage getOrCreateAccessTokenStorage() {
        if (accessTokenStorage == null) {
            accessTokenStorage = new AccessTokenManager(getContext());
        }
        return accessTokenStorage;
    }

    @NonNull
    protected synchronized SessionConfiguration getOrCreateSessionConfiguration() {
        if (sessionConfiguration == null) {
            sessionConfiguration = UberSdk.getDefaultSessionConfiguration();
        }

        if (scopes != null) {
            sessionConfiguration = sessionConfiguration.newBuilder()
                    .setScopes(scopes)
                    .build();
        }

        return sessionConfiguration;
    }

    private void setRequestCodeFromXml(TypedArray typedArray) {
        final int requestCode = typedArray.getInt(R.styleable.LoginButton_ub__request_code,
                LoginManager.REQUEST_CODE_LOGIN_DEFAULT);

        this.requestCode = requestCode;
    }

    private void setScopesFromXml(TypedArray typedArray) {
        final int scopes = typedArray.getInt(R.styleable.LoginButton_ub__scopes, 0);
        if (scopes > 0) {
            this.scopes = Scope.parseScopes(scopes);
        }
    }

}
