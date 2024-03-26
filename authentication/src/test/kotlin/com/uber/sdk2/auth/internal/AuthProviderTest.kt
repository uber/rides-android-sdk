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

import androidx.appcompat.app.AppCompatActivity
import com.uber.sdk2.auth.PKCEGenerator
import com.uber.sdk2.auth.RobolectricTestBase
import com.uber.sdk2.auth.exception.AuthException
import com.uber.sdk2.auth.internal.service.AuthService
import com.uber.sdk2.auth.internal.shadow.ShadowSsoConfigProvider
import com.uber.sdk2.auth.internal.shadow.ShadowSsoLinkFactory
import com.uber.sdk2.auth.internal.sso.SsoLinkFactory
import com.uber.sdk2.auth.internal.utils.Base64Util
import com.uber.sdk2.auth.request.AuthContext
import com.uber.sdk2.auth.request.AuthDestination
import com.uber.sdk2.auth.request.AuthType
import com.uber.sdk2.auth.request.CrossApp
import com.uber.sdk2.auth.request.PrefillInfo
import com.uber.sdk2.auth.response.AuthResult
import com.uber.sdk2.auth.response.PARResponse
import com.uber.sdk2.auth.response.UberToken
import com.uber.sdk2.auth.sso.SsoLink
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow
import retrofit2.Response

@Config(shadows = [ShadowSsoLinkFactory::class, ShadowSsoConfigProvider::class])
class AuthProviderTest : RobolectricTestBase() {
  private val activity: AppCompatActivity = mock()
  private val authService: AuthService = mock()
  private val codeVerifierGenerator: PKCEGenerator = mock()

  private lateinit var ssoLink: SsoLink

  @Before
  fun setUp() {
    ssoLink = Shadow.extract<ShadowSsoLinkFactory>(SsoLinkFactory).ssoLink
  }

  @Test
  fun `test authenticate when PKCE flow should return tokens`() = runTest {
    whenever(ssoLink.execute(any())).thenReturn("code")
    whenever(authService.loginParRequest(any(), any(), any(), any()))
      .thenReturn(Response.success(PARResponse("requestUri", "codeVerifier")))
    whenever(codeVerifierGenerator.generateCodeVerifier()).thenReturn("verifier")
    whenever(codeVerifierGenerator.generateCodeChallenge("verifier")).thenReturn("challenge")
    whenever(authService.token(any(), any(), any(), any(), any()))
      .thenReturn(Response.success(UberToken(accessToken = "accessToken")))
    val authContext =
      AuthContext(AuthDestination.CrossAppSso(listOf(CrossApp.Rider)), AuthType.PKCE(), null)
    val authProvider = AuthProvider(activity, authContext, authService, codeVerifierGenerator)
    val result = authProvider.authenticate()
    verify(authService, never()).loginParRequest(any(), any(), any(), any())
    verify(authService).token("clientId", "verifier", "authorization_code", "redirectUri", "code")
    assert(result is AuthResult.Success)
    assert((result as AuthResult.Success).uberToken.accessToken == "accessToken")
  }

  @Test
  fun `test authenticate with prefill when PKCE flow should return tokens`() = runTest {
    whenever(ssoLink.execute(any())).thenReturn("authCode")
    whenever(authService.loginParRequest(any(), any(), any(), any()))
      .thenReturn(Response.success(PARResponse("requestUri", "codeVerifier")))
    whenever(codeVerifierGenerator.generateCodeVerifier()).thenReturn("verifier")
    whenever(codeVerifierGenerator.generateCodeChallenge("verifier")).thenReturn("challenge")
    whenever(authService.token(any(), any(), any(), any(), any()))
      .thenReturn(Response.success(UberToken(accessToken = "accessToken")))
    val prefillInfo = PrefillInfo("email", "firstName", "lastName", "phoneNumber")
    val authContext =
      AuthContext(AuthDestination.CrossAppSso(listOf(CrossApp.Rider)), AuthType.PKCE(), prefillInfo)
    val authProvider = AuthProvider(activity, authContext, authService, codeVerifierGenerator)
    val result = authProvider.authenticate()
    verify(authService)
      .loginParRequest(
        "clientId",
        "code",
        Base64Util.encodePrefillInfoToString(prefillInfo),
        "profile",
      )
    verify(authService)
      .token("clientId", "verifier", "authorization_code", "redirectUri", "authCode")
    assert(result is AuthResult.Success)
    assert((result as AuthResult.Success).uberToken.accessToken == "accessToken")
  }

  @Test
  fun `test authenticate when AuthCode flow should return only AuthCode`() = runTest {
    whenever(ssoLink.execute(any())).thenReturn("authCode")
    whenever(authService.loginParRequest(any(), any(), any(), any()))
      .thenReturn(Response.success(PARResponse("requestUri", "codeVerifier")))
    val authContext =
      AuthContext(AuthDestination.CrossAppSso(listOf(CrossApp.Rider)), AuthType.AuthCode, null)
    val authProvider = AuthProvider(activity, authContext, authService, codeVerifierGenerator)
    val result = authProvider.authenticate()
    verify(authService, never()).token(any(), any(), any(), any(), any())
    assert(result is AuthResult.Success)
    assert((result as AuthResult.Success).uberToken.authCode == "authCode")
  }

  @Test
  fun `test authenticate when authException should return error result`() = runTest {
    whenever(ssoLink.execute(any())).thenThrow(AuthException.ClientError("error"))
    val authContext =
      AuthContext(AuthDestination.CrossAppSso(listOf(CrossApp.Rider)), AuthType.AuthCode, null)
    val authProvider = AuthProvider(activity, authContext, authService, codeVerifierGenerator)
    val result = authProvider.authenticate()
    assert(result is AuthResult.Error && result.authException.message == "error")
  }
}
