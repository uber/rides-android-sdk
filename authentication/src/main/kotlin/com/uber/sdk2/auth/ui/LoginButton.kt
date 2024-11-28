/**
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
package com.uber.sdk2.auth.ui

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.annotation.VisibleForTesting
import com.uber.sdk2.auth.R
import com.uber.sdk2.auth.UberAuthClientImpl
import com.uber.sdk2.auth.request.AuthContext
import com.uber.sdk2.core.ui.UberStyle
import com.uber.sdk2.core.ui.legacy.UberButton

/** The [LoginButton] is used to initiate the Uber SDK Login flow. */
class LoginButton : UberButton {
  private var authContext: AuthContext? = null

  constructor(context: Context) : super(context)

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

  constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
  ) : super(context, attrs, defStyleAttr)

  constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
  ) : super(context, attrs, defStyleAttr, defStyleRes)

  override fun init(
    context: Context,
    @StringRes defaultText: Int,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    uberStyle: UberStyle,
  ) {
    isAllCaps = true

    val defStyleRes = STYLES[uberStyle.value]

    applyStyle(context, R.string.ub__sign_in, attrs, defStyleAttr, defStyleRes)

    setOnClickListener { login() }
  }

  @VisibleForTesting
  fun login() {
    val activity = activity
    UberAuthClientImpl().authenticate(activity, authContext!!)
  }

  /**
   * A [AuthContext] is required to identify the app being authenticated.
   *
   * @param authContext to be identified.
   * @return this instance of [LoginButton]
   */
  fun authContext(authContext: AuthContext): LoginButton {
    this.authContext = authContext
    return this
  }

  companion object {
    @StyleRes
    private val STYLES = intArrayOf(R.style.UberButton_Login, R.style.UberButton_Login_White)
  }
}
