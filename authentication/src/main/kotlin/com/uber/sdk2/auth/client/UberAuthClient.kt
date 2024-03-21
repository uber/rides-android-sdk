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
package com.uber.sdk2.auth.client

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.uber.sdk2.auth.AuthActivity
import com.uber.sdk2.auth.api.request.AuthContext

object UberAuthClient {

  const val UBER_AUTH_REQUEST_CODE = 1001

  /**
   * Initializes the UberAuthClient. This method should be called before any other method in the
   * UberAuthClient. This method should be called only once. It is the entry point into the sdk to
   * setup initial configuration and pre-load any necessary resources.
   */
  fun init(context: Context) {
    // Initialize the UberAuthClient
  }

  /**
   * Authenticate the user against one of the available Uber apps. If no app is available it will
   * fallback to using a system webview to launch the authentication flow on web.
   *
   * @param authContext Context of the authentication request
   */
  fun authenticate(activity: Activity, authContext: AuthContext) {
    val intent = AuthActivity.newIntent(activity, authContext)
    activity.startActivityForResult(intent, UBER_AUTH_REQUEST_CODE)
  }

  fun authenticate(
    context: Context,
    activityResultLauncher: ActivityResultLauncher<Intent>,
    authContext: AuthContext,
  ) {
    val intent = AuthActivity.newIntent(context, authContext)
    activityResultLauncher.launch(intent)
  }
}
