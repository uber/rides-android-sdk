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
