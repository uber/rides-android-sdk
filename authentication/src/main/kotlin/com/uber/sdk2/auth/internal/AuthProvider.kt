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
import com.uber.sdk2.auth.AuthProviding
import com.uber.sdk2.auth.PKCEGenerator
import com.uber.sdk2.auth.exception.AuthException
import com.uber.sdk2.auth.internal.service.AuthService
import com.uber.sdk2.auth.internal.sso.SsoLinkFactory
import com.uber.sdk2.auth.internal.sso.UniversalSsoLink.Companion.RESPONSE_TYPE
import com.uber.sdk2.auth.internal.utils.Base64Util
import com.uber.sdk2.auth.request.AuthContext
import com.uber.sdk2.auth.request.AuthType
import com.uber.sdk2.auth.request.SsoConfig
import com.uber.sdk2.auth.request.SsoConfigProvider
import com.uber.sdk2.auth.response.AuthResult
import com.uber.sdk2.auth.response.PARResponse
import com.uber.sdk2.auth.response.UberToken
import com.uber.sdk2.core.config.UriConfig
import com.uber.sdk2.core.config.UriConfig.CODE_CHALLENGE_PARAM
import com.uber.sdk2.core.config.UriConfig.REQUEST_URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthProvider(
  private val activity: AppCompatActivity,
  private val authContext: AuthContext,
  private val authService: AuthService = AuthService.create(),
  private val codeVerifierGenerator: PKCEGenerator = PKCEGeneratorImpl,
) : AuthProviding {
  private val verifier: String = codeVerifierGenerator.generateCodeVerifier()
  private val ssoLink = SsoLinkFactory.generateSsoLink(activity, authContext)

  override suspend fun authenticate(): AuthResult {
    val ssoConfig = withContext(Dispatchers.IO) { SsoConfigProvider.getSsoConfig(activity) }
    return try {
      val parResponse = sendPushedAuthorizationRequest(ssoConfig)
      val queryParams = getQueryParams(parResponse)
      val authCode = ssoLink.execute(queryParams)
      when (authContext.authType) {
        AuthType.AuthCode -> AuthResult.Success(UberToken(authCode = authCode))
        is AuthType.PKCE -> performPkce(ssoConfig, authContext.authType, authCode)
      }
    } catch (e: AuthException) {
      AuthResult.Error(e)
    }
  }

  private suspend fun performPkce(
    ssoConfig: SsoConfig,
    authType: AuthType.PKCE,
    authCode: String,
  ): AuthResult {
    val tokenResponse =
      authService.token(
        ssoConfig.clientId,
        verifier,
        authType.grantType,
        ssoConfig.redirectUri,
        authCode,
      )

    return if (tokenResponse.isSuccessful) {
      tokenResponse.body()?.let { AuthResult.Success(it) }
        ?: AuthResult.Error(AuthException.ClientError("Token request failed with empty response"))
    } else {
      AuthResult.Error(
        AuthException.ClientError("Token request failed with code: ${tokenResponse.code()}")
      )
    }
  }

  private suspend fun sendPushedAuthorizationRequest(ssoConfig: SsoConfig) =
    authContext.prefillInfo?.let {
      val response =
        authService.loginParRequest(
          ssoConfig.clientId,
          RESPONSE_TYPE,
          Base64Util.encodePrefillInfoToString(it),
          ssoConfig.scope ?: "profile",
        )
      val body = response.body()
      body?.takeIf { response.isSuccessful }
        ?: throw AuthException.ServerError("Bad response ${response.code()}")
    } ?: PARResponse("", "")

  private fun getQueryParams(parResponse: PARResponse) = buildMap {
    parResponse.requestUri.takeIf { it.isNotEmpty() }?.let { put(REQUEST_URI, it) }
    authContext.prompt?.let { put(UriConfig.PROMPT_PARAM, it.value) }
    if (authContext.authType is AuthType.PKCE) {
      val codeChallenge = codeVerifierGenerator.generateCodeChallenge(verifier)
      put(CODE_CHALLENGE_PARAM, codeChallenge)
      put(UriConfig.CODE_CHALLENGE_METHOD, UriConfig.CODE_CHALLENGE_METHOD_VAL)
    }
  }

  override fun handleAuthCode(authCode: String) {
    ssoLink.handleAuthCode(authCode)
  }

  override fun isAuthInProgress(): Boolean {
    return ssoLink.isAuthInProgress()
  }
}
