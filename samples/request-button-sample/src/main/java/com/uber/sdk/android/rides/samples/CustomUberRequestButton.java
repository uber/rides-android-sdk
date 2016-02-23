package com.uber.sdk.android.rides.samples;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.uber.sdk.android.rides.RequestButton;

public class CustomUberRequestButton extends RequestButton implements View.OnClickListener {
    private static final String TAG = CustomUberRequestButton.class.getSimpleName();

    public CustomUberRequestButton(Context context) {
        super(context);
    }

    public CustomUberRequestButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomUberRequestButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "Custom code before calling UberRequestButton's onClick() listener");

        // Explicitly need to call UberRequestButton onClick() listener
        super.onClick(v);
    }
}
