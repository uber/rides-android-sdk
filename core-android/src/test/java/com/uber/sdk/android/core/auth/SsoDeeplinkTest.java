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
import android.content.pm.ResolveInfo;
import android.net.Uri;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.uber.sdk.android.core.BuildConfig;
import com.uber.sdk.android.core.RobolectricTestBase;
import com.uber.sdk.android.core.utils.AppProtocol;
import com.uber.sdk.core.auth.Scope;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.res.builder.RobolectricPackageManager;
import org.robolectric.shadows.ShadowResolveInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static com.uber.sdk.android.core.SupportedAppType.UBER;
import static com.uber.sdk.android.core.SupportedAppType.UBER_EATS;
import static com.uber.sdk.android.core.auth.SsoDeeplink.FlowVersion.DEFAULT;
import static com.uber.sdk.android.core.auth.SsoDeeplink.FlowVersion.REDIRECT_TO_SDK;
import static com.uber.sdk.android.core.auth.SsoDeeplink.MIN_UBER_EATS_VERSION_SUPPORTED;
import static com.uber.sdk.android.core.auth.SsoDeeplink.MIN_UBER_RIDES_VERSION_REDIRECT_FLOW_SUPPORTED;
import static com.uber.sdk.android.core.auth.SsoDeeplink.MIN_UBER_RIDES_VERSION_SUPPORTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SsoDeeplinkTest extends RobolectricTestBase {

    private static final String CLIENT_ID = "MYCLIENTID";
    private static final Set<Scope> GENERAL_SCOPES = Sets.newHashSet(Scope.HISTORY, Scope.PROFILE);
    private static final int REQUEST_CODE = 1234;
    private static final String REDIRECT_URI = "com.example.app://redirect";

    private static final String DEFAULT_URI =
            "uber://connect?client_id=MYCLIENTID&scope=profile%20history&sdk=android&flow_type=DEFAULT"
                    + "&redirect_uri=com.example.app%3A%2F%2Fredirect&sdk_version="
                    + BuildConfig.VERSION_NAME;
    @Mock
    AppProtocol appProtocol;

    Activity activity;

    RobolectricPackageManager packageManager;

    ResolveInfo resolveInfo;

    Intent redirectIntent;

    SsoDeeplink ssoDeeplink;

    @Before
    public void setUp() {
        activity = spy(Robolectric.setupActivity(Activity.class));

        redirectIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(REDIRECT_URI));
        redirectIntent.setPackage(activity.getPackageName());
        resolveInfo = ShadowResolveInfo.newResolveInfo("", activity.getPackageName());
        packageManager = RuntimeEnvironment.getRobolectricPackageManager();
        packageManager.addResolveInfoForIntent(redirectIntent, resolveInfo);

        ssoDeeplink = new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .activityRequestCode(REQUEST_CODE)
                .redirectUri(REDIRECT_URI)
                .build();
    }

    @Test
    public void isSupported_ridesNotInstalled_withoutProductPriority_shouldBeFalse() {
        when(appProtocol.isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED)).thenReturn(false);

        assertThat(ssoDeeplink.isSupported()).isFalse();

        verify(appProtocol).isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED);
        verify(appProtocol, never()).isInstalled(any(Context.class), eq(UBER_EATS), anyInt());
    }

    @Test
    public void isSupported_ridesNotInstalled_withoutProductPriority_andRedirectToSdkFlowVersion_shouldBeFalse() {
        when(appProtocol.isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_REDIRECT_FLOW_SUPPORTED)).thenReturn(false);

        assertThat(ssoDeeplink.isSupported(REDIRECT_TO_SDK)).isFalse();

        verify(appProtocol).isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_REDIRECT_FLOW_SUPPORTED);
        verify(appProtocol, never()).isInstalled(any(Context.class), eq(UBER_EATS), anyInt());
    }

    @Test
    public void isSupported_ridesInstalled_withoutProductPriority_shouldBeTrue() {
        enableSupport(DEFAULT);

        assertThat(ssoDeeplink.isSupported()).isTrue();

        verify(appProtocol).isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED);
        verify(appProtocol, never()).isInstalled(any(Context.class), eq(UBER_EATS), anyInt());
    }

    @Test
    public void isSupported_ridesInstalled_withoutProductPriority_andRedirectToSdkFlowVersion_shouldBeTrue() {
        enableSupport(REDIRECT_TO_SDK);

        assertThat(ssoDeeplink.isSupported(REDIRECT_TO_SDK)).isTrue();

        verify(appProtocol).isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_REDIRECT_FLOW_SUPPORTED);
        verify(appProtocol, never()).isInstalled(any(Context.class), eq(UBER_EATS), anyInt());
    }

    @Test
    public void isSupported_eatsNotInstalled_withEatsProductPriority_shouldBeFalse() {
        when(appProtocol.isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED)).thenReturn(false);

        ssoDeeplink = new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .activityRequestCode(REQUEST_CODE)
                .redirectUri(REDIRECT_URI)
                .productFlowPriority(ImmutableList.of(UBER_EATS))
                .build();

        assertThat(ssoDeeplink.isSupported()).isFalse();
        assertThat(ssoDeeplink.isSupported(REDIRECT_TO_SDK)).isFalse();

        verify(appProtocol, times(2)).isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED);
        verify(appProtocol, never()).isInstalled(any(Context.class), eq(UBER), anyInt());
    }

    @Test
    public void isSupported_eatsInstalled_withEatsProductPriority_shouldBeTrue() {
        when(appProtocol.isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED)).thenReturn(true);

        ssoDeeplink = new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .activityRequestCode(REQUEST_CODE)
                .redirectUri(REDIRECT_URI)
                .productFlowPriority(ImmutableList.of(UBER_EATS))
                .build();

        assertThat(ssoDeeplink.isSupported()).isTrue();
        assertThat(ssoDeeplink.isSupported(REDIRECT_TO_SDK)).isTrue();

        verify(appProtocol, times(2)).isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED);
        verify(appProtocol, never()).isInstalled(any(Context.class), eq(UBER), anyInt());
    }

    @Test
    public void isSupported_noneInstalled_withCombinedProductPriority_shouldBeFalse() {
        when(appProtocol.isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED)).thenReturn(false);
        when(appProtocol.isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED)).thenReturn(false);

        ssoDeeplink = new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .activityRequestCode(REQUEST_CODE)
                .redirectUri(REDIRECT_URI)
                .productFlowPriority(ImmutableList.of(UBER, UBER_EATS))
                .build();

        assertThat(ssoDeeplink.isSupported()).isFalse();

        InOrder orderVerifier = inOrder(appProtocol);
        orderVerifier.verify(appProtocol).isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED);
        orderVerifier.verify(appProtocol).isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED);
    }

    @Test
    public void isSupported_noneInstalled_withCombinedProductPriority_andRedirectToSdkFlowVersion_shouldBeFalse() {
        when(appProtocol.isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_REDIRECT_FLOW_SUPPORTED)).thenReturn(false);
        when(appProtocol.isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED)).thenReturn(false);

        ssoDeeplink = new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .activityRequestCode(REQUEST_CODE)
                .redirectUri(REDIRECT_URI)
                .productFlowPriority(ImmutableList.of(UBER, UBER_EATS))
                .build();

        assertThat(ssoDeeplink.isSupported(REDIRECT_TO_SDK)).isFalse();

        InOrder orderVerifier = inOrder(appProtocol);
        orderVerifier.verify(appProtocol).isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_REDIRECT_FLOW_SUPPORTED);
        orderVerifier.verify(appProtocol).isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED);
    }

    @Test
    public void isSupported_bothAppsInstalled_withCombinedProductPriority_shouldBeTrue() {
        enableSupport(DEFAULT);

        ssoDeeplink = new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .activityRequestCode(REQUEST_CODE)
                .redirectUri(REDIRECT_URI)
                .productFlowPriority(ImmutableList.of(UBER, UBER_EATS))
                .build();

        assertThat(ssoDeeplink.isSupported()).isTrue();

        verify(appProtocol).isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED);
        verify(appProtocol, never()).isInstalled(any(Context.class), eq(UBER_EATS), anyInt());
    }

    @Test
    public void isSupported_bothAppsInstalled_withCombinedProductPriority_andRedirectToSdkFlowVersion_shouldBeTrue() {
        enableSupport(REDIRECT_TO_SDK);

        ssoDeeplink = new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .activityRequestCode(REQUEST_CODE)
                .redirectUri(REDIRECT_URI)
                .productFlowPriority(ImmutableList.of(UBER, UBER_EATS))
                .build();

        assertThat(ssoDeeplink.isSupported(REDIRECT_TO_SDK)).isTrue();

        verify(appProtocol).isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_REDIRECT_FLOW_SUPPORTED);
        verify(appProtocol, never()).isInstalled(any(Context.class), eq(UBER_EATS), anyInt());
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
    public void isSupported_withRidesAppInstalled_andDefaultFlowVersion_andAboveMinDefaultFlowVersion_shouldBeTrue() {
        when(appProtocol.isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED)).thenReturn(true);
        when(appProtocol.isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED)).thenReturn(false);

        assertThat(ssoDeeplink.isSupported()).isTrue();

        verify(appProtocol).isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED);
    }

    @Test
    public void isSupported_withRedirectToSdkFlowVersion_andCantResolveRedirectIntent_shouldBeFalse() {
        enableSupport(REDIRECT_TO_SDK);
        packageManager.removeResolveInfosForIntent(redirectIntent, activity.getPackageName());

        assertThat(ssoDeeplink.isSupported(REDIRECT_TO_SDK)).isFalse();

        verify(appProtocol, never()).isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_REDIRECT_FLOW_SUPPORTED);
        verify(appProtocol, never()).isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED);
    }

    @Test
    public void execute_withRidesInstalled_andDefaultFlow_andNoProductPriority_shouldSetPackageAndStartActivityForResult() {
        enableSupport(DEFAULT);

        String packageName = "PACKAGE_NAME";
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.packageName = packageName;

        when(appProtocol.getInstalledPackages(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED))
                .thenReturn(Collections.singletonList(packageInfo));

        ssoDeeplink.execute();

        verify(appProtocol).getInstalledPackages(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED);
        verify(appProtocol, never()).getInstalledPackages(any(Context.class), eq(UBER_EATS), anyInt());

        final ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(activity).startActivityForResult(intentCaptor.capture(), eq(REQUEST_CODE));
        Intent intent = intentCaptor.getValue();

        assertThat(intent.getPackage()).isEqualTo(packageName);
        assertThat(intent.getData().toString()).isEqualTo(DEFAULT_URI);
    }

    @Test
    public void execute_withRidesInstalled_andRedirectToSdkFlow_andNoProductPriority_shouldSetPackageAndStartActivity() {
        enableSupport(REDIRECT_TO_SDK);

        String packageName = "PACKAGE_NAME";
        PackageInfo packageInfo = new PackageInfo();
        packageInfo.packageName = packageName;

        when(appProtocol.getInstalledPackages(activity, UBER, MIN_UBER_RIDES_VERSION_REDIRECT_FLOW_SUPPORTED))
                .thenReturn(Collections.singletonList(packageInfo));

        ssoDeeplink.execute(REDIRECT_TO_SDK);
        verify(appProtocol).getInstalledPackages(activity, UBER, MIN_UBER_RIDES_VERSION_REDIRECT_FLOW_SUPPORTED);
        verify(appProtocol, never()).getInstalledPackages(any(Context.class), eq(UBER_EATS), anyInt());

        final ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(activity).startActivity(intentCaptor.capture());
        Intent intent = intentCaptor.getValue();

        String expectedUri =
                "uber://connect?client_id=MYCLIENTID&scope=profile%20history&sdk=android&flow_type=REDIRECT_TO_SDK"
                        + "&redirect_uri=com.example.app%3A%2F%2Fredirect&sdk_version="
                        + BuildConfig.VERSION_NAME;

        assertThat(intent.getData().toString()).isEqualTo(expectedUri);
        assertThat(intent.getPackage()).isEqualTo(packageName);
    }

    @Test
    public void execute_withEatsProductFlowPriority_shouldLaunchEats() {
        enableSupport(DEFAULT);

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
    public void execute_withCombinedProductFlowPriority_andBothAppsInstalled_shouldLaunchFirstPriorityApp() {
        enableSupport(DEFAULT);

        String eatsPackageName = "com.ubercab.eats";
        PackageInfo eatsPackageInfo = new PackageInfo();
        eatsPackageInfo.packageName = eatsPackageName;

        String ridesPackageName = "com.ubercab";
        PackageInfo ridesPackageInfo = new PackageInfo();
        ridesPackageInfo.packageName = ridesPackageName;

        when(appProtocol.getInstalledPackages(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED))
                .thenReturn(Collections.singletonList(eatsPackageInfo));
        when(appProtocol.getInstalledPackages(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED))
                .thenReturn(Collections.singletonList(ridesPackageInfo));

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .activityRequestCode(REQUEST_CODE)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .productFlowPriority(ImmutableList.of(UBER, UBER_EATS))
                .build()
                .execute();

        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(activity).startActivityForResult(intentArgumentCaptor.capture(), anyInt());

        assertThat(intentArgumentCaptor.getValue().getPackage()).isEqualTo(ridesPackageName);
    }

    @Test
    public void execute_withoutRequestCode_shouldUseDefaultRequestCode() {
        enableSupport(DEFAULT);

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .redirectUri(REDIRECT_URI)
                .build()
                .execute();

        final ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
        final ArgumentCaptor<Integer> requestCodeCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(activity).startActivityForResult(intentCaptor.capture(), requestCodeCaptor.capture());

        Uri uri = intentCaptor.getValue().getData();

        assertThat(uri.toString()).isEqualTo(DEFAULT_URI);
        assertThat(requestCodeCaptor.getValue()).isEqualTo(LoginManager.REQUEST_CODE_LOGIN_DEFAULT);
    }

    @Test
    public void execute_withScopesAndCustomScopes_shouldSucceed() {
        enableSupport(DEFAULT);

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .activityRequestCode(REQUEST_CODE)
                .scopes(GENERAL_SCOPES)
                .customScopes(Arrays.asList("sample", "test"))
                .appProtocol(appProtocol)
                .build()
                .execute();

        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(activity).startActivityForResult(intentArgumentCaptor.capture(), anyInt());

        Uri uri = intentArgumentCaptor.getValue().getData();
        assertThat(uri.getQueryParameter("scope")).contains("history", "profile", "sample", "test");
    }

    @Test
    public void execute_withOnlyCustomScopes_shouldSucceed() {
        enableSupport(DEFAULT);

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .activityRequestCode(REQUEST_CODE)
                .scopes(Collections.<Scope>emptyList())
                .customScopes(Arrays.asList("sample", "test"))
                .appProtocol(appProtocol)
                .build()
                .execute();

        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(activity).startActivityForResult(intentArgumentCaptor.capture(), anyInt());

        Uri uri = intentArgumentCaptor.getValue().getData();
        assertThat(uri.getQueryParameter("scope")).contains("sample", "test");
    }

    @Test
    public void execute_withoutRedirectUri_shouldUseDefaultUri() {
        enableSupport(REDIRECT_TO_SDK);
        packageManager.removeResolveInfosForIntent(redirectIntent, activity.getPackageName());
        String expectedRedirectUri = activity.getPackageName().concat(".uberauth://redirect");
        Intent expectedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(expectedRedirectUri));
        expectedIntent.setPackage(activity.getPackageName());
        packageManager.addResolveInfoForIntent(expectedIntent, resolveInfo);

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .appProtocol(appProtocol)
                .build()
                .execute(REDIRECT_TO_SDK);

        ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
        verify(activity).startActivity(intentArgumentCaptor.capture());

        Uri uri = intentArgumentCaptor.getValue().getData();
        assertThat(uri.getQueryParameter("redirect_uri")).isEqualTo(expectedRedirectUri);
    }

    @Test(expected = IllegalStateException.class)
    public void execute_withoutAnyScopes_shouldFail() {
        enableSupport(DEFAULT);

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(Collections.<Scope>emptyList())
                .customScopes(Collections.<String>emptyList())
                .activityRequestCode(REQUEST_CODE)
                .appProtocol(appProtocol)
                .build()
                .execute();
    }

    @Test(expected = NullPointerException.class)
    public void execute_withoutClientId_shouldFail() {
        enableSupport(DEFAULT);

        new SsoDeeplink.Builder(activity)
                .scopes(GENERAL_SCOPES)
                .activityRequestCode(REQUEST_CODE)
                .appProtocol(appProtocol)
                .build()
                .execute();
    }

    @Test(expected = IllegalStateException.class)
    public void execute_withRidesBelowMinVersion_noProductPriority_shouldFail() {
        when(appProtocol.isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED)).thenReturn(false);

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .build()
                .execute();
    }

    @Test(expected = IllegalStateException.class)
    public void execute_withBothAppsBelowMinVersion_andCombinedProductPriority_shouldFail() {
        when(appProtocol.isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_SUPPORTED)).thenReturn(false);
        when(appProtocol.isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED)).thenReturn(false);

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .productFlowPriority(ImmutableList.of(UBER, UBER_EATS))
                .build()
                .execute();
    }

    @Test(expected = IllegalStateException.class)
    public void execute_withBothAppsBelowMinRedirectToSdkVersion_andCombinedProductPriority_shouldFail() {
        when(appProtocol.isInstalled(activity, UBER, MIN_UBER_RIDES_VERSION_REDIRECT_FLOW_SUPPORTED)).thenReturn(false);
        when(appProtocol.isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED)).thenReturn(false);

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .productFlowPriority(ImmutableList.of(UBER, UBER_EATS))
                .build()
                .execute(REDIRECT_TO_SDK);
    }

    @Test(expected = IllegalStateException.class)
    public void execute_withRedirectToSdkFlowVersion_andCantResolveRedirectIntent_shouldFail() {
        enableSupport(REDIRECT_TO_SDK);
        packageManager.removeResolveInfosForIntent(redirectIntent, activity.getPackageName());

        new SsoDeeplink.Builder(activity)
                .clientId(CLIENT_ID)
                .scopes(GENERAL_SCOPES)
                .build()
                .execute(REDIRECT_TO_SDK);
    }

    private void enableSupport(SsoDeeplink.FlowVersion flowVersion) {
        int ridesMinVersion = flowVersion == REDIRECT_TO_SDK
                ? MIN_UBER_RIDES_VERSION_REDIRECT_FLOW_SUPPORTED
                : MIN_UBER_RIDES_VERSION_SUPPORTED;
        when(appProtocol.isInstalled(activity, UBER, ridesMinVersion)).thenReturn(true);
        when(appProtocol.isInstalled(activity, UBER_EATS, MIN_UBER_EATS_VERSION_SUPPORTED)).thenReturn(true);
    }
}
