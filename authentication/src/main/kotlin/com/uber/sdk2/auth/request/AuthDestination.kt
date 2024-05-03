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
