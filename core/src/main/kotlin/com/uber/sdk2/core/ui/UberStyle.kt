/*
 * Copyright (C) 2024. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
