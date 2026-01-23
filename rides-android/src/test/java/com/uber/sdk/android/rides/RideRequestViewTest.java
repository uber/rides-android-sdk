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
import android.content.Context;
import android.content.Intent;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import com.google.common.collect.ImmutableList;
import com.uber.sdk.core.auth.AccessToken;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.core.client.AccessTokenSession;
import com.uber.sdk.core.client.SessionConfiguration;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowActivity;

import java.io.IOException;
import java.util.Map;

import static com.uber.sdk.android.rides.TestUtils.readUriResourceWithUserAgentParam;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.robolectric.Shadows.shadowOf;

/**
 * Tests {@link RideRequestView}
 */
public class RideRequestViewTest extends RobolectricTestBase {

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
    private static final String USER_AGENT_RIDE_VIEW = String.format("rides-android-v%s-ride_request_view",
            BuildConfig.VERSION_NAME);

    private AccessToken accessToken;
    private RideRequestView rideRequestView;
    private RideRequestView.RideRequestWebViewClient client;
    private RideRequestView.RideRequestWebViewClientCallback callback;
    private Context context;

    @Before
    public void setup() {
        context = RuntimeEnvironment.application;

        accessToken = new AccessToken(2592000, ImmutableList.of(Scope.RIDE_WIDGETS), TOKEN_STRING,
                "refreshToken", "Bearer");

        rideRequestView = new RideRequestView(Robolectric.setupActivity(Activity.class));
        callback = mock(RideRequestView.RideRequestWebViewClientCallback.class);
        client = rideRequestView.new RideRequestWebViewClient(callback);
    }

    @Test
    public void onBuildUrl_inDefaultRegion_shouldHaveUrlWithDefaultDomain() throws IOException {
        String path = "src/test/resources/riderequestviewuris/default_uri";
        String expectedUri = readUriResourceWithUserAgentParam(path, USER_AGENT_RIDE_VIEW);

        RideParameters rideParameters = new RideParameters.Builder().build();

        SessionConfiguration configuration = new SessionConfiguration.Builder()
                .setClientId("clientId")
                .build();

        String result = RideRequestView.buildUrlFromRideParameters(context, rideParameters, configuration);
        assertEquals(expectedUri, result);
    }


    @Test
    public void onBuildUrl_inSandboxMode_shouldHaveUrlWithSandboxParam() throws IOException {
        RideParameters rideParameters = new RideParameters.Builder().build();

        SessionConfiguration configuration = new SessionConfiguration.Builder()
                .setClientId("clientId")
                .setEnvironment(SessionConfiguration.Environment.SANDBOX)
                .build();


        String result = RideRequestView.buildUrlFromRideParameters(context, rideParameters, configuration);
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
        SessionConfiguration configuration = new SessionConfiguration.Builder()
                .setClientId("clientId")
                .build();

        String result = RideRequestView.buildUrlFromRideParameters(context, rideParameters, configuration);
        assertEquals(expectedUri, result);
    }

    @Test
    public void onBuildUrl_withUserAgentNonNull_shouldNotOverride() throws IOException {
        String widgetUserAgent = String.format("rides-android-v%s-ride_request_widget",
                BuildConfig.VERSION_NAME);
        String path = "src/test/resources/riderequestviewuris/default_uri";
        String expectedUri = readUriResourceWithUserAgentParam(path, widgetUserAgent);

        RideParameters rideParameters = new RideParameters.Builder().build();
        rideParameters.setUserAgent(widgetUserAgent);

        SessionConfiguration configuration = new SessionConfiguration.Builder()
                .setClientId("clientId")
                .build();

        String result = RideRequestView.buildUrlFromRideParameters(context, rideParameters, configuration);
        assertEquals(expectedUri, result);
    }

    @Test
    public void onGetHeaders_withAccessToken_shouldReturnCorrectHeader() {
        String token = "accessToken123";
        AccessToken accessToken = new AccessToken(2592000, ImmutableList.of(Scope.HISTORY), token,
                "refreshToken", "tokenType");
        Map<String, String> headers = RideRequestView.getHeaders(accessToken);
        assertEquals(headers.size(), 1);
        assertEquals(headers.get("Authorization"), "Bearer " + token);
    }

    @Test
    public void onRideRequestViewInit_withCustomSession_viewShouldAuthorize() {
        AccessTokenSession session = mock(AccessTokenSession.class);
        rideRequestView.setSession(session);
        assertNotNull(rideRequestView.getSession());
        assertEquals(session, rideRequestView.getSession());
    }

    @Test
    public void onRideRequestViewLoad_withoutSession_viewShouldHaveAuthorizationError() {
        rideRequestView.setRideParameters(new RideParameters.Builder().setPickupLocation(37.2342, -122.4232, null,
                null).build());
        RideRequestViewCallback callback = mock(RideRequestViewCallback.class);
        rideRequestView.setRideRequestViewCallback(callback);
        rideRequestView.load();
        verify(callback, times(1)).onErrorReceived(RideRequestViewError.NO_ACCESS_TOKEN);
    }

    @Test
    public void whileRideRequestViewRunning_whenAccessTokenExpires_shouldReceiveUnauthorizedError() {
        boolean shouldOverrideUrlLoading = client.shouldOverrideUrlLoading(mock(WebView.class),
                "uberconnect://oauth#error=unauthorized");
        verify(callback, times(1)).onErrorParsed(RideRequestViewError.UNAUTHORIZED);
        assertTrue(shouldOverrideUrlLoading);
    }

    @Test
    public void whileRideRequestViewRunning_whenWebResourceErrorOccurs_shouldReceiveWebError() {
        client.onReceivedError(mock(WebView.class), mock(WebResourceRequest.class), mock(WebResourceError.class));
        verify(callback, times(1)).onErrorParsed(RideRequestViewError.CONNECTIVITY_ISSUE);
    }

    @Test
    public void whileRideRequestViewRunning_whenWebHTTPErrorOccurs_shouldDoNothing() {
        client.onReceivedHttpError(mock(WebView.class), mock(WebResourceRequest.class), mock(WebResourceResponse.class));
        verify(callback, never()).onErrorParsed(any(RideRequestViewError.class));
    }

    @Test
    public void whileRideRequestViewRunning_whenUnknownErrorOccurs_shouldReceiveUnknownError() {
        boolean shouldOverrideUrlLoading = client.shouldOverrideUrlLoading(mock(WebView.class),
                "uberconnect://oauth#error=on_fire");
        verify(callback, times(1)).onErrorParsed(RideRequestViewError.UNKNOWN);
        assertTrue(shouldOverrideUrlLoading);
    }

    @Test
    public void whileRideRequestViewRunning_whenRegionErrorOccurs_shouldReceiveRegionError() {
        boolean shouldOverrideUrlLoading = client.shouldOverrideUrlLoading(mock(WebView.class),
                "uberconnect://oauth#error=wrong_region");
        verify(callback, times(1)).onErrorParsed(RideRequestViewError.WRONG_REGION);
        assertTrue(shouldOverrideUrlLoading);
    }

    @Test
    public void shouldOverrideUrlLoading_whenHttpUrl_shouldNotOverride() {
        assertFalse(client.shouldOverrideUrlLoading(mock(WebView.class), "http://uber.com"));
        verifyNoInteractions(callback);
    }

    @Test
    public void shouldOverrideUrlLoading_whenHttpsUrl_shouldNotOverride() {
        assertFalse(client.shouldOverrideUrlLoading(mock(WebView.class), "https://uber.com"));
        verifyNoInteractions(callback);
    }

    @Test
    @Ignore
    public void shouldOverrideUrlLoading_whenNonHttpOrRedirect_shouldOverrideAndLaunchActivity() {
        Activity activity = Robolectric.setupActivity(Activity.class);
        ShadowActivity shadowActivity = shadowOf(activity);
        RideRequestView rideRequestView = new RideRequestView(activity);

        client = rideRequestView.new RideRequestWebViewClient(callback);

        assertTrue(client.shouldOverrideUrlLoading(mock(WebView.class), "tel:+91555555555"));
        verifyNoInteractions(callback);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals(Intent.ACTION_VIEW, startedIntent.getAction());
        assertEquals("tel:+91555555555#Intent;action=android.intent.action.VIEW;end", startedIntent.toUri(0));
    }
}
