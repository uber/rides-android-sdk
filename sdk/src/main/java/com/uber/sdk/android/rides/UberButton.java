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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;

/**
 * {@link android.widget.Button} that can be used as a button and provides default Uber styling.
 */
public class UberButton extends Button {

    private static final int DEFAULT_TEXT_SIZE = 18;

    /**
     * Constructor.
     *
     * @param context the context creating the view.
     */
    public UberButton(Context context) {
        this(context, null);
    }

    /**
     * Constructor.
     *
     * @param context the context creating the view.
     * @param attrs attributes for the view.
     */
    public UberButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor.
     *
     * @param context the context creating the view.
     * @param attrs attributes for the view.
     * @param defStyleAttr the default attribute to use for a style if none is specified.
     */
    public UberButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.UberButton);
    }

    /**
     * Constructor.
     *
     * @param context the context creating the view.
     * @param attrs attributes for the view.
     * @param defStyleAttr the default attribute to use for a style if none is specified.
     * @param defStyleRes the default style, used only if defStyleAttr is 0 or can not be found in the theme.
     */
    public UberButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, defStyleRes == 0 ? R.style.UberButton : defStyleRes);
    }

    protected void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        setBackgroundAttributes(context, attrs, defStyleAttr, defStyleRes);
        setDrawableAttributes(context, attrs, defStyleAttr, defStyleRes);
        setPaddingAttributes(context, attrs, defStyleAttr, defStyleRes);
        setTextAttributes(context, attrs, defStyleAttr, defStyleRes);
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
            if (backgroundAttributes.hasValue(0)) {
                int backgroundResource = backgroundAttributes.getResourceId(0, 0);
                if (backgroundResource != 0) {
                    setBackgroundResource(backgroundResource);
                } else {
                    setBackgroundColor(backgroundAttributes.getColor(0, Color.BLACK));
                }
            } else {
                setBackgroundColor(backgroundAttributes.getColor(0, Color.BLACK));
            }
        } finally {
            backgroundAttributes.recycle();
        }
    }

    private void setDrawableAttributes(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes) {
        int attrsResources[] = {
                android.R.attr.drawableLeft,
                android.R.attr.drawableTop,
                android.R.attr.drawableRight,
                android.R.attr.drawableBottom,
                android.R.attr.drawablePadding,
        };
        TypedArray drawableAttributes = context.getTheme().obtainStyledAttributes(
                attrs,
                attrsResources,
                defStyleAttr,
                defStyleRes);
        try {
            setCompoundDrawablesWithIntrinsicBounds(
                    drawableAttributes.getResourceId(0, 0),
                    drawableAttributes.getResourceId(1, 0),
                    drawableAttributes.getResourceId(2, 0),
                    drawableAttributes.getResourceId(3, 0));
            setCompoundDrawablePadding(drawableAttributes.getDimensionPixelSize(4, 0));
        } finally {
            drawableAttributes.recycle();
        }
    }

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
                android.R.attr.text,
        };
        TypedArray textAttributes = context.getTheme().obtainStyledAttributes(
                attrs,
                attrsResources,
                defStyleAttr,
                defStyleRes);
        try {
            setTextColor(textAttributes.getColor(0, Color.WHITE));
            setGravity(textAttributes.getInt(1, Gravity.CENTER));
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, textAttributes.getDimensionPixelSize(2, DEFAULT_TEXT_SIZE));
            setTypeface(Typeface.defaultFromStyle(textAttributes.getInt(3, Typeface.NORMAL)));
            String text = textAttributes.getString(4);
            if (text != null) {
                setText(textAttributes.getString(4));
            }
        } finally {
            textAttributes.recycle();
        }
    }
}
