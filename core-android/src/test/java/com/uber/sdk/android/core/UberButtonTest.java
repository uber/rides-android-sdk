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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;

import org.apache.maven.artifact.ant.shaded.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link UberButton}
 */
public class UberButtonTest extends RobolectricTestBase {

    private static final String ANDROID_COLOR_BLACK = "@android:color/black";
    private static final String ANDROID_COLOR_WHITE = "@android:color/white";
    private static final String DRAWABLE_UBER_BADGE = "@drawable/uber_badge";
    private static final String GRAVITY_END = "end";
    private static final String STYLE_ITALIC = "italic";
    private static final String ONE_SP = "1sp";
    private static final String TWO_SP = "2sp";
    private static final String THREE_SP = "3sp";
    private static final String FOUR_SP = "4sp";
    private static final String TEXT = "test";

    private static final String UBER_PACKAGE_NAME = "com.uber.sdk.android.core";

    private Context context;

    @Before
    public void setup() {
        context = Robolectric.buildActivity(Activity.class).create().get();
    }

    @Test
    public void onCreate_whenBackgroundAttributeSet_shouldSetBackground() {
        AttributeSet attributeSet = Robolectric.buildAttributeSet()
                .addAttribute(android.R.attr.background, ANDROID_COLOR_WHITE)
                .build();
        UberButton uberButton = new UberButton(context, attributeSet, 0, 0) { };
        assertEquals(Color.WHITE, ((ColorDrawable) uberButton.getBackground()).getColor());
    }

    @Test
    public void onCreate_whenCompoundDrawablesAndPaddingSet_shouldSetCompoundDrawableAttributes() {
        AttributeSet attributeSet = Robolectric.buildAttributeSet()
                .addAttribute(android.R.attr.drawableLeft, DRAWABLE_UBER_BADGE)
                .addAttribute(android.R.attr.drawableTop, DRAWABLE_UBER_BADGE)
                .addAttribute(android.R.attr.drawableRight, DRAWABLE_UBER_BADGE)
                .addAttribute(android.R.attr.drawableBottom, DRAWABLE_UBER_BADGE)
                .addAttribute(android.R.attr.drawablePadding, ONE_SP)
                .build();

        UberButton uberButton = new UberButton(context, attributeSet, 0, 0) { };
        Drawable[] drawables = uberButton.getCompoundDrawables();
        assertNotNull(drawables[0]);
        assertNotNull(drawables[1]);
        assertNotNull(drawables[2]);
        assertNotNull(drawables[3]);
        assertEquals(1, uberButton.getCompoundDrawablePadding());
    }

    @Test
    public void onCreate_whenOverallPaddingSet_shouldAddOverallPadding() {
        AttributeSet attributeSet = Robolectric.buildAttributeSet()
                .addAttribute(android.R.attr.padding, ONE_SP)
                .build();
        UberButton uberButton = new UberButton(context, attributeSet, 0, 0) { };
        assertEquals(1, uberButton.getPaddingLeft());
        assertEquals(1, uberButton.getPaddingTop());
        assertEquals(1, uberButton.getPaddingRight());
        assertEquals(1, uberButton.getPaddingBottom());
    }

    @Test
    public void onCreate_whenIndividualPaddingsSet_shouldHaveSeparatePaddings() {
        AttributeSet attributeSet = Robolectric.buildAttributeSet()
                .addAttribute(android.R.attr.paddingLeft, ONE_SP)
                .addAttribute(android.R.attr.paddingTop, TWO_SP)
                .addAttribute(android.R.attr.paddingRight, THREE_SP)
                .addAttribute(android.R.attr.paddingBottom, FOUR_SP)
                .build();

        UberButton uberButton = new UberButton(context, attributeSet, 0, 0) { };
        assertEquals(1, uberButton.getPaddingLeft());
        assertEquals(2, uberButton.getPaddingTop());
        assertEquals(3, uberButton.getPaddingRight());
        assertEquals(4, uberButton.getPaddingBottom());
    }

    @Test
    public void onCreate_whenIndividualAndOverallPaddingsSet_shouldHaveIndividualPaddingsTrumpOverall() {
        AttributeSet attributeSet = Robolectric.buildAttributeSet()
                .addAttribute(android.R.attr.padding, ONE_SP)
                .addAttribute(android.R.attr.paddingTop, TWO_SP)
                .addAttribute(android.R.attr.paddingBottom, FOUR_SP)
                .build();

        UberButton uberButton = new UberButton(context, attributeSet, 0, 0) { };
        assertEquals(1, uberButton.getPaddingLeft());
        assertEquals(2, uberButton.getPaddingTop());
        assertEquals(1, uberButton.getPaddingRight());
        assertEquals(4, uberButton.getPaddingBottom());
    }

    @Test
    public void onCreate_whenTextAttributesSet_shouldAddAllAttributes() {
        AttributeSet attributeSet = Robolectric.buildAttributeSet()
                .addAttribute(android.R.attr.textColor, ANDROID_COLOR_BLACK)
                .addAttribute(android.R.attr.gravity, GRAVITY_END)
                .addAttribute(android.R.attr.textSize, FOUR_SP)
                .addAttribute(android.R.attr.textStyle, STYLE_ITALIC)
                .addAttribute(android.R.attr.text, TEXT)
                .build();

        UberButton uberButton = new UberButton(context, attributeSet, 0, 0) { };
        assertEquals(Color.BLACK, uberButton.getCurrentTextColor());
        assertEquals(Typeface.ITALIC, uberButton.getTypeface().getStyle());
        assertEquals(4, uberButton.getTextSize(), 0);
        assertEquals(TEXT, uberButton.getText().toString());
        assertTrue(uberButton.getGravity() != 0);
    }

    @Test
    public void onCreate_whenNoAttributesSet_shouldUseUberButtonDefaults() {
        UberButton uberButton = new UberButton(context, null, 0, 0) { };
        Resources resources = context.getResources();

        assertEquals(R.drawable.uber_button_background_selector_black,
                uberButton.getBackgroundResource());

        assertNull(uberButton.getCompoundDrawables()[0]);
        assertNull(uberButton.getCompoundDrawables()[1]);
        assertNull(uberButton.getCompoundDrawables()[2]);
        assertNull(uberButton.getCompoundDrawables()[3]);

        assertEquals(0, uberButton.getPaddingLeft(), 0);
        assertEquals(0, uberButton.getPaddingTop(), 0);
        assertEquals(0, uberButton.getPaddingRight(), 0);
        assertEquals(0, uberButton.getPaddingBottom(), 0);

        assertEquals(resources.getColor(R.color.uber_white), uberButton.getCurrentTextColor());
        assertEquals(Typeface.NORMAL, uberButton.getTypeface().getStyle());
        assertTrue(StringUtils.isEmpty(uberButton.getText().toString()));
    }

    @Test
    public void onCreate_whenUberStyleSet_shouldUseUberStyle() {
        AttributeSet attributeSet = Robolectric.buildAttributeSet()
                .addAttribute(R.attr.ub__style, "white")
                .build();

        UberButton uberButton = new UberButton(context, attributeSet, 0, R.style.UberButton_White) { };
        Resources resources = context.getResources();

        assertEquals(R.drawable.uber_button_background_selector_white, uberButton.getBackgroundResource());

        assertNull(uberButton.getCompoundDrawables()[0]);
        assertNull(uberButton.getCompoundDrawables()[1]);
        assertNull(uberButton.getCompoundDrawables()[2]);
        assertNull(uberButton.getCompoundDrawables()[3]);

        assertEquals(0, uberButton.getPaddingLeft(), 0);
        assertEquals(0, uberButton.getPaddingTop(), 0);
        assertEquals(0, uberButton.getPaddingRight(), 0);
        assertEquals(0, uberButton.getPaddingBottom(), 0);

        assertEquals(resources.getColor(R.color.uber_black), uberButton.getCurrentTextColor());
        assertEquals(Typeface.NORMAL, uberButton.getTypeface().getStyle());
        assertTrue(uberButton.getGravity() != 0);
        assertTrue(StringUtils.isEmpty(uberButton.getText().toString()));
    }

    @Test
    public void getActivity_whenAttachedToActivity_shouldReturnActivity() {
        final Activity testActivity = Robolectric.setupActivity(Activity.class);

        final UberButton uberButton = new UberButton(testActivity, null, 0, 0) { };

        assertThat(uberButton.getActivity()).isSameAs(testActivity);
    }

    @Test
    public void getActivity_whenContextWrapper_shouldReturnActivity() {
        final Activity testActivity = Robolectric.setupActivity(Activity.class);
        final ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(testActivity, 0);

        UberButton uberButton = new UberButton(contextThemeWrapper, null, 0, 0) { };

        assertThat(uberButton.getActivity()).isSameAs(testActivity);
    }

    @Test
    public void getActivity_whenContextWrapperNested_shouldReturnActivity() {
        final Activity testActivity = Robolectric.setupActivity(Activity.class);
        final ContextThemeWrapper nestedWrapper = new ContextThemeWrapper(testActivity, 0);
        final ContextThemeWrapper wrapper = new ContextThemeWrapper(nestedWrapper, 0);

        UberButton uberButton = new UberButton(wrapper, null, 0, 0) { };

        assertThat(uberButton.getActivity()).isSameAs(testActivity);
    }

    @Test(expected = IllegalStateException.class)
    public void getActivity_whenContextIsNotActivity_shouldThrowException() {
        final ContextThemeWrapper wrapper = new ContextThemeWrapper(RuntimeEnvironment.application, 0);

        UberButton uberButton = new UberButton(wrapper, null, 0, 0) { };

        uberButton.getActivity();
    }
}
