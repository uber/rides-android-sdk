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
import android.content.pm.PackageInfo;
import android.net.Uri;
import com.google.common.collect.ImmutableList;
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
import java.util.Collections;
import java.util.Set;

import static com.uber.sdk.android.core.SupportedAppType.UBER;
import static com.uber.sdk.android.core.SupportedAppType.UBER_EATS;
import static com.uber.sdk.android.core.auth.SsoDeeplink.MIN_UBER_EATS_VERSION_SUPPORTED;
import static com.uber.sdk.android.core.auth.SsoDeeplink.MIN_UBER_RIDES_VERSION_SUPPORTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
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
    public void isSupported_appInstalled_shouldBeTrue() {
        when(appProtocol.isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED)).thenReturn(true);
        when(appProtocol.isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED)).thenReturn(false);

        final SsoDeeplink link = new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .build();

        assertThat(link.isSupported()).isTrue();
    }

    @Test
    public void isSupported_eatsAppInstalled_withoutProductPriority_shouldBeFalse() {
        when(appProtocol.isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED)).thenReturn(false);
        when(appProtocol.isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED)).thenReturn(true);

        final SsoDeeplink link = new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .build();

        assertThat(link.isSupported()).isFalse();
    }

    @Test
    public void isSupported_eatsAppInstalled_withProductPriority_shouldBeTrue() {
        when(appProtocol.isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED)).thenReturn(false);
        when(appProtocol.isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED)).thenReturn(true);

        final SsoDeeplink link = new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .productFlowPriority(ImmutableList.of(UBER_EATS))
                .build();

        assertThat(link.isSupported()).isTrue();
    }

    @Test
    public void isSupported_appNotInstalled_shouldBeFalse() {
        when(appProtocol.isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED)).thenReturn(false);
        when(appProtocol.isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED)).thenReturn(false);

        final SsoDeeplink link = new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .build();

        assertThat(link.isSupported()).isFalse();
    }

    @Test
    public void execute_withInstalledPackage_shouldSetPackage() {
        String packageName = "PACKAGE_NAME";
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.packageName = packageName;

        when(appProtocol.isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED)).thenReturn(true);
        when(appProtocol.getInstalledPackages(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED))
                .thenReturn(Collections.singletonList(packageInfo));

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(Scope.HISTORY, Scope.PROFILE)
                .activityRequestCode(REQUEST_CODE)
                .appProtocol(appProtocol)
                .build()
                .execute();

        final ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(activity).startActivityForResult(intentCaptor.capture(), eq(REQUEST_CODE));
        Intent intent = intentCaptor.getValue();

        assertThat(intent.getPackage()).isEqualTo(packageName);
    }

    @Test
    public void execute_withoutRegion_shouldUseWorld() {
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
    public void execute_withoutRequestCode_shouldUseDefaultRequstCode() {
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
    public void execute_withoutScopes_shouldFail() {
        enableSupport();

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .activityRequestCode(REQUEST_CODE)
                .appProtocol(appProtocol)
                .build()
                .execute();
    }

    @Test
    public void execute_withScopesAndCustomScopes_shouldSucceed() {
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

    @Test
    public void execute_withEatsProductFlowPriority_shouldLaunchEats() {
        enableSupport();
        String eatsPackageName = "com.ubercab.eats";
        PackageInfo eatsPackageInfo = new PackageInfo();
        eatsPackageInfo.packageName = eatsPackageName;
        when(appProtocol.getInstalledPackages(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED))
                .thenReturn(Collections.singletonList(eatsPackageInfo));

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .activityRequestCode(REQUEST_CODE)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .productFlowPriority(ImmutableList.of(UBER_EATS))
                .build()
                .execute();

        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(activity).startActivityForResult(intentArgumentCaptor.capture(), anyInt());

        assertThat(intentArgumentCaptor.getValue().getPackage()).isEqualTo(eatsPackageName);
    }

    @Test
    public void execute_withMissingProductFlowPriority_shouldLaunchRides() {
        enableSupport();
        String ridesPackageName = "com.ubercab";
        PackageInfo ridesPackageInfo = new PackageInfo();
        ridesPackageInfo.packageName = ridesPackageName;
        when(appProtocol.getInstalledPackages(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED))
                .thenReturn(Collections.singletonList(ridesPackageInfo));

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .activityRequestCode(REQUEST_CODE)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .productFlowPriority(ImmutableList.<SupportedAppType>of())
                .build()
                .execute();

        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(activity).startActivityForResult(intentArgumentCaptor.capture(), anyInt());

        assertThat(intentArgumentCaptor.getValue().getPackage()).isEqualTo(ridesPackageName);
    }

    @Test(expected = NullPointerException.class)
    public void execute_withoutClientId_shouldFail() {
        enableSupport();

        new SsoDeeplink.Builder(activity)
                .scopes(GENERAL_SCOPES)
                .activityRequestCode(REQUEST_CODE)
                .appProtocol(appProtocol)
                .build()
                .execute();
    }

    @Test(expected = IllegalStateException.class)
    public void execute_withoutAppInstalled_shouldFail() {
        when(appProtocol.isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED)).thenReturn(false);
        when(appProtocol.isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED)).thenReturn(false);

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .build()
                .execute();
    }

    private void enableSupport() {
        when(appProtocol.isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED)).thenReturn(true);
        when(appProtocol.isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED)).thenReturn(true);
    }
}
