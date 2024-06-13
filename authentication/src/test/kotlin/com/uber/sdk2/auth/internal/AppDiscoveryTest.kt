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
