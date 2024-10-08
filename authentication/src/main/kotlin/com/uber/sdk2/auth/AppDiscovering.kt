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
package com.uber.sdk2.auth

import android.net.Uri
import com.uber.sdk2.auth.request.CrossApp

/** Provides a way to discover the app to authenticate the user. */
fun interface AppDiscovering {

  /**
   * Finds the best application to handle a given [Uri].
   *
   * This function searches through available applications on the device to determine the most
   * suitable app that can handle the specified [Uri].
   *
   * @param uri The [Uri] for which the best application needs to be found. The [Uri] should be
   *   well-formed and include a scheme (e.g., http, https) that applications can recognize and
   *   handle.
   * @return The package name of the best application to handle the given [Uri], or `null` if no app
   *   is found.
   */
  fun findAppForSso(uri: Uri, appPriority: Iterable<CrossApp>): String?
}
