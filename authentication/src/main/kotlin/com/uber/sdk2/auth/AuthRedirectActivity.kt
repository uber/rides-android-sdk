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
import android.os.Bundle

class AuthRedirectActivity : Activity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    // while this does not appear to be achieving much, handling the redirect in this way
    // ensures that we can remove the browser tab from the back stack. See the documentation
    // on AuthorizationManagementActivity for more details.
    startActivity(AuthActivity.newResponseIntent(this, intent.data))
    finish()
  }
}
