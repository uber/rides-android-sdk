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
package com.uber.sdk2.auth.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Provides different apps that could be used for authentication using SSO flow.
 *
 * @param packages The list of packages that could be used for authentication.
 */
@Parcelize
sealed class CrossApp(val packages: List<String>) : Parcelable {
  /** The Eats app. */
  data object Eats : CrossApp(EATS_APPS)

  /** The Rider app. */
  data object Rider : CrossApp(RIDER_APPS)

  /** The Driver app. */
  data object Driver : CrossApp(DRIVER_APPS)

  companion object {
    /** The list of all driver apps. */
    private val DRIVER_APPS =
      listOf("com.ubercab.driver", "com.ubercab.driver.debug", "com.ubercab.driver.internal")

    /** The list of all rider apps. */
    private val RIDER_APPS =
      listOf("com.ubercab", "com.ubercab.presidio.development", "com.ubercab.rider.internal")

    /** The list of all Eats apps. */
    private val EATS_APPS =
      listOf("com.ubercab.eats", "com.ubercab.eats.debug", "com.ubercab.eats.internal")
  }
}
