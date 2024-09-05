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
