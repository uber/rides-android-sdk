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
package com.uber.sdk2.auth.internal

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.uber.sdk2.auth.AppDiscovering
import com.uber.sdk2.auth.request.CrossApp

/**
 * Default implementation of [AppDiscovering]. This implementation uses the [PackageManager] to find
 * the best app to handle the given [Uri].
 */
class AppDiscovery(val context: Context) : AppDiscovering {
  override fun findAppForSso(uri: Uri, appPriority: Iterable<CrossApp>): String? {
    val intent = Intent(Intent.ACTION_VIEW, uri)

    // Use PackageManager to find activities that can handle the Intent
    val packageManager = context.packageManager
    val appsList = packageManager.queryIntentActivities(intent, 0)

    // Extract the package names from the ResolveInfo objects and return them
    val packageNames = appsList.map { it.activityInfo.packageName }

    // Find the first package in appPriority that is in packageNames

    return appPriority.flatMap { it.packages }.firstOrNull { it in packageNames }
  }
}
