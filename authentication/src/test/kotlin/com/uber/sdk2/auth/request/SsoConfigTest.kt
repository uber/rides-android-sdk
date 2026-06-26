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

import android.content.Context
import android.content.res.Resources
import com.uber.sdk2.auth.RobolectricTestBase
import java.io.ByteArrayInputStream
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SsoConfigTest : RobolectricTestBase() {

  @Test
  fun `getSsoConfig returns valid config`() {
    val context = mock<Context>()
    val resources = mock<Resources>()
    val resourceId = 1
    val configJsonString =
      """{"client_id":"testClientId","redirect_uri":"testRedirectUri","scope":"testScope"}"""

    whenever(context.resources).thenReturn(resources)
    whenever(context.packageName).thenReturn("com.uber.sdk2.auth")
    whenever(
        resources.getIdentifier(
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
        )
      )
      .thenReturn(resourceId)
    whenever(resources.openRawResource(ArgumentMatchers.anyInt()))
      .thenReturn(ByteArrayInputStream(configJsonString.toByteArray()))

    val result = SsoConfigProvider.getSsoConfig(context)

    Assert.assertEquals("testClientId", result.clientId)
    Assert.assertEquals("testRedirectUri", result.redirectUri)
    Assert.assertEquals("testScope", result.scope)
  }

  @Test
  fun `getSsoConfig returns valid config with no scope`() {
    val context = mock<Context>()
    val resources = mock<Resources>()
    val resourceId = 1
    val configJsonString = """{"client_id":"testClientId","redirect_uri":"testRedirectUri"}"""

    whenever(context.resources).thenReturn(resources)
    whenever(context.packageName).thenReturn("com.uber.sdk2.auth")
    whenever(
        resources.getIdentifier(
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
        )
      )
      .thenReturn(resourceId)
    whenever(resources.openRawResource(ArgumentMatchers.anyInt()))
      .thenReturn(ByteArrayInputStream(configJsonString.toByteArray()))

    val result = SsoConfigProvider.getSsoConfig(context)

    Assert.assertEquals("testClientId", result.clientId)
    Assert.assertEquals("testRedirectUri", result.redirectUri)
    Assert.assertEquals(null, result.scope)
  }

  @Test(expected = Exception::class)
  fun `getSsoConfig throws exception when config file is not found`() {
    val context = mock<Context>()
    val resources = mock<Resources>()
    val resourceId = 0

    whenever(context.resources).thenReturn(resources)
    whenever(context.packageName).thenReturn("com.uber.sdk2.auth")
    whenever(
        resources.getIdentifier(
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
        )
      )
      .thenReturn(resourceId)

    SsoConfigProvider.getSsoConfig(context)
  }

  @Test(expected = Exception::class)
  fun `getSsoConfig throws exception when config file is empty`() {
    val context = mock<Context>()
    val resources = mock<Resources>()
    val resourceId = 1
    val configJsonString = ""

    whenever(context.resources).thenReturn(resources)
    whenever(context.packageName).thenReturn("com.uber.sdk2.auth")
    whenever(
        resources.getIdentifier(
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
        )
      )
      .thenReturn(resourceId)
    whenever(resources.openRawResource(ArgumentMatchers.anyInt()))
      .thenReturn(ByteArrayInputStream(configJsonString.toByteArray()))

    SsoConfigProvider.getSsoConfig(context)
  }

  @Test(expected = Exception::class)
  fun `getSsoConfig throws exception when config file is invalid`() {
    val context = mock<Context>()
    val resources = mock<Resources>()
    val resourceId = 1
    val configJsonString = """{"client_id":"testClientId"}"""

    whenever(context.resources).thenReturn(resources)
    whenever(context.packageName).thenReturn("com.uber.sdk2.auth")
    whenever(
        resources.getIdentifier(
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
        )
      )
      .thenReturn(resourceId)
    whenever(resources.openRawResource(ArgumentMatchers.anyInt()))
      .thenReturn(ByteArrayInputStream(configJsonString.toByteArray()))

    SsoConfigProvider.getSsoConfig(context)
  }

  @Test(expected = Exception::class)
  fun `getSsoConfig throws exception when config file is missing required fields`() {
    val context = mock<Context>()
    val resources = mock<Resources>()
    val resourceId = 1
    val configJsonString = """{}"""

    whenever(context.resources).thenReturn(resources)
    whenever(context.packageName).thenReturn("com.uber.sdk2.auth")
    whenever(
        resources.getIdentifier(
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
        )
      )
      .thenReturn(resourceId)
    whenever(resources.openRawResource(ArgumentMatchers.anyInt()))
      .thenReturn(ByteArrayInputStream(configJsonString.toByteArray()))

    SsoConfigProvider.getSsoConfig(context)
  }
}
