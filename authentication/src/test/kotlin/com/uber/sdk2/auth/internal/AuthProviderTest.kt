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
import com.uber.sdk2.auth.request.Prompt
import com.uber.sdk2.auth.response.AuthResult
import com.uber.sdk2.auth.response.PARResponse
import com.uber.sdk2.auth.response.UberToken
import com.uber.sdk2.auth.sso.SsoLink
import com.uber.sdk2.core.config.UriConfig
import com.uber.sdk2.core.config.UriConfig.CODE_CHALLENGE_METHOD
import com.uber.sdk2.core.config.UriConfig.CODE_CHALLENGE_METHOD_VAL
import com.uber.sdk2.core.config.UriConfig.CODE_CHALLENGE_PARAM
import com.uber.sdk2.core.config.UriConfig.REQUEST_URI
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
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
    reset(ssoLink)
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
    verify(ssoLink).execute(any())
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
    val argumentCaptor = argumentCaptor<Map<String, String>>()
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
    verify(ssoLink).execute(argumentCaptor.capture())
    assert(argumentCaptor.firstValue[REQUEST_URI] == "requestUri")
    assert(argumentCaptor.firstValue[CODE_CHALLENGE_PARAM] == "challenge")
    assert(argumentCaptor.firstValue[CODE_CHALLENGE_METHOD] == CODE_CHALLENGE_METHOD_VAL)
    assert(argumentCaptor.firstValue.containsValue(UriConfig.PROMPT_PARAM).not())
    assert(argumentCaptor.firstValue.size == 3)
    assert(result is AuthResult.Success)
    assert((result as AuthResult.Success).uberToken.accessToken == "accessToken")
  }

  @Test
  fun `test authenticate when AuthCode flow should return only AuthCode`() = runTest {
    whenever(ssoLink.execute(any())).thenReturn("authCode")
    whenever(authService.loginParRequest(any(), any(), any(), any()))
      .thenReturn(Response.success(PARResponse("requestUri", "codeVerifier")))
    val prefillInfo = PrefillInfo("email", "firstName", "lastName", "phoneNumber")
    val authContext =
      AuthContext(
        AuthDestination.CrossAppSso(listOf(CrossApp.Rider)),
        AuthType.AuthCode,
        prefillInfo,
      )
    val authProvider = AuthProvider(activity, authContext, authService, codeVerifierGenerator)
    val argumentCaptor = argumentCaptor<Map<String, String>>()
    val result = authProvider.authenticate()
    verify(authService, never()).token(any(), any(), any(), any(), any())
    verify(ssoLink).execute(argumentCaptor.capture())
    assert(argumentCaptor.lastValue[REQUEST_URI] == "requestUri")
    assert(argumentCaptor.lastValue.size == 1)
    assert(result is AuthResult.Success)
    assert((result as AuthResult.Success).uberToken.authCode == "authCode")
  }

  @Test
  fun `test authenticate when AuthCode flow and prefillInfo is Null should return only AuthCode`() =
    runTest {
      whenever(ssoLink.execute(any())).thenReturn("authCode")
      whenever(authService.loginParRequest(any(), any(), any(), any()))
        .thenReturn(Response.success(PARResponse("requestUri", "codeVerifier")))
      val authContext =
        AuthContext(AuthDestination.CrossAppSso(listOf(CrossApp.Rider)), AuthType.AuthCode, null)
      val authProvider = AuthProvider(activity, authContext, authService, codeVerifierGenerator)
      val argumentCaptor = argumentCaptor<Map<String, String>>()
      val result = authProvider.authenticate()
      verify(authService, never()).token(any(), any(), any(), any(), any())
      verify(ssoLink).execute(argumentCaptor.capture())
      assert(argumentCaptor.lastValue.isEmpty())
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
    verify(ssoLink).execute(any())
    assert(result is AuthResult.Error && result.authException.message == "error")
  }

  @Test
  fun `test authenticate when prompt param is present should add to query param`() = runTest {
    whenever(ssoLink.execute(any())).thenReturn("authCode")
    whenever(authService.loginParRequest(any(), any(), any(), any()))
      .thenReturn(Response.success(PARResponse("requestUri", "codeVerifier")))
    val authContext =
      AuthContext(
        AuthDestination.CrossAppSso(listOf(CrossApp.Rider)),
        AuthType.AuthCode,
        null,
        prompt = Prompt.LOGIN,
      )
    val authProvider = AuthProvider(activity, authContext, authService, codeVerifierGenerator)
    val argumentCaptor = argumentCaptor<Map<String, String>>()
    val result = authProvider.authenticate()
    verify(authService, never()).token(any(), any(), any(), any(), any())
    verify(ssoLink).execute(argumentCaptor.capture())
    assert(argumentCaptor.lastValue[UriConfig.PROMPT_PARAM] == Prompt.LOGIN.value)
    assert(result is AuthResult.Success)
    assert((result as AuthResult.Success).uberToken.authCode == "authCode")
  }

  @Test
  fun `test authenticate when PAR request fails should continue without metadata`() = runTest {
    whenever(ssoLink.execute(any())).thenReturn("authCode")
    // Mock PAR request to return error response (e.g., 500)
    val errorResponse: Response<PARResponse> = Response.error(500, mock())
    whenever(authService.loginParRequest(any(), any(), any(), any())).thenReturn(errorResponse)
    val prefillInfo = PrefillInfo("email", "firstName", "lastName", "phoneNumber")
    val authContext =
      AuthContext(
        AuthDestination.CrossAppSso(listOf(CrossApp.Rider)),
        AuthType.AuthCode,
        prefillInfo,
      )
    val authProvider = AuthProvider(activity, authContext, authService, codeVerifierGenerator)
    val argumentCaptor = argumentCaptor<Map<String, String>>()
    val result = authProvider.authenticate()
    // Verify PAR request was attempted
    verify(authService).loginParRequest(any(), any(), any(), any())
    // Verify authentication continued without request_uri
    verify(ssoLink).execute(argumentCaptor.capture())
    assert(argumentCaptor.lastValue.containsKey(REQUEST_URI).not())
    // Verify authentication succeeded
    assert(result is AuthResult.Success)
    assert((result as AuthResult.Success).uberToken.authCode == "authCode")
  }

  @Test
  fun `test authenticate when PAR request throws exception should continue without metadata`() =
    runTest {
      whenever(ssoLink.execute(any())).thenReturn("authCode")
      // Mock PAR request to throw network exception
      whenever(authService.loginParRequest(any(), any(), any(), any()))
        .thenThrow(RuntimeException("Network error"))
      val prefillInfo = PrefillInfo("email", "firstName", "lastName", "phoneNumber")
      val authContext =
        AuthContext(
          AuthDestination.CrossAppSso(listOf(CrossApp.Rider)),
          AuthType.AuthCode,
          prefillInfo,
        )
      val authProvider = AuthProvider(activity, authContext, authService, codeVerifierGenerator)
      val argumentCaptor = argumentCaptor<Map<String, String>>()
      val result = authProvider.authenticate()
      // Verify PAR request was attempted
      verify(authService).loginParRequest(any(), any(), any(), any())
      // Verify authentication continued without request_uri
      verify(ssoLink).execute(argumentCaptor.capture())
      assert(argumentCaptor.lastValue.containsKey(REQUEST_URI).not())
      // Verify authentication succeeded
      assert(result is AuthResult.Success)
      assert((result as AuthResult.Success).uberToken.authCode == "authCode")
    }

  @Test
  fun `test authenticate with PKCE when PAR fails should still include code challenge`() = runTest {
    whenever(ssoLink.execute(any())).thenReturn("authCode")
    whenever(codeVerifierGenerator.generateCodeVerifier()).thenReturn("verifier")
    whenever(codeVerifierGenerator.generateCodeChallenge("verifier")).thenReturn("challenge")
    whenever(authService.token(any(), any(), any(), any(), any()))
      .thenReturn(Response.success(UberToken(accessToken = "accessToken")))
    // Mock PAR request to fail
    val errorResponse: Response<PARResponse> = Response.error(500, mock())
    whenever(authService.loginParRequest(any(), any(), any(), any())).thenReturn(errorResponse)
    val prefillInfo = PrefillInfo("email", "firstName", "lastName", "phoneNumber")
    val authContext =
      AuthContext(AuthDestination.CrossAppSso(listOf(CrossApp.Rider)), AuthType.PKCE(), prefillInfo)
    val authProvider = AuthProvider(activity, authContext, authService, codeVerifierGenerator)
    val argumentCaptor = argumentCaptor<Map<String, String>>()
    val result = authProvider.authenticate()
    // Verify PAR request was attempted
    verify(authService).loginParRequest(any(), any(), any(), any())
    // Verify authentication continued with code challenge but without request_uri
    verify(ssoLink).execute(argumentCaptor.capture())
    assert(argumentCaptor.lastValue.containsKey(REQUEST_URI).not())
    assert(argumentCaptor.lastValue[CODE_CHALLENGE_PARAM] == "challenge")
    assert(argumentCaptor.lastValue[CODE_CHALLENGE_METHOD] == CODE_CHALLENGE_METHOD_VAL)
    // Verify authentication succeeded
    assert(result is AuthResult.Success)
    assert((result as AuthResult.Success).uberToken.accessToken == "accessToken")
  }
}
