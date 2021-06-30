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

package com.uber.sdk.android.rides;

import android.content.Context;
import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;

import com.uber.sdk.android.core.Deeplink;
import com.uber.sdk.android.core.utils.AppProtocol;
import com.uber.sdk.android.core.utils.CustomTabsHelper;
import com.uber.sdk.core.client.SessionConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static com.uber.sdk.android.core.SupportedAppType.UBER;
import static com.uber.sdk.android.rides.TestUtils.readUriResourceWithUserAgentParam;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link RideRequestDeeplink}
 */
public class RideRequestDeeplinkTest extends RobolectricTestBase {

    private static final String PRODUCT_ID = "productId";
    private static final Double PICKUP_LAT = 32.1234;
    private static final Double PICKUP_LONG = -122.3456;
    private static final String PICKUP_NICK = "pickupNick";
    private static final String PICKUP_ADDR = "Pickup Address";
    private static final Double DROPOFF_LAT = 32.5678;
    private static final Double DROPOFF_LONG = -122.6789;
    private static final String DROPOFF_NICK = "pickupNick";
    private static final String DROPOFF_ADDR = "Dropoff Address";
    private static final String USER_AGENT_DEEPLINK = String
            .format("rides-android-v%s-deeplink", BuildConfig.VERSION_NAME);

    @Mock Context context;
    @Mock AppProtocol appProtocol;
    @Mock CustomTabsHelper customTabsHelper;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(appProtocol.isInstalled(eq(context), eq(UBER))).thenReturn(true);
        when(appProtocol.isAppLinkSupported()).thenReturn(false);
    }

    @Test
    public void onBuildDeeplink_whenClientIdAndDefaultRideParamsProvided_shouldHaveDefaults() throws IOException {
        String expectedUri = readUriResourceWithUserAgentParam("src/test/resources/deeplinkuris/just_client_provided",
                USER_AGENT_DEEPLINK);

        RideParameters rideParameters = new RideParameters.Builder().build();
        RideRequestDeeplink deeplink = new RideRequestDeeplink.Builder(context)
                .setAppProtocol(appProtocol)
                .setRideParameters(rideParameters)
                .setSessionConfiguration(new SessionConfiguration.Builder().setClientId("clientId").build())
                .build();

        assertEquals("URI does not match.", expectedUri, deeplink.getUri().toString());
    }

    @Test
    public void onBuildDeeplink_whenFullRideParamsProvided_shouldCompleteUri() throws IOException {
        String expectedUri = readUriResourceWithUserAgentParam("src/test/resources/deeplinkuris/full_details_uri",
                USER_AGENT_DEEPLINK);

        RideParameters rideParameters = new RideParameters.Builder()
                .setPickupLocation(PICKUP_LAT, PICKUP_LONG, PICKUP_NICK, PICKUP_ADDR)
                .setDropoffLocation(DROPOFF_LAT, DROPOFF_LONG, DROPOFF_NICK, DROPOFF_ADDR)
                .setProductId(PRODUCT_ID)
                .build();
        RideRequestDeeplink deeplink = new RideRequestDeeplink.Builder(context)
                .setAppProtocol(appProtocol)
                .setRideParameters(rideParameters)
                .setSessionConfiguration(new SessionConfiguration.Builder().setClientId("clientId").build())
                .build();

        assertEquals("URI does not match.", expectedUri, deeplink.getUri().toString());
    }

    @Test
    public void onBuildDeeplink_whenPickupAndClientIdProvided_shouldNotHaveDropoffOrProduct()
            throws IOException {
        String path = "src/test/resources/deeplinkuris/pickup_and_client_provided";
        String expectedUri = readUriResourceWithUserAgentParam(path, USER_AGENT_DEEPLINK);

        RideParameters rideParameters = new RideParameters.Builder()
                .setPickupLocation(PICKUP_LAT, PICKUP_LONG, PICKUP_NICK, PICKUP_ADDR)
                .build();
        RideRequestDeeplink deeplink = new RideRequestDeeplink.Builder(context)
                .setAppProtocol(appProtocol)
                .setRideParameters(rideParameters)
                .setSessionConfiguration(new SessionConfiguration.Builder().setClientId("clientId").build())
                .build();

        assertEquals("URI does not match.", expectedUri, deeplink.getUri().toString());
    }

    @Test
    public void onBuildDeeplink_whenDropoffClientIdAndProductIdProvided_shouldHaveDefaultPickupAndFullDropoff()
            throws IOException {
        String path = "src/test/resources/deeplinkuris/dropoff_client_and_product_provided";
        String expectedUri = readUriResourceWithUserAgentParam(path, USER_AGENT_DEEPLINK);

        RideParameters rideParameters = new RideParameters.Builder()
                .setProductId(PRODUCT_ID)
                .setDropoffLocation(DROPOFF_LAT, DROPOFF_LONG, DROPOFF_NICK, DROPOFF_ADDR)
                .build();
        RideRequestDeeplink deeplink = new RideRequestDeeplink.Builder(context)
                .setAppProtocol(appProtocol)
                .setRideParameters(rideParameters)
                .setSessionConfiguration(new SessionConfiguration.Builder().setClientId("clientId").build())
                .build();

        assertEquals("URI does not match.", expectedUri, deeplink.getUri().toString());
    }

    @Test
    public void onBuildDeeplink_whenNoNicknameOrAddressProvided_shouldNotHaveNicknameAndAddress()
            throws IOException {
        String expectedUri = readUriResourceWithUserAgentParam("src/test/resources/deeplinkuris/missing_nickname_or_address",
                USER_AGENT_DEEPLINK);

        RideParameters rideParameters = new RideParameters.Builder()
                .setProductId(PRODUCT_ID)
                .setPickupLocation(PICKUP_LAT, PICKUP_LONG, null, null)
                .setDropoffLocation(DROPOFF_LAT, DROPOFF_LONG, null, null)
                .build();
        RideRequestDeeplink deeplink = new RideRequestDeeplink.Builder(context)
                .setAppProtocol(appProtocol)
                .setRideParameters(rideParameters)
                .setSessionConfiguration(new SessionConfiguration.Builder().setClientId("clientId").build())
                .build();

        assertEquals("URI does not match.", expectedUri, deeplink.getUri().toString());
    }

    @Test(expected = NullPointerException.class)
    public void onBuildDeeplink_whenNoRideParams_shouldNotBuild() {
        new RideRequestDeeplink.Builder(context).build();
    }

    @Test
    public void getUri_whenUberAppInstalledAndAppLinkSupported_shouldUseAppLink() throws IOException {
        String expectedUri = readUriResourceWithUserAgentParam
                ("src/test/resources/deeplinkuris/mobile_web_ul_just_client_provided",
                USER_AGENT_DEEPLINK);

        when(appProtocol.isAppLinkSupported()).thenReturn(true);

        RideParameters rideParameters = new RideParameters.Builder().build();

        RideRequestDeeplink rideRequestDeeplink = new RideRequestDeeplink.Builder(context)
                .setAppProtocol(appProtocol)
                .setRideParameters(rideParameters)
                .setSessionConfiguration(new SessionConfiguration.Builder().setClientId("clientId").build())
                .build();

        assertThat(rideRequestDeeplink.getUri().toString()).isEqualTo(expectedUri);
    }

    @Test
    public void getUri_whenUberAppInstalledAndAppLinkNotSupported_shouldUseNativeLink() throws IOException {
        String expectedUri = readUriResourceWithUserAgentParam
                ("src/test/resources/deeplinkuris/just_client_provided",
                        USER_AGENT_DEEPLINK);

        when(appProtocol.isAppLinkSupported()).thenReturn(false);

        RideParameters rideParameters = new RideParameters.Builder().build();
        RideRequestDeeplink rideRequestDeeplink = new RideRequestDeeplink.Builder(context)
                .setAppProtocol(appProtocol)
                .setRideParameters(rideParameters)
                .setSessionConfiguration(new SessionConfiguration.Builder().setClientId("clientId").build())
                .build();

        assertThat(rideRequestDeeplink.getUri().toString()).isEqualTo(expectedUri);
    }

    @Test
    public void getUri_whenUberAppNotInstalledAndFallbackMobileWeb_shouldUseMobileWeb() throws IOException {
        String expectedUri = readUriResourceWithUserAgentParam
                ("src/test/resources/deeplinkuris/mobile_web_just_client_provided",
                        USER_AGENT_DEEPLINK);

        when(appProtocol.isInstalled(eq(context), eq(UBER))).thenReturn(false);

        RideParameters rideParameters = new RideParameters.Builder().build();
        RideRequestDeeplink rideRequestDeeplink = new RideRequestDeeplink.Builder(context)
                .setAppProtocol(appProtocol)
                .setRideParameters(rideParameters)
                .setSessionConfiguration(new SessionConfiguration.Builder().setClientId("clientId").build())
                .setFallback(Deeplink.Fallback.MOBILE_WEB)
                .build();

        assertThat(rideRequestDeeplink.getUri().toString()).isEqualTo(expectedUri);
    }

    @Test
    public void getUri_whenUberAppNotInstalledAndFallbackAppInstall_shouldUseAppInstall() throws IOException {
        String expectedUri = readUriResourceWithUserAgentParam
                ("src/test/resources/deeplinkuris/mobile_web_ul_just_client_provided",
                        USER_AGENT_DEEPLINK);

        when(appProtocol.isInstalled(eq(context), eq(UBER))).thenReturn(false);

        RideParameters rideParameters = new RideParameters.Builder().build();
        RideRequestDeeplink rideRequestDeeplink = new RideRequestDeeplink.Builder(context)
                .setAppProtocol(appProtocol)
                .setRideParameters(rideParameters)
                .setSessionConfiguration(new SessionConfiguration.Builder().setClientId("clientId").build())
                .setFallback(Deeplink.Fallback.APP_INSTALL)
                .build();

        assertThat(rideRequestDeeplink.getUri().toString()).isEqualTo(expectedUri);
    }

    @Test
    public void getUri_whenUberAppNotInstalledAndFallbackNotSet_shouldUseAppInstall() throws IOException {
        String expectedUri = readUriResourceWithUserAgentParam
                ("src/test/resources/deeplinkuris/mobile_web_ul_just_client_provided",
                        USER_AGENT_DEEPLINK);

        when(appProtocol.isInstalled(eq(context), eq(UBER))).thenReturn(false);

        RideParameters rideParameters = new RideParameters.Builder().build();
        RideRequestDeeplink rideRequestDeeplink = new RideRequestDeeplink.Builder(context)
                .setAppProtocol(appProtocol)
                .setRideParameters(rideParameters)
                .setSessionConfiguration(new SessionConfiguration.Builder().setClientId("clientId").build())
                .build();

        assertThat(rideRequestDeeplink.getUri().toString()).isEqualTo(expectedUri);
    }

    @Test
    public void getUri_withCustomTab_callsCustomTabHelper() {
        RideParameters rideParameters = new RideParameters.Builder().build();


        RideRequestDeeplink rideRequestDeeplink = new RideRequestDeeplink.Builder(context)
                .setAppProtocol(appProtocol)
                .setCustomTabsHelper(customTabsHelper)
                .setRideParameters(rideParameters)
                .setSessionConfiguration(new SessionConfiguration.Builder().setClientId("clientId").build())
                .build();

        rideRequestDeeplink.execute();
        ArgumentCaptor<Uri> argumentCaptor = ArgumentCaptor.forClass(Uri.class);
        verify(customTabsHelper).openCustomTab(eq(context), any(CustomTabsIntent.class),
                argumentCaptor.capture(), any(CustomTabsHelper.BrowserFallback.class));

        Uri uri = argumentCaptor.getValue();
        assertThat(uri).isEqualTo(rideRequestDeeplink.getUri());
    }
}
