/*
 * Copyright (C) 2016. Uber Technologies
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
package com.uber.sdk2.auth.ui

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.annotation.VisibleForTesting
import com.uber.sdk2.auth.R
import com.uber.sdk2.auth.UberAuthClientImpl
import com.uber.sdk2.auth.request.AuthContext
import com.uber.sdk2.core.ui.UberButton
import com.uber.sdk2.core.ui.UberStyle

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
