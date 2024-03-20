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
import com.uber.sdk2.auth.api.AuthProviding
import com.uber.sdk2.auth.api.PKCEGenerator
import com.uber.sdk2.auth.api.exception.AuthException
import com.uber.sdk2.auth.api.request.AuthContext
import com.uber.sdk2.auth.api.request.AuthType
import com.uber.sdk2.auth.api.request.SsoConfigProvider
import com.uber.sdk2.auth.api.response.AuthResult
import com.uber.sdk2.auth.api.response.PARResponse
import com.uber.sdk2.auth.api.response.UberToken
import com.uber.sdk2.auth.internal.service.AuthService
import com.uber.sdk2.auth.internal.sso.SsoLinkFactory

class AuthProvider(
  private val activity: AppCompatActivity,
  private val authContext: AuthContext,
  private val authService: AuthService = AuthService.create(),
  private val codeVerifierGenerator: PKCEGenerator = PKCEGeneratorImpl,
) : AuthProviding {

  private val verifier: String = codeVerifierGenerator.generateCodeVerifier()
  private val ssoLink = SsoLinkFactory.generateSsoLink(activity, authContext)

  override suspend fun authenticate(): AuthResult {
    val ssoConfig = SsoConfigProvider.getSsoConfig(activity)
    val parResponse =
      authContext.prefillInfo?.let {
        val response =
          authService.loginParRequest(ssoConfig.clientId, "code", it, ssoConfig.scope ?: "profile")
        if (response.isSuccessful && response.body() != null) {
          response.body()
        } else {
          throw AuthException.ServerError("bad response ${response.code()}")
        }
      } ?: PARResponse("", "")

    val queryParams: Map<String, String> =
      mapOf(
        "request_uri" to parResponse.requestUri,
        "code_challenge" to codeVerifierGenerator.generateCodeChallenge(verifier),
      )
    try {
      val authCode = ssoLink.execute(queryParams)
      return when (authContext.authType) {
        AuthType.AuthCode -> AuthResult.Success(UberToken(authCode = authCode))
        is AuthType.PKCE -> {
          val tokenResponse =
            authService.token(
              ssoConfig.clientId,
              verifier,
              authContext.authType.grantType,
              ssoConfig.redirectUri,
              authCode,
            )

          if (tokenResponse.isSuccessful) {
            tokenResponse.body()?.let { AuthResult.Success(it) }
              ?: AuthResult.Error(
                AuthException.ClientError("Token request failed with empty response")
              )
          } else {
            AuthResult.Error(
              AuthException.ClientError("Token request failed with code: ${tokenResponse.code()}")
            )
          }
        }
      }
    } catch (e: AuthException) {
      return AuthResult.Error(e)
    }
  }

  override fun handleAuthCode(authCode: String) {
    ssoLink.handleAuthCode(authCode)
  }
}
