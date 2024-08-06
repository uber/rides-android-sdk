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

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.uber.sdk2.auth.response.PARResponse
import com.uber.sdk2.auth.response.UberToken
import com.uber.sdk2.core.config.UriConfig
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/** Service for making network requests to the auth server. */
interface AuthService {

  @FormUrlEncoded
  @POST("/oauth/v2/par")
  suspend fun loginParRequest(
    @Field("client_id") clientId: String,
    @Field("response_type") responseType: String,
    @Field("login_hint") prefillInfoString: String,
    @Field("scope") scope: String,
  ): Response<PARResponse>

  @FormUrlEncoded
  @POST("/oauth/v2/token")
  suspend fun token(
    @Field("client_id") clientId: String?,
    @Field("code_verifier") codeVerifier: String?,
    @Field("grant_type") grantType: String?,
    @Field("redirect_uri") redirectUri: String?,
    @Field("code") authCode: String?,
  ): Response<UberToken>

  companion object {
    /** Creates an instance of [AuthService]. */
    fun create(): AuthService {
      val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
      return Retrofit.Builder()
        .baseUrl(UriConfig.getAuthHost())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(AuthService::class.java)
    }
  }
}
