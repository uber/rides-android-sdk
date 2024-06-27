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

package com.uber.sdk.android.core;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.annotation.StyleableRes;
import androidx.annotation.VisibleForTesting;
import android.util.AttributeSet;

public enum UberStyle {
    /**
     * Black background, white text. This is the default.
     */
    BLACK(0),

    /**
     * White background, black text.
     */
    WHITE(1);

    @VisibleForTesting
    public static UberStyle DEFAULT = BLACK;

    private int intValue;

    UberStyle(int value) {
        this.intValue = value;
    }

    /**
     * If the value is not found returns default Style.
     */
    @NonNull
    static UberStyle fromInt(int enumValue) {
        for (UberStyle style : values()) {
            if (style.getValue() == enumValue) {
                return style;
            }
        }

        return DEFAULT;
    }

    public int getValue() {
        return intValue;
    }

    public static UberStyle getStyleFromAttribute(
            @NonNull Context context,
            @Nullable AttributeSet attributeSet,
            @StyleRes int defStyleRes,
            @StyleableRes int[] styleableMain,
            @StyleableRes int styleable) {

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attributeSet,
                styleableMain, 0, defStyleRes);
        try {
            int style = typedArray.getInt(styleable, UberStyle.DEFAULT.getValue());
            return UberStyle.fromInt(style);
        } finally {
            typedArray.recycle();
        }
    }
}

