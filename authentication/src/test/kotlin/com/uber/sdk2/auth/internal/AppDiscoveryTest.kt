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
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import com.uber.sdk2.auth.RobolectricTestBase
import com.uber.sdk2.auth.request.CrossApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AppDiscoveryTest : RobolectricTestBase() {
  private val context: Context = mock()
  private val packageManager: PackageManager = mock()
  private val appDiscovery = AppDiscovery(context)

  private val riderAppResolveInfoList =
    CrossApp.Rider.packages.map {
      ResolveInfo().apply { activityInfo = ActivityInfo().apply { this.packageName = it } }
    }

  private val driverAppResolveInfoList =
    CrossApp.Driver.packages.map {
      ResolveInfo().apply { activityInfo = ActivityInfo().apply { packageName = it } }
    }

  @Before
  fun setup() {
    whenever(context.packageManager).thenReturn(packageManager)
  }

  @Test
  fun `findAppForSso when app priority is defined but no app is found should return null`() {
    val uri = Uri.parse("https://auth.uber.com/authorize")
    val appPriority = listOf(CrossApp.Rider, CrossApp.Eats)

    whenever(context.packageManager.queryIntentActivities(any(), anyInt())).thenReturn(emptyList())
    val result = appDiscovery.findAppForSso(uri, appPriority)

    assertNull(result)
  }

  @Test
  fun `findAppForSso when app priority is defined and app is found should return app package name`() {
    val uri = Uri.parse("https://auth.uber.com/authorize")
    val appPriority = listOf(CrossApp.Rider, CrossApp.Eats)
    whenever(context.packageManager).thenReturn(packageManager)
    whenever(packageManager.queryIntentActivities(any(), anyInt()))
      .thenReturn(riderAppResolveInfoList)

    val result = appDiscovery.findAppForSso(uri, appPriority)

    assertEquals("com.ubercab", result)
  }

  @Test
  fun `findAppForSso when app priority is defined and apps available but not found should return null`() {
    val uri = Uri.parse("https://auth.uber.com/authorize")
    val appPriority = listOf(CrossApp.Rider, CrossApp.Eats)
    whenever(context.packageManager).thenReturn(packageManager)
    whenever(packageManager.queryIntentActivities(any(), anyInt()))
      .thenReturn(driverAppResolveInfoList)

    val result = appDiscovery.findAppForSso(uri, appPriority)

    assertNull(result)
  }
}
