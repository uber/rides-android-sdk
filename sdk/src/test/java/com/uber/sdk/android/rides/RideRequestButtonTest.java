/*
 * Copyright (c) 2015 Uber Technologies, Inc.
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
import android.content.res.Resources;
import android.util.AttributeSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.res.Attribute;
import org.robolectric.res.ResourceLoader;
import org.robolectric.shadows.RoboAttributeSet;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

/**
 * Tests {@link RideRequestButton}
 */
public class RideRequestButtonTest extends RobolectricTestBase {

    private Activity mActivity;
    private RideRequestButton mRequestButton;
    private final static String UB_STYLE_ATTR = "%s:attr/ub__style";


    @Captor
    private ArgumentCaptor<Context> mContextArguementCaptor;
    @Captor
    private ArgumentCaptor<RideParameters> mRideParamsArguementCaptor;

    @Before
    public void setup() {
        mActivity = Robolectric.setupActivity(Activity.class);
        mRequestButton = new RideRequestButton(mActivity);
    }

    @Test
    public void onClick_whenHasSetRequestBehaviorAndRideParams_shouldCallRequestRideAndAddUserAgent() {
        RideParameters rideParameters = new RideParameters.Builder().build();
        mRequestButton.setRideParameters(rideParameters);
        RideRequestBehavior rideRequestBehavior = mock(RideRequestBehavior.class);
        mRequestButton.setRequestBehavior(rideRequestBehavior);
        mRequestButton.performClick();

        verify(rideRequestBehavior, times(1)).requestRide(mActivity, rideParameters);
        assertEquals("rides-android-v0.3.2-button", rideParameters.getUserAgent());
    }

    @Test
    public void testInit_whenCalled_shouldCallGetStyleWithDefault() {
        RideRequestButton button = spy(mRequestButton);
        doReturn(RideRequestButton.Style.DEFAULT.getStyleId())
                .when(button)
                .getStyleWithDefault(any(Context.class), isNull(AttributeSet.class), anyInt());

        button.init(mActivity, null, 0, R.style.UberButton_RideRequest);

        verify(button, times(1))
                .getStyleWithDefault(any(Context.class), isNull(AttributeSet.class), anyInt());
    }

    @Test
    public void testGetStyleWithDefault_whenBlack_shouldUseBlack() {
        Resources resources = RuntimeEnvironment.application.getResources();
        ResourceLoader resourceLoader = shadowOf(resources).getResourceLoader();
        AttributeSet set = new RoboAttributeSet(
                Arrays.asList(new Attribute(getUbStyleAttr(), "black", getPackage())),
                resourceLoader);

        assertEquals(RideRequestButton.Style.BLACK.getStyleId(),
                mRequestButton.getStyleWithDefault(mActivity, set, 0));
    }

    @Test
    public void testGetStyleWithDefault_whenWhite_shouldUseWhite() {
        Resources resources = RuntimeEnvironment.application.getResources();
        ResourceLoader resourceLoader = shadowOf(resources).getResourceLoader();
        AttributeSet set = new RoboAttributeSet(
                Arrays.asList(new Attribute(getUbStyleAttr(), "white", getPackage())),
                resourceLoader);

        assertEquals(RideRequestButton.Style.WHITE.getStyleId(),
                mRequestButton.getStyleWithDefault(mActivity, set, 0));
    }

    @Test
    public void testGetStyleWithDefault_whenEmpty_shouldUseDefault() {
        assertEquals(RideRequestButton.Style.DEFAULT.getStyleId(),
                mRequestButton.getStyleWithDefault(mActivity, null, 0));
    }

    private String getUbStyleAttr() {
        return String.format(UB_STYLE_ATTR, getPackage());
    }

    private String getPackage() {
        return mActivity.getPackageName();
    }
}
