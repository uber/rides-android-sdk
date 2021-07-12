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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uber.sdk.android.core.Deeplink;
import com.uber.sdk.android.core.UberButton;
import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.android.core.UberStyle;
import com.uber.sdk.android.rides.internal.RideRequestButtonController;
import com.uber.sdk.android.rides.internal.RideRequestButtonView;
import com.uber.sdk.core.client.Session;
import com.uber.sdk.core.client.SessionConfiguration;
import com.uber.sdk.rides.client.model.PriceEstimate;
import com.uber.sdk.rides.client.model.TimeEstimate;

import java.util.concurrent.TimeUnit;

import static com.uber.sdk.android.core.utils.Preconditions.checkNotNull;

/**
 * An Uber styled button to request rides with specific {@link RideParameters}. Default {@link RideParameters} is
 * set to a pickup of the device's location. Requires a client ID to function.
 */
public class RideRequestButton extends FrameLayout implements RideRequestButtonView {

    private static final
    @StyleRes
    int[] STYLES = {R.style.UberButton, R.style.UberButton_White};

    private static final String USER_AGENT_BUTTON = String.format("rides-android-v%s-button",
            BuildConfig.VERSION_NAME);

    private RideRequestBehavior rideRequestBehavior;

    @NonNull
    private RideParameters rideParameters = new RideParameters.Builder().build();

    private UberButton requestButton;
    private TextView timeEstimateView;
    private TextView priceEstimateView;
    private RideRequestButtonController controller;
    private Session session;
    private RideRequestButtonCallback callback;
    private Deeplink.Fallback deeplinkFallback;

    public RideRequestButton(Context context) {
        this(context, null);
    }

    public RideRequestButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RideRequestButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.UberButton_Login);
    }

    @TargetApi(21)
    public RideRequestButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        final UberStyle uberStyle = UberStyle.getStyleFromAttribute(context, attrs,
                defStyleRes, R.styleable.RideRequestButton,
                R.styleable.RideRequestButton_ub__style);
        init(context, attrs, defStyleAttr, uberStyle);
    }

    private void init(final Context context, AttributeSet attrs, int defStyleAttr, UberStyle uberStyle) {

        @StyleRes int styleRes = STYLES[uberStyle.getValue()];

        inflate(context, R.layout.ub__ride_request_button, this);

        requestButton = (UberButton) findViewById(R.id.request_button);
        timeEstimateView = (TextView) findViewById(R.id.time_estimate);
        priceEstimateView = (TextView) findViewById(R.id.price_estimate);

        setBackgroundAttributes(context, attrs, defStyleAttr, styleRes);
        setPaddingAttributes(context, attrs, defStyleAttr, styleRes);
        setTextAttributes(context, attrs, defStyleAttr, styleRes);

        showDefaultView();
        deeplinkFallback = Deeplink.Fallback.APP_INSTALL;
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                rideParameters.setUserAgent(USER_AGENT_BUTTON);

                final SessionConfiguration config;
                if (session != null) {
                    config = session.getAuthenticator().getSessionConfiguration();
                } else {
                    config = UberSdk.getDefaultSessionConfiguration();
                }

                RideRequestDeeplink deeplink = new RideRequestDeeplink.Builder(context)
                        .setSessionConfiguration(config)
                        .setFallback(deeplinkFallback)
                        .setRideParameters(rideParameters)
                        .build();
                deeplink.execute();
            }
        });
    }

    /**
     * Sets the {@link RideParameters} that will be used to request a ride when the button is clicked. If not set will
     * use default RideParameters behavior.
     *
     * @return this instance of {@link RideRequestButton}
     */
    public RideRequestButton setRideParameters(@NonNull RideParameters rideParameters) {
        this.rideParameters = rideParameters;
        return this;
    }

    /**
     * Sets the {@link Deeplink.Fallback} to be used when the Uber app isn't installed
     *
     * @return this instance of {@link RideRequestButton}
     */
    public RideRequestButton setDeeplinkFallback(@NonNull Deeplink.Fallback fallback) {
        this.deeplinkFallback = fallback;
        return this;
    }

    /**
     * Sets how the request button should act for button actions.
     *
     * @param requestBehavior an object that implements {@link RideRequestBehavior}
     * @return this instance of {@link RideRequestButton}
     *
     * @deprecated Button will use deeplink by default use RideRequestButton to indicate fallback
     * now instead.
     */
    @Deprecated
    public RideRequestButton setRequestBehavior(@NonNull RideRequestBehavior requestBehavior) {
        rideRequestBehavior = requestBehavior;
        return this;
    }

    /**
     * Set {@link RideRequestButtonCallback}. This is optional but it is recommended to supply one
     * for error handling.
     *
     * @param callback to be notified on Success or Failure
     * @return this instance of {@link RideRequestButton}
     */
    public RideRequestButton setCallback(@NonNull RideRequestButtonCallback callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Retrieve estimates from the server and updates the button accordingly. Requires:
     * 1. {@link #setSession(Session)}
     * 2. {@link #setRideParameters(RideParameters)}, with product_id, pick_up_latitude and pick_up_longitude are required to get Time Estimate. drop_off_latitude and drop_off_longitude are only required if Price Estimate is desired.
     * 3. {@link #setCallback(RideRequestButtonCallback)}, this is although optional but recommended to handle errors and track events.
     */
    public void loadRideInformation() {

        checkNotNull(session, "ServerToken is empty. Have you called setServerToken?");

        getOrCreateController().loadRideInformation(rideParameters);
    }

    /**
     * Set {@link Session}
     *
     * @param session to be used for request signing
     * @return this instance of {@link RideRequestButton}
     */
    public RideRequestButton setSession(@NonNull Session session) {
        this.session = session;
        return this;
    }

    private void setBackgroundAttributes(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes) {
        int attrsResources[] = {
                android.R.attr.background,
        };
        TypedArray backgroundAttributes = context.getTheme().obtainStyledAttributes(
                attrs,
                attrsResources,
                defStyleAttr,
                defStyleRes);
        try {
            applyBackgroundResource(backgroundAttributes);
        } finally {
            backgroundAttributes.recycle();
        }
    }

    private void setTextAttributes(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes) {
        int attrsResources[] = {
                android.R.attr.textColor,
                android.R.attr.gravity,
                android.R.attr.textSize,
                android.R.attr.textStyle,
                android.R.attr.text
        };
        TypedArray textAttributes = context.getTheme().obtainStyledAttributes(
                attrs,
                attrsResources,
                defStyleAttr,
                defStyleRes);
        try {
            applyTextAttributes(priceEstimateView, textAttributes);
            applyTextAttributes(timeEstimateView, textAttributes);
            applyTextAttributes(requestButton, textAttributes);
        } finally {
            textAttributes.recycle();
        }
    }

    @SuppressLint("ResourceType")
    private void applyTextAttributes(TextView textView, TypedArray textAttributes) {
        textView.setTextColor(textAttributes.getColor(0, Color.WHITE));
        textView.setTypeface(Typeface.defaultFromStyle(textAttributes.getInt(3, Typeface.NORMAL)));
        String text = textAttributes.getString(4);
        if (text != null) {
            textView.setText(textAttributes.getString(4));
        }
    }

    @SuppressLint("ResourceType")
    private void setPaddingAttributes(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes) {
        int attrsResources[] = {
                android.R.attr.padding,
                android.R.attr.paddingLeft,
                android.R.attr.paddingTop,
                android.R.attr.paddingRight,
                android.R.attr.paddingBottom,
        };
        TypedArray paddingAttributes = context.getTheme().obtainStyledAttributes(
                attrs,
                attrsResources,
                defStyleAttr,
                defStyleRes);
        try {
            int padding = paddingAttributes.getDimensionPixelOffset(0, 0);
            int paddingLeft = paddingAttributes.getDimensionPixelSize(1, 0);
            paddingLeft = paddingLeft == 0 ? padding : paddingLeft;
            int paddingTop = paddingAttributes.getDimensionPixelSize(2, 0);
            paddingTop = paddingTop == 0 ? padding : paddingTop;
            int paddingRight = paddingAttributes.getDimensionPixelSize(3, 0);
            paddingRight = paddingRight == 0 ? padding : paddingRight;
            int paddingBottom = paddingAttributes.getDimensionPixelSize(4, 0);
            paddingBottom = paddingBottom == 0 ? padding : paddingBottom;
            setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        } finally {
            paddingAttributes.recycle();
        }
    }

    private void applyBackgroundResource(TypedArray backgroundAttributes) {
        int color = Color.BLACK;
        if (backgroundAttributes.hasValue(0)) {
            int backgroundResource = backgroundAttributes.getResourceId(0, 0);
            if (backgroundResource != 0) {
                setBackgroundResource(backgroundResource);
                for (int i = 0; i < getChildCount(); i++) {
                    View childView = getChildAt(i);
                    if (!(childView instanceof LinearLayout)) {
                        childView.setBackgroundResource(backgroundResource);
                    }
                }
                return;
            } else {
                color = backgroundAttributes.getColor(0, Color.BLACK);
            }
        }

        setBackgroundColor(color);
        for (int i = 0; i < getChildCount(); i++) {
            View childView = getChildAt(i);
            childView.setBackgroundColor(color);
        }
    }

    private synchronized RideRequestButtonController getOrCreateController() {
        if (controller == null) {
            checkNotNull(session, "Must set session using setSession.");

            controller = new RideRequestButtonController(this, session, callback);
        }
        return controller;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (controller != null) {
            controller.destroy();
        }
    }

    @Override
    public void showDefaultView() {
        requestButton.setText(R.string.ub__ride_with_uber);
        priceEstimateView.setText("");
        priceEstimateView.setVisibility(GONE);

        timeEstimateView.setText("");
        timeEstimateView.setVisibility(GONE);
    }

    @Override
    public void showEstimate(@NonNull TimeEstimate timeEstimate) {
        requestButton.setText(R.string.ub__get_ride);

        timeEstimateView.setText(getResources().getString(R.string.ub__time_estimate,
                TimeUnit.SECONDS.toMinutes(timeEstimate.getEstimate())));
        timeEstimateView.setVisibility(VISIBLE);
    }

    @Override
    public void showEstimate(@NonNull TimeEstimate timeEstimate, @NonNull PriceEstimate priceEstimate) {
        showEstimate(timeEstimate);

        priceEstimateView.setText(getResources().getString(R.string.ub__price_estimate,
                priceEstimate.getEstimate(), priceEstimate.getDisplayName()));
        priceEstimateView.setVisibility(VISIBLE);
    }
}
