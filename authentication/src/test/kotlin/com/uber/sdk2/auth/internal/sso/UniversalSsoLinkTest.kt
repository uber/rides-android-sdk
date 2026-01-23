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

import androidx.appcompat.app.AppCompatActivity
import com.uber.sdk2.auth.AppDiscovering
import com.uber.sdk2.auth.RobolectricTestBase
import com.uber.sdk2.auth.request.AuthContext
import com.uber.sdk2.auth.request.AuthDestination
import com.uber.sdk2.auth.request.AuthType
import com.uber.sdk2.auth.request.CrossApp
import com.uber.sdk2.auth.request.PrefillInfo
import com.uber.sdk2.auth.request.SsoConfig
import com.uber.sdk2.auth.sso.CustomTabsLauncher
import com.uber.sdk2.core.config.UriConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.Robolectric

class UniversalSsoLinkTest : RobolectricTestBase() {
  private val activity: AppCompatActivity =
    Robolectric.buildActivity(AppCompatActivity::class.java).get()
  private val ssoConfig: SsoConfig =
    SsoConfig(clientId = "clientId", redirectUri = "redirectUri", scope = "scope")
  private val authContext: AuthContext =
    AuthContext(
      authDestination = AuthDestination.CrossAppSso(listOf(CrossApp.Rider)),
      authType = AuthType.AuthCode,
      prefillInfo =
        PrefillInfo(
          email = "email",
          firstName = "firstName",
          lastName = "lastName",
          phoneNumber = "phoneNumber",
        ),
    )
  private val appDiscovering: AppDiscovering = mock()

  private val customTabsLauncher: CustomTabsLauncher = mock()

  private val universalSsoLink =
    UniversalSsoLink(
      activity = activity,
      ssoConfig = ssoConfig,
      authContext = authContext,
      appDiscovering = appDiscovering,
      customTabsLauncher = customTabsLauncher,
    )

  private val testDispatcher = StandardTestDispatcher()

  @OptIn(ExperimentalCoroutinesApi::class)
  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    whenever(appDiscovering.findAppForSso(any(), any())).thenReturn("com.uber")
  }

  @Test
  fun `execute should return a string`() =
    runTest(testDispatcher) {
      universalSsoLink.resultDeferred.complete("SuccessResult")

      // Simulate calling execute and handle outcomes.
      val result = universalSsoLink.execute(mapOf("param1" to "value1"))

      assertNotNull(result)
      assertEquals("SuccessResult", result)
    }

  @Test
  fun `execute should fail`() =
    runTest(testDispatcher) {
      universalSsoLink.resultDeferred.completeExceptionally(Exception("Failed"))

      // Simulate calling execute and handle outcomes.
      try {
        universalSsoLink.execute(mapOf("param1" to "value1"))
      } catch (e: Exception) {
        assertEquals("Failed", e.message)
      }
    }

  @Test
  fun `execute should use inApp authentication when apps are not present`() =
    runTest(testDispatcher) {
      whenever(appDiscovering.findAppForSso(any(), any())).thenReturn(null)

      doNothing().whenever(customTabsLauncher).launch(any())
      universalSsoLink.resultDeferred.complete("SuccessResult")

      // Simulate calling execute and handle outcomes.
      val result = universalSsoLink.execute(mapOf())

      assertNotNull(result)
      assertEquals("SuccessResult", result)

      verify(customTabsLauncher).launch(any())
    }

  @Test
  fun `execute should use inApp authentication with authorize path when apps are not present`() =
    runTest(testDispatcher) {
      whenever(appDiscovering.findAppForSso(any(), any())).thenReturn(null)

      doNothing().whenever(customTabsLauncher).launch(any())
      universalSsoLink.resultDeferred.complete("SuccessResult")

      // Simulate calling execute and handle outcomes.
      val result = universalSsoLink.execute(mapOf())

      assertNotNull(result)
      assertEquals("SuccessResult", result)

      verify(customTabsLauncher)
        .launch(argThat { this.path.equals("/${UriConfig.AUTHORIZE_PATH}") })
    }

  @Test
  fun `handleAuthCode when called should complete`() =
    runTest(testDispatcher) {
      universalSsoLink.handleAuthCode("authCode")
      val result = universalSsoLink.execute(mapOf())

      assertNotNull(result)
      assertEquals("authCode", result)
    }

  @Test
  fun `execute when query params are provided and auth destination is InApp should updated the uri`() =
    runTest(testDispatcher) {
      universalSsoLink.resultDeferred.complete("SuccessResult")
      whenever(appDiscovering.findAppForSso(any(), any())).thenReturn(null)
      doNothing().whenever(customTabsLauncher).launch(any())

      // Simulate calling execute and handle outcomes.
      val result = universalSsoLink.execute(mapOf("param1" to "value1"))

      assertNotNull(result)
      assertEquals("SuccessResult", result)
      verify(customTabsLauncher).launch(argThat { getQueryParameter("param1") == "value1" })
    }
}
