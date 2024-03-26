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
