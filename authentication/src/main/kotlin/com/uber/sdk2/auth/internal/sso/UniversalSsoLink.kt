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
package com.uber.sdk2.auth.internal.sso

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.RESULT_CANCELED
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import com.uber.sdk2.auth.api.AppDiscovering
import com.uber.sdk2.auth.api.exception.AuthException
import com.uber.sdk2.auth.api.request.AuthContext
import com.uber.sdk2.auth.api.request.AuthDestination
import com.uber.sdk2.auth.api.request.SsoConfig
import com.uber.sdk2.auth.api.sso.CustomTabsLauncher
import com.uber.sdk2.auth.api.sso.SsoLink
import com.uber.sdk2.core.config.UriConfig
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Represents the Single Sign-On (SSO) link for authentication. It uses the [AppDiscovering] service
 * to find the app that can handle the SSO request. If no app is found, it falls back to using a
 * custom tab to handle the SSO request.
 *
 * @param activity The activity that is used to start the SSO authentication.
 * @param ssoConfig The configuration for the SSO authentication.
 * @param authContext The context containing request config for the authentication.
 * @param appDiscovering The app discovering service to find the app for SSO.
 */
internal class UniversalSsoLink(
  private val activity: AppCompatActivity,
  private val ssoConfig: SsoConfig,
  private val authContext: AuthContext,
  private val appDiscovering: AppDiscovering,
  private val customTabsLauncher: CustomTabsLauncher = CustomTabsLauncherImpl(activity),
) : SsoLink {

  @VisibleForTesting val resultDeferred = CompletableDeferred<String>()

  private val launcher =
    activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      try {
        resultDeferred.complete(handleResult(result))
      } catch (e: AuthException) {
        resultDeferred.completeExceptionally(e)
      }
    }

  override suspend fun execute(optionalQueryParams: Map<String, String>): String {
    val uri =
      UriConfig.assembleUri(
          ssoConfig.clientId,
          RESPONSE_TYPE,
          ssoConfig.redirectUri,
          ssoConfig.scope,
        )
        .buildUpon()
        .also { builder ->
          optionalQueryParams.entries.forEach { entry ->
            builder.appendQueryParameter(entry.key, entry.value)
          }
        }
        .build()
    withContext(Dispatchers.Main) {
      when (authContext.authDestination) {
        is AuthDestination.CrossAppSso -> {
          val packageName =
            appDiscovering.findAppForSso(uri, authContext.authDestination.appPriority)
          packageName?.let {
            val intent = Intent()
            intent.`package` = packageName
            intent.data = uri
            launcher.launch(intent)
          } ?: loadCustomtab(uri)
        }
        is AuthDestination.InApp -> loadCustomtab(uri)
      }
    }
    return resultDeferred.await()
  }

  override fun handleAuthCode(authCode: String) {
    resultDeferred.complete(authCode)
  }

  private fun handleResult(result: ActivityResult): String {
    return when (result.resultCode) {
      RESULT_OK -> {
        Log.d("xxxx", result.data?.getStringExtra("CODE_RECEIVED").orEmpty())
        result.data?.let {
          if (!it.getStringExtra("CODE_RECEIVED").isNullOrEmpty()) {
            it.getStringExtra("CODE_RECEIVED").orEmpty()
          } else if (!it.getStringExtra("EXTRA_ERROR").isNullOrEmpty()) {
            throw AuthException.ClientError(it.getStringExtra("EXTRA_ERROR").orEmpty())
          } else {
            throw AuthException.ClientError(AuthException.EMPTY_RESPONSE)
          }
        } ?: throw AuthException.ClientError(AuthException.NULL_RESPONSE)
      }
      RESULT_CANCELED -> {
        throw AuthException.ClientError(AuthException.CANCELED)
      }
      else -> {
        // should never happen
        throw AuthException.ClientError(AuthException.UNKNOWN)
      }
    }
  }

  private fun loadCustomtab(uri: Uri) {
    customTabsLauncher.launch(uri)
  }

  companion object {
    /** The response type constant for the SSO authentication. */
    internal const val RESPONSE_TYPE = "code"
  }
}
