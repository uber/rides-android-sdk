/**
 * Copyright (c) 2024 Uber Technologies, Inc.
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
package com.uber.sdk2.core.ui

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.annotation.VisibleForTesting

enum class UberStyle(val value: Int) {
  /** Black background, white text. This is the default. */
  BLACK(0),

  /** White background, black text. */
  WHITE(1);

  companion object {
    @VisibleForTesting var DEFAULT: UberStyle = BLACK

    /** If the value is not found returns default Style. */
    fun fromInt(enumValue: Int): UberStyle {
      for (style in entries) {
        if (style.value == enumValue) {
          return style
        }
      }

      return DEFAULT
    }

    fun getStyleFromAttribute(
      context: Context,
      attributeSet: AttributeSet?,
      @StyleRes defStyleRes: Int,
      @StyleableRes styleableMain: IntArray?,
      @StyleableRes styleable: Int,
    ): UberStyle {
      val typedArray =
        context.theme.obtainStyledAttributes(attributeSet, styleableMain!!, 0, defStyleRes)
      try {
        val style = typedArray.getInt(styleable, DEFAULT.value)
        return fromInt(style)
      } finally {
        typedArray.recycle()
      }
    }
  }
}
