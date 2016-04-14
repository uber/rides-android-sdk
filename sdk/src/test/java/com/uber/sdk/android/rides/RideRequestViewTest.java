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

import android.app.Activity;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.google.common.collect.ImmutableList;
import com.uber.sdk.android.rides.auth.AccessToken;
import com.uber.sdk.android.rides.auth.Scope;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import static com.uber.sdk.android.rides.TestUtils.readUriResourceWithUserAgentParam;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests {@link RideRequestView}
 */
public class RideRequestViewTest extends RobolectricTestBase {

    boolean[] callbackSuccess = new boolean[1];

    private static final String CLIENT_ID = "clientId";
    private static final String PRODUCT_ID = "productId";
    private static final Double PICKUP_LAT = 32.1234;
    private static final Double PICKUP_LONG = -122.3456;
    private static final String PICKUP_NICK = "pickupNick";
    private static final String PICKUP_ADDR = "Pickup Address";
    private static final Double DROPOFF_LAT = 32.5678;
    private static final Double DROPOFF_LONG = -122.6789;
    private static final String DROPOFF_NICK = "pickupNick";
    private static final String DROPOFF_ADDR = "Dropoff Address";
    private static final String TOKEN_STRING = "thisIsAnAccessToken";
    private static final String USER_AGENT_RIDE_VIEW = "rides-android-v0.3.0-ride_request_view";
    private AccessToken mAccessToken;
    private RideRequestView mRideRequestView;

    @Before
    public void setup() {
        UberSdk.initialize(RuntimeEnvironment.application, CLIENT_ID);
        mAccessToken = new AccessToken(new Date(1458770906206l), ImmutableList.of(Scope.RIDE_WIDGETS), TOKEN_STRING);
        mRideRequestView = new RideRequestView(Robolectric.setupActivity(Activity.class));
    }

    @Test
    public void onBuildUrl_inDefaultRegion_shouldHaveUrlWithDefaultDomain() throws IOException {
        String path = "src/test/resources/riderequestviewuris/default_uri";
        String expectedUri = readUriResourceWithUserAgentParam(path, USER_AGENT_RIDE_VIEW);

        RideParameters rideParameters = new RideParameters.Builder().build();
        String result = RideRequestView.buildUrlFromRideParameters(rideParameters);
        assertEquals(expectedUri, result);
    }

    @Test
    public void onBuildUrl_inChinaRegion_shouldHaveUrlWithChinaDomain() throws IOException {
        String path = "src/test/resources/riderequestviewuris/china_uri";
        String expectedUri = readUriResourceWithUserAgentParam(path, USER_AGENT_RIDE_VIEW);

        RideParameters rideParameters = new RideParameters.Builder().build();
        UberSdk.setRegion(UberSdk.Region.CHINA);
        String result = RideRequestView.buildUrlFromRideParameters(rideParameters);
        assertEquals(expectedUri, result);
    }

    @Test
    public void onBuildUrl_inSandboxMode_shouldHaveUrlWithSandboxParam() throws IOException {
        RideParameters rideParameters = new RideParameters.Builder().build();
        UberSdk.setSandboxMode(true);
        String result = RideRequestView.buildUrlFromRideParameters(rideParameters);
        assertTrue(result.contains("&env=sandbox"));
    }

    @Test
    public void onBuildUrl_withRideParams_shouldHaveRideParamsQueryParams() throws IOException {
        String path = "src/test/resources/riderequestviewuris/full_details_uri";
        String expectedUri = readUriResourceWithUserAgentParam(path, USER_AGENT_RIDE_VIEW);

        RideParameters rideParameters = new RideParameters.Builder()
                .setPickupLocation(PICKUP_LAT, PICKUP_LONG, PICKUP_NICK, PICKUP_ADDR)
                .setDropoffLocation(DROPOFF_LAT, DROPOFF_LONG, DROPOFF_NICK, DROPOFF_ADDR)
                .setProductId(PRODUCT_ID)
                .build();
        String result = RideRequestView.buildUrlFromRideParameters(rideParameters);
        assertEquals(expectedUri, result);
    }

    @Test
    public void onBuildUrl_withUserAgentNonNull_shouldNotOverride() throws IOException {
        String widgetUserAgent = "rides-android-v0.3.0-ride_request_widget";
        String path = "src/test/resources/riderequestviewuris/default_uri";
        String expectedUri = readUriResourceWithUserAgentParam(path, widgetUserAgent);

        RideParameters rideParameters = new RideParameters.Builder().build();
        rideParameters.setUserAgent(widgetUserAgent);
        String result = RideRequestView.buildUrlFromRideParameters(rideParameters);
        assertEquals(expectedUri, result);
    }

    @Test
    public void onGetHeaders_withAccessToken_shouldReturnCorrectHeader() {
        String token = "accessToken123";
        AccessToken accessToken = new AccessToken(new Date(2384938l), ImmutableList.of(Scope.HISTORY), token);
        Map<String, String> headers = RideRequestView.getHeaders(accessToken);
        assertEquals(headers.size(), 1);
        assertEquals(headers.get("Authorization"), "Bearer "+token);
    }

    @Test
    public void onRideRequestViewInit_withCustomAccessToken_viewShouldAuthorize() {
        mRideRequestView.setAccessToken(mAccessToken);
        assertNotNull(mRideRequestView.getAccessToken());
        assertEquals(mRideRequestView.getAccessToken().getToken(), TOKEN_STRING);
    }

    @Test
    public void onRideRequestViewLoad_withoutAccessToken_viewShouldHaveAuthorizationError() {
        mRideRequestView.setRideParameters(new RideParameters.Builder().setPickupLocation(37.2342, -122.4232, null,
                null).build());
        RideRequestViewCallback callback = mock(RideRequestViewCallback.class);
        mRideRequestView.setRideRequestViewCallback(callback);
        mRideRequestView.load();
        verify(callback, times(1)).onErrorReceived(RideRequestViewError.NO_ACCESS_TOKEN);
    }

    @Test
    public void whileRideRequestViewRunning_whenAccessTokenExpires_shouldReceiveUnauthorizedError() {
        RideRequestView.RideRequestWebViewClientCallback callback = mock(RideRequestView.RideRequestWebViewClientCallback.class);
        RideRequestView.RideRequestWebViewClient client = new RideRequestView.RideRequestWebViewClient(callback);
        boolean shouldOverrideUrlLoading = client.shouldOverrideUrlLoading(mock(WebView.class),
                "uberconnect://oauth#error=unauthorized");
        verify(callback, times(1)).onErrorParsed(RideRequestViewError.UNAUTHORIZED);
        assertTrue(shouldOverrideUrlLoading);
    }

    @Test
    public void whileRideRequestViewRunning_whenWebResourceErrorOccurs_shouldReceiveWebError() {
        RideRequestView.RideRequestWebViewClientCallback callback = mock(RideRequestView
                .RideRequestWebViewClientCallback.class);
        RideRequestView.RideRequestWebViewClient client = new RideRequestView.RideRequestWebViewClient(callback);
        client.onReceivedError(mock(WebView.class), mock(WebResourceRequest.class), mock(WebResourceError.class));
        verify(callback, times(1)).onErrorParsed(RideRequestViewError.CONNECTIVITY_ISSUE);
    }

    @Test
    public void whileRideRequestViewRunning_whenWebHTTPErrorOccurs_shouldReceiveWebError() {
        RideRequestView.RideRequestWebViewClientCallback callback = mock(RideRequestView
                .RideRequestWebViewClientCallback.class);
        RideRequestView.RideRequestWebViewClient client = new RideRequestView.RideRequestWebViewClient(callback);
        client.onReceivedHttpError(mock(WebView.class), mock(WebResourceRequest.class), mock(WebResourceResponse.class));
        verify(callback, times(1)).onErrorParsed(RideRequestViewError.CONNECTIVITY_ISSUE);
    }

    @Test
    public void whileRideRequestViewRunning_whenUnknownErrorOccurs_shouldReceiveUnknownError() {
        RideRequestView.RideRequestWebViewClientCallback callback = mock(RideRequestView
                .RideRequestWebViewClientCallback.class);
        RideRequestView.RideRequestWebViewClient client = new RideRequestView.RideRequestWebViewClient(callback);
        boolean shouldOverrideUrlLoading = client.shouldOverrideUrlLoading(mock(WebView.class),
                "uberconnect://oauth#error=on_fire");
        verify(callback, times(1)).onErrorParsed(RideRequestViewError.UNKNOWN);
        assertTrue(shouldOverrideUrlLoading);
    }

    @Test
    public void whileRideRequestViewRunning_whenRegionErrorOccurs_shouldReceiveRegionError() {
        RideRequestView.RideRequestWebViewClientCallback callback = mock(RideRequestView
                .RideRequestWebViewClientCallback.class);
        RideRequestView.RideRequestWebViewClient client = new RideRequestView.RideRequestWebViewClient(callback);
        boolean shouldOverrideUrlLoading = client.shouldOverrideUrlLoading(mock(WebView.class),
                "uberconnect://oauth#error=wrong_region");
        verify(callback, times(1)).onErrorParsed(RideRequestViewError.WRONG_REGION);
        assertTrue(shouldOverrideUrlLoading);
    }

    @After
    public void teardown() {
        UberSdkAccessor.clearPrefs();
    }
}
