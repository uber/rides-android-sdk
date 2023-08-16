/*
 * Copyright (c) 2023 Uber Technologies, Inc.
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

package com.uber.sdk.android.samples;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.uber.sdk.android.rides.samples.BuildConfig;
import com.uber.sdk.android.rides.samples.R;
import com.uber.sdk.android.samples.auth.AuthUriAssembler;
import com.uber.sdk.android.samples.auth.PkceUtil;
import com.uber.sdk.android.samples.model.AccessToken;
import com.uber.sdk.android.samples.network.AuthorizationCodeGrantFlow;
import com.uber.sdk.android.samples.network.TokenRequestFlowCallback;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;


public class DemoActivity extends AppCompatActivity {
    private static final String LOG_TAG = DemoActivity.class.getSimpleName();
    public static final String CLIENT_ID = BuildConfig.CLIENT_ID;
    public static final String REDIRECT_URI = BuildConfig.REDIRECT_URI;

    public static final String BASE_URL = "https://auth.uber.com";
    private static final String ACCESS_TOKEN_SHARED_PREFERENCES = ".demoActivityStorage";
    private static final String ACCESS_TOKEN = ".access_token";
    private static final String EXTRA_CODE_RECEIVED = "CODE_RECEIVED";
    private static final int CUSTOM_BUTTON_REQUEST_CODE = 1111;
    private static final String CODE_VERIFIER = PkceUtil.generateCodeVerifier();

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        sharedPreferences = getApplicationContext()
                .getSharedPreferences(ACCESS_TOKEN_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        Button appLinkButton = findViewById(R.id.applink_uber_button);
        appLinkButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String codeChallenge;
            try {
                codeChallenge = PkceUtil.generateCodeChallange(CODE_VERIFIER);
            } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            intent.setData(
                    AuthUriAssembler.assemble(
                                    CLIENT_ID,
                                    "profile",
                                    "code",
                                    codeChallenge,
                                    REDIRECT_URI
                            )
            );
            startActivityForResult(intent, CUSTOM_BUTTON_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(LOG_TAG, String.format("onActivityResult requestCode:[%s] resultCode [%s]",
                requestCode, resultCode));
        if (data != null) {
            final String authorizationCode = data.getStringExtra(EXTRA_CODE_RECEIVED);
            if (authorizationCode != null) {
                handleAuthCode(authorizationCode);
            }
        }
    }

    private void handleAuthCode(String authCode) {
        new AuthorizationCodeGrantFlow(
                BASE_URL,
                CLIENT_ID,
                REDIRECT_URI,
                authCode,
                CODE_VERIFIER
        ).execute(new TokenRequestFlowCallback() {
            @Override
            public void onSuccess(AccessToken accessToken) {
                sharedPreferences.edit()
                        .putString(ACCESS_TOKEN, accessToken.getToken())
                        .apply();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Toast.makeText(
                        DemoActivity.this,
                        getString(R.string.authorization_code_error_message, throwable.getMessage()),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.action_clear) {
            sharedPreferences.edit().clear().apply();
            Toast.makeText(this, "AccessToken cleared", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_copy) {
            String accessToken = sharedPreferences.getString(ACCESS_TOKEN, "");

            String message = accessToken.isEmpty() ? "No AccessToken stored" : "AccessToken copied to clipboard";
            if (!accessToken.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("UberDemoAccessToken", accessToken);
                clipboard.setPrimaryClip(clip);
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}
