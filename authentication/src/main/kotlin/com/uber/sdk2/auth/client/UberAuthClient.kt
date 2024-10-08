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
package com.uber.sdk2.auth.client

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.uber.sdk2.auth.request.AuthContext

/**
 * Client to authenticate the user against one of the available Uber apps. It is entrypoint for the
 * consumers to initiate the authentication flow
 */
interface UberAuthClient {
  /**
   * Authenticate the user against one of the available Uber apps. If no app is available it will
   * fallback to using a system webview to launch the authentication flow on web.
   *
   * @param authContext Context of the authentication request
   * @param activity Activity to launch the authentication flow
   */
  fun authenticate(activity: Activity, authContext: AuthContext)

  /**
   * Authenticate the user against one of the available Uber apps. If no app is available it will
   * fallback to using a system webview, a.k.a. custom tabs to launch the authentication flow on
   * web.
   *
   * @param context Context to launch the authentication flow
   * @param activityResultLauncher ActivityResultLauncher to launch the authentication flow
   * @param authContext Context of the authentication request
   */
  fun authenticate(
    context: Context,
    activityResultLauncher: ActivityResultLauncher<Intent>,
    authContext: AuthContext,
  )
}
