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
package com.uber.sdk2.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.uber.sdk2.auth.client.UberAuthClient
import com.uber.sdk2.auth.internal.AuthActivity
import com.uber.sdk2.auth.request.AuthContext

/** Implementation of [UberAuthClient] that uses the [AuthActivity] to authenticate the user. */
class UberAuthClientImpl : UberAuthClient {

  override fun authenticate(activity: Activity, authContext: AuthContext) {
    val intent = AuthActivity.newIntent(activity, authContext)
    activity.startActivityForResult(intent, UBER_AUTH_REQUEST_CODE)
  }

  override fun authenticate(
    context: Context,
    activityResultLauncher: ActivityResultLauncher<Intent>,
    authContext: AuthContext,
  ) {
    val intent = AuthActivity.newIntent(context, authContext)
    activityResultLauncher.launch(intent)
  }

  companion object {
    /** Request code for the authentication flow used when launching the [AuthActivity]. */
    const val UBER_AUTH_REQUEST_CODE = 1001
  }
}
