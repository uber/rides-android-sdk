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
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.util.Pair;
import com.google.common.collect.Sets;
import com.uber.sdk.android.core.BuildConfig;
import com.uber.sdk.android.core.RobolectricTestBase;
import com.uber.sdk.android.core.SupportedAppType;
import com.uber.sdk.android.core.utils.AppProtocol;
import com.uber.sdk.core.auth.Scope;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.robolectric.Robolectric;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static com.uber.sdk.android.core.SupportedAppType.UBER;
import static com.uber.sdk.android.core.SupportedAppType.UBER_EATS;
import static com.uber.sdk.android.core.auth.SsoDeeplink.MIN_UBER_RIDES_VERSION_SUPPORTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SsoDeeplinkTest extends RobolectricTestBase {

    private static final String CLIENT_ID = "MYCLIENTID";
    private static final Set<Scope> GENERAL_SCOPES = Sets.newHashSet(Scope.HISTORY, Scope.PROFILE);
    private static final int REQUEST_CODE = 1234;

    private static final String DEFAULT_REGION =
            "uber://connect?client_id=MYCLIENTID&scope=profile%20history&sdk=android&sdk_version="
                    + BuildConfig.VERSION_NAME;

    @Mock
    AppProtocol appProtocol;

    Activity activity;

    @Before
    public void setUp() {
        activity = spy(Robolectric.setupActivity(Activity.class));
    }

    @Test
    public void testIsSupported_appInstalled_shouldBeTrue() {
        enableSupport(UBER, MIN_UBER_RIDES_VERSION_SUPPORTED);

        final SsoDeeplink link = new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .build();

        assertThat(link.isSupported()).isTrue();
    }

    @Test
    public void testIsSupported_eatsAppInstalled_shouldBeTrue() {
        enableSupport(UBER_EATS, SsoDeeplink.MIN_UBER_EATS_VERSION_SUPPORTED);

        final SsoDeeplink link = new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .build();

        assertThat(link.isSupported()).isTrue();
    }

    @Test
    public void testIsSupported_appInstalledWithBadSignature_shouldBeFalse() {
        enableSupport();
        when(appProtocol.validateSignature(any(Context.class), anyString())).thenReturn(false);

        final SsoDeeplink link = new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .build();

        final boolean isSupported = link.isSupported();

        assertThat(isSupported).isFalse();
    }

    @Test
    public void testIsSupported_appInstalledButOldVersion_shouldBeFalse() {
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.versionCode = 0;
        when(appProtocol.getInstalledUberAppPackage(activity)).thenReturn(packageInfo);
        when(appProtocol.getInstalledUberApp(activity)).thenReturn(Pair.create(UBER, packageInfo));
        when(appProtocol.validateSignature(any(Context.class), anyString())).thenReturn(true);
        when(appProtocol.validateMinimumVersion(eq(activity), any(PackageInfo.class), eq(0))).thenReturn(false);

        SsoDeeplink link = new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .build();

        assertThat(link.isSupported()).isFalse();
    }

    @Test
    public void testIsSupported_noAppInstalled_shouldBeFalse() {
        when(appProtocol.getInstalledUberApp(activity)).thenReturn(null);

        SsoDeeplink link = new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .build();

        assertThat(link.isSupported()).isFalse();
    }

    @Test
    public void testInvokeWithoutRegion_shouldUseWorld() {
        enableSupport();

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(Scope.HISTORY, Scope.PROFILE)
                .activityRequestCode(REQUEST_CODE)
                .appProtocol(appProtocol)
                .build()
                .execute();

        final ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        final ArgumentCaptor<Integer> requestCodeCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(activity).startActivityForResult(intentCaptor.capture(), requestCodeCaptor.capture());

        final Uri uri = intentCaptor.getValue().getData();

        assertThat(uri.toString()).isEqualTo(DEFAULT_REGION);
        assertThat(requestCodeCaptor.getValue()).isEqualTo(REQUEST_CODE);
    }

    @Test
    public void testInvokeWithoutRequestCode_shouldUseDefaultRequstCode() {
        enableSupport();

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .build()
                .execute();

        final ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        final ArgumentCaptor<Integer> requestCodeCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(activity).startActivityForResult(intentCaptor.capture(), requestCodeCaptor.capture());

        Uri uri = intentCaptor.getValue().getData();

        assertThat(uri.toString()).isEqualTo(DEFAULT_REGION);
        assertThat(requestCodeCaptor.getValue()).isEqualTo(LoginManager.REQUEST_CODE_LOGIN_DEFAULT);
    }


    @Test(expected = IllegalStateException.class)
    public void testInvokeWithoutScopes_shouldFail() {
        enableSupport();

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .activityRequestCode(REQUEST_CODE)
                .appProtocol(appProtocol)
                .build()
                .execute();
    }

    @Test
    public void testInvokeWithScopesAndCustomScopes_shouldSucceed() {
        enableSupport();

        Collection<String> collection = Arrays.asList("sample", "test");

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .activityRequestCode(REQUEST_CODE)
                .scopes(GENERAL_SCOPES)
                .customScopes(collection)
                .appProtocol(appProtocol)
                .build()
                .execute();

        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(activity).startActivityForResult(intentArgumentCaptor.capture(), anyInt());

        Uri uri = intentArgumentCaptor.getValue().getData();
        assertThat(uri.getQueryParameter("scope")).contains("history", "profile", "sample", "test");
    }

    @Test(expected = NullPointerException.class)
    public void testInvokeWithoutClientId_shouldFail() {
        enableSupport();

        new SsoDeeplink.Builder(activity)
                .scopes(GENERAL_SCOPES)
                .activityRequestCode(REQUEST_CODE)
                .appProtocol(appProtocol)
                .build()
                .execute();
    }

    @Test(expected = IllegalStateException.class)
    public void testInvokeWithoutAppInstalled_shouldFail() {
        when(appProtocol.getInstalledUberApp(activity)).thenReturn(null);

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .build()
                .execute();
    }

    private void enableSupport() {
        enableSupport(UBER, MIN_UBER_RIDES_VERSION_SUPPORTED);
    }

    private void enableSupport(SupportedAppType appType, int versionCode) {
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.versionCode = versionCode;
        when(appProtocol.getInstalledUberAppPackage(activity)).thenReturn(packageInfo);
        when(appProtocol.getInstalledUberApp(activity)).thenReturn(Pair.create(appType, packageInfo));
        when(appProtocol.validateSignature(any(Context.class), anyString())).thenReturn(true);
        when(appProtocol.validateMinimumVersion(eq(activity), any(PackageInfo.class), eq(versionCode))).thenReturn(true);
    }
}
