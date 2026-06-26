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

package com.uber.sdk.android.rides.internal;


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static java.lang.Double.valueOf;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.squareup.moshi.Moshi;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButtonCallback;
import com.uber.sdk.core.client.internal.BigDecimalAdapter;
import com.uber.sdk.rides.client.error.ApiError;
import com.uber.sdk.rides.client.model.PriceEstimate;
import com.uber.sdk.rides.client.model.PriceEstimatesResponse;
import com.uber.sdk.rides.client.model.TimeEstimate;
import com.uber.sdk.rides.client.model.TimeEstimatesResponse;
import com.uber.sdk.rides.client.services.RidesService;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class RideRequestButtonControllerTest {

    private static final String TIME_ESTIMATES_API = "/v1.2/estimates/time";
    private static final String PRICE_ESTIMATES_API = "/v1.2/estimates/price";
    private static WireMockConfiguration WIRE_MOCK_CONFIG = wireMockConfig()
            .notifier(new ConsoleNotifier(true))
            .dynamicPort();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WIRE_MOCK_CONFIG);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String PRODUCT_ID = "a1111c8c-c720-46c3-8534-2fcdd730040d";

    private static final float DROP_OFF_LATITUDE = 1.2f;
    private static final float DROP_OFF_LONGITUDE = 1.3f;

    private static final String DROP_OFF_NICKNAME = "drop off";
    private static final String DROP_OFF_ADDRESS = "1455 Market St, Fremont.";

    private static final float PICKUP_LONGITUDE = 2.3f;
    private static final float PICKUP_LATITUDE = 2.1f;

    private static final String PICKUP_ADDRESS = "685 Market St, San Francisco";
    private static final String PICKUP_NICKNAME = "pick up";

    @Mock
    RideRequestButtonView view;

    @Mock
    Call<TimeEstimatesResponse> timeEstimateCall;

    @Mock
    Call<PriceEstimatesResponse> priceEstimateCall;

    @Mock
    RideRequestButtonCallback callback;

    private RideRequestButtonController controller;
    private RideParameters rideParameters;

    private RidesService service;
    private CountDownLatch countDownLatch;
    private OkHttpClient okHttpClient;

    @Before
    public void setUp() throws Exception {

        rideParameters = new RideParameters.Builder()
                .setProductId(PRODUCT_ID)
                .setDropoffLocation(valueOf(DROP_OFF_LATITUDE),
                        valueOf(DROP_OFF_LONGITUDE), DROP_OFF_NICKNAME, DROP_OFF_ADDRESS)
                .setPickupLocation(valueOf(PICKUP_LATITUDE), valueOf(PICKUP_LONGITUDE),
                        PICKUP_NICKNAME, PICKUP_ADDRESS)
                .build();

        Moshi moshi = new Moshi.Builder()
                .add(new BigDecimalAdapter())
                .build();

        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY))
                .readTimeout(1, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();

        countDownLatch = new CountDownLatch(2);

        service = new Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .callbackExecutor(new Executor() {
                    @Override
                    public void execute(@Nonnull Runnable command) {
                        command.run();
                        countDownLatch.countDown();
                    }
                })
                .client(okHttpClient)
                .baseUrl("http://localhost:" + wireMockRule.port())
                .build()
                .create(RidesService.class);

        controller = new RideRequestButtonController(view, service, callback);
    }

    @After
    public void tearDown() throws Exception {
        wireMockRule.resetMappings();
    }

    @Test
    public void testLoadInformationWithProductId_whenEstimatesSuccessful() throws Exception {
        stubPriceApiSuccessful();

        stubTimeApiWithProductIdSuccessful();

        controller.loadRideInformation(rideParameters);

        countDownLatch.await(3, TimeUnit.SECONDS);

        verify(callback).onRideInformationLoaded();
        verify(callback, never()).onError(Mockito.any(ApiError.class));
        verify(callback, never()).onError(Mockito.any(Throwable.class));

        ArgumentCaptor<PriceEstimate> priceCaptor = ArgumentCaptor.forClass(PriceEstimate.class);
        ArgumentCaptor<TimeEstimate> timeCaptor = ArgumentCaptor.forClass(TimeEstimate.class);

        verify(view).showEstimate(timeCaptor.capture(), priceCaptor.capture());
        assertThat(priceCaptor.getValue().getEstimate()).isEqualTo("$9-12");
        assertThat(timeCaptor.getValue().getEstimate()).isEqualTo(120);
    }

    @Test
    public void testLoadInformationNoProductId_whenEstimatesSuccessful() throws Exception {
        stubPriceApiSuccessful();

        stubTimeApiSuccessful();

        rideParameters = new RideParameters.Builder()
                .setDropoffLocation(valueOf(DROP_OFF_LATITUDE),
                        valueOf(DROP_OFF_LONGITUDE), DROP_OFF_NICKNAME, DROP_OFF_ADDRESS)
                .setPickupLocation(valueOf(PICKUP_LATITUDE), valueOf(PICKUP_LONGITUDE),
                        PICKUP_NICKNAME, PICKUP_ADDRESS)
                .build();

        controller.loadRideInformation(rideParameters);

        countDownLatch.await(3, TimeUnit.SECONDS);

        verify(callback).onRideInformationLoaded();
        verify(callback, never()).onError(Mockito.any(ApiError.class));
        verify(callback, never()).onError(Mockito.any(Throwable.class));

        ArgumentCaptor<PriceEstimate> priceCaptor = ArgumentCaptor.forClass(PriceEstimate.class);
        ArgumentCaptor<TimeEstimate> timeCaptor = ArgumentCaptor.forClass(TimeEstimate.class);

        verify(view).showEstimate(timeCaptor.capture(), priceCaptor.capture());
        assertThat(priceCaptor.getValue().getEstimate()).isEqualTo("$5.75");
        assertThat(timeCaptor.getValue().getEstimate()).isEqualTo(100);
    }

    @Test
    public void testLoadInformation_whenEstimatesSuccessfulButViewDestroyed() throws Exception {
        stubPriceApiSuccessful();

        stubTimeApiWithProductIdSuccessful();

        controller.destroy();

        controller.loadRideInformation(rideParameters);

        assertThat(controller.pendingDelegate.view).isNull();
        assertThat(controller.pendingDelegate.callback).isNull();

        countDownLatch.await(3, TimeUnit.SECONDS);

        verifyNoInteractions(callback);
        verifyNoInteractions(view);
    }

    @Test
    public void testLoadInformationTimeOnly_whenEstimatesSuccessfulButViewDestroyed() throws Exception {
        stubPriceApiSuccessful();

        stubTimeApiWithProductIdSuccessful();

        rideParameters = new RideParameters.Builder()
                .setProductId(PRODUCT_ID)
                .setPickupLocation(valueOf(PICKUP_LATITUDE), valueOf(PICKUP_LONGITUDE),
                        PICKUP_NICKNAME, PICKUP_ADDRESS)
                .build();

        controller.destroy();

        controller.loadRideInformation(rideParameters);

        assertThat(controller.pendingDelegate.view).isNull();
        assertThat(controller.pendingDelegate.callback).isNull();

        countDownLatch.await(3, TimeUnit.SECONDS);

        verifyNoInteractions(callback);
        verifyNoInteractions(view);
    }

    @Test
    public void testLoadInformation_whenErrorButViewDestroyed() throws Exception {
        stubTimeApi(aResponse().withStatus(500));

        stubPriceApi(aResponse().withStatus(500));

        controller.destroy();

        controller.loadRideInformation(rideParameters);

        assertThat(controller.pendingDelegate.view).isNull();
        assertThat(controller.pendingDelegate.callback).isNull();

        countDownLatch.await(3, TimeUnit.SECONDS);

        verifyNoInteractions(callback);
        verifyNoInteractions(view);
    }

    @Test
    public void testLoadInformationWithProductId_whenTimeOnly() throws Exception {
        stubPriceApi(aResponse().withStatus(500));

        stubTimeApiWithProductIdSuccessful();

        rideParameters = new RideParameters.Builder()
                .setProductId(PRODUCT_ID)
                .setPickupLocation(valueOf(PICKUP_LATITUDE), valueOf(PICKUP_LONGITUDE),
                        PICKUP_NICKNAME, PICKUP_ADDRESS)
                .build();

        controller.loadRideInformation(rideParameters);

        countDownLatch.countDown();
        countDownLatch.await(3, TimeUnit.SECONDS);

        verify(callback).onRideInformationLoaded();
        verify(callback, never()).onError(Mockito.any(ApiError.class));
        verify(callback, never()).onError(Mockito.any(Throwable.class));

        ArgumentCaptor<TimeEstimate> timeCaptor = ArgumentCaptor.forClass(TimeEstimate.class);
        verify(view).showEstimate(timeCaptor.capture());
        assertThat(timeCaptor.getValue().getEstimate()).isEqualTo(120);
    }

    @Test
    public void testLoadInformationNoProductId_whenTimeOnly() throws Exception {
        stubPriceApi(aResponse().withStatus(500));

        stubTimeApiSuccessful();

        rideParameters = new RideParameters.Builder()
                .setProductId(PRODUCT_ID)
                .setPickupLocation(valueOf(PICKUP_LATITUDE), valueOf(PICKUP_LONGITUDE),
                        PICKUP_NICKNAME, PICKUP_ADDRESS)
                .build();

        controller.loadRideInformation(rideParameters);

        countDownLatch.countDown();
        countDownLatch.await(3, TimeUnit.SECONDS);

        verify(callback).onRideInformationLoaded();
        verify(callback, never()).onError(Mockito.any(ApiError.class));
        verify(callback, never()).onError(Mockito.any(Throwable.class));

        ArgumentCaptor<TimeEstimate> timeCaptor = ArgumentCaptor.forClass(TimeEstimate.class);
        verify(view).showEstimate(timeCaptor.capture());
        assertThat(timeCaptor.getValue().getEstimate()).isEqualTo(120);
    }

    @Test
    public void testLoadInformation_whenPriceReturnsApiError() throws Exception {
        stubPriceApi(aResponse().withStatus(500));

        stubTimeApiWithProductIdSuccessful();

        controller.loadRideInformation(rideParameters);

        countDownLatch.await(3, TimeUnit.SECONDS);

        verify(callback, never()).onRideInformationLoaded();

        ArgumentCaptor<ApiError> errorCaptor = ArgumentCaptor.forClass(ApiError.class);
        verify(callback).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getClientErrors().get(0).getStatus()).isEqualTo(500);

        verify(callback, never()).onError(any(Throwable.class));

        verify(view, never()).showEstimate(any(TimeEstimate.class));
        verify(view, never()).showEstimate(any(TimeEstimate.class), any(PriceEstimate.class));

        verify(view).showDefaultView();
    }

    @Test
    public void testLoadInformation_whenTimeReturnsApiError() throws Exception {
        stubPriceApiSuccessful();

        stubTimeApi(aResponse().withStatus(500));

        controller.loadRideInformation(rideParameters);

        countDownLatch.await(3, TimeUnit.SECONDS);

        verify(callback, never()).onRideInformationLoaded();

        ArgumentCaptor<ApiError> errorCaptor = ArgumentCaptor.forClass(ApiError.class);
        verify(callback).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getClientErrors().get(0).getStatus()).isEqualTo(500);

        verify(callback, never()).onError(any(Throwable.class));

        verify(view, never()).showEstimate(any(TimeEstimate.class));
        verify(view, never()).showEstimate(any(TimeEstimate.class), any(PriceEstimate.class));

        verify(view).showDefaultView();
    }

    @Test
    public void testLoadInformation_whenNoMatchingProductTimeEstimates() throws Exception {
        stubPriceApiSuccessful();

        stubTimeApi(aResponse().withBodyFile("times_estimate_no_uberx.json"));

        controller.loadRideInformation(rideParameters);

        countDownLatch.await(3, TimeUnit.SECONDS);

        verify(callback, never()).onRideInformationLoaded();

        ArgumentCaptor<ApiError> errorCaptor = ArgumentCaptor.forClass(ApiError.class);
        verify(callback).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getClientErrors().get(0).getStatus()).isEqualTo(404);

        verify(callback, never()).onError(any(Throwable.class));

        verify(view, never()).showEstimate(any(TimeEstimate.class));
        verify(view, never()).showEstimate(any(TimeEstimate.class), any(PriceEstimate.class));

        verify(view).showDefaultView();
    }

    @Test
    public void testLoadInformation_whenNoMatchingProductPriceEstimates() throws Exception {
        stubTimeApiSuccessful();

        stubPriceApi(aResponse().withBodyFile("prices_estimate_no_uberx.json"));

        controller.loadRideInformation(rideParameters);

        countDownLatch.await(3, TimeUnit.SECONDS);

        verify(callback, never()).onRideInformationLoaded();

        ArgumentCaptor<ApiError> errorCaptor = ArgumentCaptor.forClass(ApiError.class);
        verify(callback).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getClientErrors().get(0).getStatus()).isEqualTo(404);

        verify(callback, never()).onError(any(Throwable.class));

        verify(view, never()).showEstimate(any(TimeEstimate.class));
        verify(view, never()).showEstimate(any(TimeEstimate.class), any(PriceEstimate.class));

        verify(view).showDefaultView();
    }

    @Test
    public void testLoadInformation_whenPriceEstimatesEmpty() throws Exception {
        stubTimeApiSuccessful();

        stubPriceApi(aResponse().withBody("{}"));

        controller.loadRideInformation(rideParameters);

        countDownLatch.await(3, TimeUnit.SECONDS);

        verify(callback, never()).onRideInformationLoaded();

        ArgumentCaptor<ApiError> errorCaptor = ArgumentCaptor.forClass(ApiError.class);
        verify(callback).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getClientErrors().get(0).getStatus()).isEqualTo(404);

        verify(callback, never()).onError(any(Throwable.class));

        verify(view, never()).showEstimate(any(TimeEstimate.class));
        verify(view, never()).showEstimate(any(TimeEstimate.class), any(PriceEstimate.class));

        verify(view).showDefaultView();
    }

    @Test
    public void testLoadInformation_whenTimeEstimatesEmpty() throws Exception {
        stubTimeApi(aResponse().withBody("{}"));

        stubPriceApiSuccessful();

        controller.loadRideInformation(rideParameters);

        countDownLatch.await(3, TimeUnit.SECONDS);

        verify(callback, never()).onRideInformationLoaded();

        ArgumentCaptor<ApiError> errorCaptor = ArgumentCaptor.forClass(ApiError.class);
        verify(callback).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue().getClientErrors().get(0).getStatus()).isEqualTo(404);

        verify(callback, never()).onError(any(Throwable.class));

        verify(view, never()).showEstimate(any(TimeEstimate.class));
        verify(view, never()).showEstimate(any(TimeEstimate.class), any(PriceEstimate.class));

        verify(view).showDefaultView();
    }

    @Test
    public void testLoadInformation_whenPriceFails() throws Exception {
        stubTimeApi(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE));
        stubPriceApiSuccessful();

        controller.loadRideInformation(rideParameters);

        countDownLatch.await(3, TimeUnit.SECONDS);

        verify(callback, never()).onRideInformationLoaded();

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(callback).onError(errorCaptor.capture());
        assertThat(errorCaptor.getValue()).isNotNull();

        verify(view, never()).showEstimate(any(TimeEstimate.class));
        verify(view, never()).showEstimate(any(TimeEstimate.class), any(PriceEstimate.class));

        verify(view).showDefaultView();
    }

    @Test
    public void testLoadInformation_whenTimeFails() throws Exception {
        stubTimeApiSuccessful();
        stubPriceApi(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE));

        controller.loadRideInformation(rideParameters);

        countDownLatch.await(3, TimeUnit.SECONDS);

        verify(callback, never()).onRideInformationLoaded();

        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(callback).onError(errorCaptor.capture());

        assertThat(errorCaptor.getValue()).isNotNull();

        verify(view, never()).showEstimate(any(TimeEstimate.class));
        verify(view, never()).showEstimate(any(TimeEstimate.class), any(PriceEstimate.class));

        verify(view).showDefaultView();
    }

    @Test
    public void testLoadInformation_wheNoPickup() {
        rideParameters = new RideParameters.Builder()
                .setProductId(PRODUCT_ID)
                .setDropoffLocation(valueOf(DROP_OFF_LATITUDE),
                        valueOf(DROP_OFF_LONGITUDE), DROP_OFF_NICKNAME, DROP_OFF_ADDRESS)
                .build();

        controller.loadRideInformation(rideParameters);

        verify(view).showDefaultView();
        verify(view, never()).showEstimate(any(TimeEstimate.class));
        verify(view, never()).showEstimate(any(TimeEstimate.class), any(PriceEstimate.class));
    }

    @Test
    public void testLoadInformation_wheNoPickupLongitude() {
        rideParameters = new RideParameters.Builder()
                .setProductId(PRODUCT_ID)
                .setPickupLocation(valueOf(PICKUP_LATITUDE), null, PICKUP_NICKNAME, PICKUP_ADDRESS)
                .setDropoffLocation(valueOf(DROP_OFF_LATITUDE),
                        valueOf(DROP_OFF_LONGITUDE), DROP_OFF_NICKNAME, DROP_OFF_ADDRESS)
                .build();

        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Pickup point latitude is set in " +
                "RideParameters but not the longitude.");

        controller.loadRideInformation(rideParameters);
    }

    @Test
    public void testLoadInformation_wheNoPickupLatitude() {
        rideParameters = new RideParameters.Builder()
                .setProductId(PRODUCT_ID)
                .setPickupLocation(null, valueOf(PICKUP_LONGITUDE), PICKUP_NICKNAME, PICKUP_ADDRESS)
                .setDropoffLocation(null, valueOf(DROP_OFF_LONGITUDE), DROP_OFF_NICKNAME, DROP_OFF_ADDRESS)
                .build();

        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Pickup point longitude is set in " +
                "RideParameters but not the latitude.");

        controller.loadRideInformation(rideParameters);
    }

    @Test
    public void testLoadInformation_wheNoDropOffLatitude() {
        rideParameters = new RideParameters.Builder()
                .setProductId(PRODUCT_ID)
                .setPickupLocation(valueOf(PICKUP_LATITUDE), valueOf(PICKUP_LONGITUDE),
                        PICKUP_NICKNAME, PICKUP_ADDRESS)
                .setDropoffLocation(null, valueOf(DROP_OFF_LONGITUDE), DROP_OFF_NICKNAME, DROP_OFF_ADDRESS)
                .build();

        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Dropoff point longitude is set in RideParameters but not the latitude.");

        controller.loadRideInformation(rideParameters);
    }

    @Test
    public void testLoadInformation_wheNoDropOffLongitude() {
        rideParameters = new RideParameters.Builder()
                .setProductId(PRODUCT_ID)
                .setPickupLocation(valueOf(PICKUP_LATITUDE), valueOf(PICKUP_LONGITUDE),
                        PICKUP_NICKNAME, PICKUP_ADDRESS)
                .setDropoffLocation(valueOf(DROP_OFF_LATITUDE), null, DROP_OFF_NICKNAME, DROP_OFF_ADDRESS)
                .build();

        expectedException.expect(NullPointerException.class);
        expectedException.expectMessage("Dropoff point latitude is set in RideParameters but not the longitude.");

        controller.loadRideInformation(rideParameters);
    }

    private static void stubPriceApi(ResponseDefinitionBuilder responseBuilder) {
        stubFor(get(urlPathEqualTo(PRICE_ESTIMATES_API))
                .withQueryParam("start_latitude", equalTo(String.valueOf(PICKUP_LATITUDE)))
                .withQueryParam("start_longitude", equalTo(String.valueOf(PICKUP_LONGITUDE)))
                .withQueryParam("end_latitude", equalTo(String.valueOf(DROP_OFF_LATITUDE)))
                .withQueryParam("end_longitude", equalTo(String.valueOf(DROP_OFF_LONGITUDE)))
                .willReturn(responseBuilder));
    }

    private static void stubPriceApiSuccessful() {
        stubPriceApi(aResponse().withBodyFile("prices_estimate.json"));
    }

    private static void stubTimeApiWithProductIdSuccessful() {
        stubFor(get(urlPathMatching(TIME_ESTIMATES_API))
                .withQueryParam("start_latitude", equalTo(String.valueOf(PICKUP_LATITUDE)))
                .withQueryParam("start_longitude", equalTo(String.valueOf(PICKUP_LONGITUDE)))
                .withQueryParam("product_id", equalTo(PRODUCT_ID))
                .willReturn(aResponse().withBodyFile("time_estimate_uberx.json")));
    }

    private static void stubTimeApiSuccessful() {
        stubTimeApi(aResponse().withBodyFile("times_estimate.json"));
    }

    private static void stubTimeApi(ResponseDefinitionBuilder responseBuilder) {
        stubFor(get(urlPathMatching(TIME_ESTIMATES_API))
                .withQueryParam("start_latitude", equalTo(String.valueOf(PICKUP_LATITUDE)))
                .withQueryParam("start_longitude", equalTo(String.valueOf(PICKUP_LONGITUDE)))
                .willReturn(responseBuilder));
    }
}
