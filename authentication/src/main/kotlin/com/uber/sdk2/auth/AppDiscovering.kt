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
