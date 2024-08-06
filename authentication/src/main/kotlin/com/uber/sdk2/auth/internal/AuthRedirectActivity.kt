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
package com.uber.sdk2.auth.internal

import android.app.Activity
import android.os.Bundle

/**
 * Activity that handles the redirect from the browser after the user has authenticated. While this
 * does not appear to be achieving much, handling the redirect in this way ensures that we can
 * remove the browser tab from the back stack. See AuthorizationManagementActivity in App-Auth
 * public repo for more details.
 */
class AuthRedirectActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    startActivity(AuthActivity.newResponseIntent(this, intent.data))
    finish()
  }
}
