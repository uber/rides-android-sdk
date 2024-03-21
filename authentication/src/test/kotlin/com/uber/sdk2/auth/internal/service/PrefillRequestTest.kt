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
package com.uber.sdk2.auth.internal.service

import com.uber.sdk2.auth.RobolectricTestBase
import com.uber.sdk2.auth.api.request.PrefillInfo
import com.uber.sdk2.auth.api.response.PARResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response

class PrefillRequestTest : RobolectricTestBase() {
  private val authService: AuthService = mock()
  private val prefillInfo = PrefillInfo("email", "firstName", "lastName", "phoneNumber")

  @Test
  fun `test pushedAuthorizationRequest when successfulResponse should receiveRequestUri`() =
    runTest {
      whenever(authService.loginParRequest(any(), any(), any(), any()))
        .thenReturn(Response.success(PARResponse("requestUri", "expiresIn")))
      val response =
        PrefillRequest.pushedAuthorizationRequest(authService, "clientId", prefillInfo, "scope")
      assertNotNull(response)
      assertEquals("requestUri", response.requestUri)
    }
}
