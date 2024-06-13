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
package com.uber.sdk2.core.ui.legacy

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.annotation.VisibleForTesting
import androidx.appcompat.widget.AppCompatButton
import com.uber.sdk2.core.R
import com.uber.sdk2.core.ui.UberStyle

/** [android.widget.Button] that can be used as a button and provides default Uber styling. */
open class UberButton
@JvmOverloads
constructor(
  context: Context,
  attrs: AttributeSet? = null,
  defStyleAttr: Int = 0,
  defStyleRes: Int = R.style.UberButton,
) : AppCompatButton(context, attrs, defStyleAttr) {
  @DrawableRes private var backgroundResource = 0

  /**
   * Constructor.
   *
   * @param context the context creating the view.
   * @param attrs attributes for the view.
   * @param defStyleAttr the default attribute to use for a style if none is specified.
   * @param defStyleRes the default style, used only if defStyleAttr is 0 or can not be found in the
   *   theme.
   */
  /**
   * Constructor.
   *
   * @param context the context creating the view.
   * @param attrs attributes for the view.
   * @param defStyleAttr the default attribute to use for a style if none is specified.
   */
  /**
   * Constructor.
   *
   * @param context the context creating the view.
   * @param attrs attributes for the view.
   */
  /**
   * Constructor.
   *
   * @param context the context creating the view.
   */
  init {
    val uberStyle =
      UberStyle.getStyleFromAttribute(
        context,
        attrs,
        defStyleRes,
        R.styleable.UberButton,
        R.styleable.UberButton_ub__style,
      )
    init(context, 0, attrs, defStyleAttr, uberStyle)
  }

  protected open fun init(
    context: Context,
    @StringRes defaultText: Int,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    uberStyle: UberStyle,
  ) {
    val defStyleRes = STYLES[uberStyle.value]

    applyStyle(context, defaultText, attrs, defStyleAttr, defStyleRes)
  }

  protected fun applyStyle(
    context: Context,
    @StringRes defaultText: Int,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    @StyleRes defStyleRes: Int,
  ) {
    setBackgroundAttributes(context, attrs, defStyleAttr, defStyleRes)
    setDrawableAttributes(context, attrs, defStyleAttr, defStyleRes)
    setPaddingAttributes(context, attrs, defStyleAttr, defStyleRes)
    setTextAttributes(context, attrs, defStyleAttr, defStyleRes)
    if (defaultText != 0) {
      setText(defaultText)
    }
  }

  protected val activity: Activity
    get() {
      var context = context
      while (context !is Activity && context is ContextWrapper) {
        context = context.baseContext
      }

      if (context is Activity) {
        return context
      }

      throw IllegalStateException("Button is not attached to an activity.")
    }

  @VisibleForTesting
  @DrawableRes
  fun getBackgroundResource(): Int {
    return backgroundResource
  }

  override fun setBackgroundResource(@DrawableRes backgroundResource: Int) {
    this.backgroundResource = backgroundResource
    super.setBackgroundResource(backgroundResource)
  }

  private fun setBackgroundAttributes(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
  ) {
    val attrsResources = intArrayOf(android.R.attr.background)
    @SuppressLint("ResourceType")
    val backgroundAttributes =
      context.theme.obtainStyledAttributes(attrs, attrsResources, defStyleAttr, defStyleRes)
    try {
      if (backgroundAttributes.hasValue(0)) {
        val backgroundResource = backgroundAttributes.getResourceId(0, 0)
        if (backgroundResource != 0) {
          setBackgroundResource(backgroundResource)
        } else {
          setBackgroundColor(backgroundAttributes.getColor(0, Color.BLACK))
        }
      } else {
        setBackgroundColor(backgroundAttributes.getColor(0, Color.BLACK))
      }
    } finally {
      backgroundAttributes.recycle()
    }
  }

  @SuppressLint("ResourceType")
  private fun setDrawableAttributes(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
  ) {
    val attrsResources =
      intArrayOf(
        android.R.attr.drawableLeft,
        android.R.attr.drawableTop,
        android.R.attr.drawableRight,
        android.R.attr.drawableBottom,
        android.R.attr.drawablePadding,
      )
    val drawableAttributes =
      context.theme.obtainStyledAttributes(attrs, attrsResources, defStyleAttr, defStyleRes)
    try {
      setCompoundDrawablesWithIntrinsicBounds(
        drawableAttributes.getResourceId(0, 0),
        drawableAttributes.getResourceId(1, 0),
        drawableAttributes.getResourceId(2, 0),
        drawableAttributes.getResourceId(3, 0),
      )
      compoundDrawablePadding = drawableAttributes.getDimensionPixelSize(4, 0)
    } finally {
      drawableAttributes.recycle()
    }
  }

  @SuppressLint("ResourceType")
  private fun setPaddingAttributes(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
  ) {
    val attrsResources =
      intArrayOf(
        android.R.attr.padding,
        android.R.attr.paddingLeft,
        android.R.attr.paddingTop,
        android.R.attr.paddingRight,
        android.R.attr.paddingBottom,
      )
    val paddingAttributes =
      context.theme.obtainStyledAttributes(attrs, attrsResources, defStyleAttr, defStyleRes)
    try {
      val padding = paddingAttributes.getDimensionPixelOffset(0, 0)
      var paddingLeft = paddingAttributes.getDimensionPixelSize(1, 0)
      paddingLeft = if (paddingLeft == 0) padding else paddingLeft
      var paddingTop = paddingAttributes.getDimensionPixelSize(2, 0)
      paddingTop = if (paddingTop == 0) padding else paddingTop
      var paddingRight = paddingAttributes.getDimensionPixelSize(3, 0)
      paddingRight = if (paddingRight == 0) padding else paddingRight
      var paddingBottom = paddingAttributes.getDimensionPixelSize(4, 0)
      paddingBottom = if (paddingBottom == 0) padding else paddingBottom
      setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
    } finally {
      paddingAttributes.recycle()
    }
  }

  @SuppressLint("ResourceType")
  private fun setTextAttributes(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
  ) {
    val attrsResources =
      intArrayOf(
        android.R.attr.textColor,
        android.R.attr.gravity,
        android.R.attr.textStyle,
        android.R.attr.text,
      )
    val textAttributes =
      context.theme.obtainStyledAttributes(attrs, attrsResources, defStyleAttr, defStyleRes)
    try {
      setTextColor(textAttributes.getColor(0, Color.WHITE))
      gravity = textAttributes.getInt(1, Gravity.CENTER)
      typeface = Typeface.defaultFromStyle(textAttributes.getInt(2, Typeface.NORMAL))
      val text = textAttributes.getString(3)
      if (text != null) {
        setText(text)
      }
    } finally {
      textAttributes.recycle()
    }
  }

  companion object {
    @StyleRes private val STYLES = intArrayOf(R.style.UberButton, R.style.UberButton_White)
  }
}
