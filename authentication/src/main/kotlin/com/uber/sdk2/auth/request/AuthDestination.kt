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
package com.uber.sdk2.auth.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Represents the destination app to authenticate the user. */
@Parcelize
sealed class AuthDestination : Parcelable {
  /**
   * Authenticating within the same app by using a system webview, a.k.a Custom Tabs. If custom tabs
   * are unavailable the authentication flow will be launched in the system browser app.
   */
  data object InApp : AuthDestination()

  /**
   * Authenticating via one of the family of Uber apps using the Single Sign-On (SSO) flow in the
   * order of priority mentioned. If none of the apps are available we will fall back to the [InApp]
   * flow
   *
   * @param appPriority The order of the apps to use for the SSO flow. Defaults to
   *   [CrossApp.Rider, CrossApp.Eats, CrossApp.Driver] priority
   */
  data class CrossAppSso(val appPriority: List<CrossApp> = DEFAULT_APP_PRIORITY) :
    AuthDestination()

  companion object {
    /**
     * Default app priority to use for the SSO flow. The order of the apps to use for the SSO flow
     */
    private val DEFAULT_APP_PRIORITY = listOf(CrossApp.Rider, CrossApp.Eats, CrossApp.Driver)
  }
}
