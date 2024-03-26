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
package com.uber.sdk2.auth.internal.sso

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.uber.sdk2.auth.sso.CustomTabsLauncher
import com.uber.sdk2.core.utils.CustomTabsHelper

/** Default implementation of [CustomTabsLauncher]. */
class CustomTabsLauncherImpl(private val context: Context) : CustomTabsLauncher {
  /** Launches a custom tab with the given [uri]. */
  override fun launch(uri: Uri) {
    val intent = CustomTabsIntent.Builder().build()
    CustomTabsHelper.openCustomTab(context, intent, uri, CustomTabsHelper.BrowserFallback())
  }
}
