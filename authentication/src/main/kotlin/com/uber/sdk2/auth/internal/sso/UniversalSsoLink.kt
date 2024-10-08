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

import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.RESULT_CANCELED
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import com.uber.sdk2.auth.AppDiscovering
import com.uber.sdk2.auth.exception.AuthException
import com.uber.sdk2.auth.request.AuthContext
import com.uber.sdk2.auth.request.AuthDestination
import com.uber.sdk2.auth.request.SsoConfig
import com.uber.sdk2.auth.sso.CustomTabsLauncher
import com.uber.sdk2.auth.sso.SsoLink
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
  private var isAuthInProgress: Boolean = false

  private val launcher =
    activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      try {
        resultDeferred.complete(handleResult(result))
      } catch (e: AuthException) {
        resultDeferred.completeExceptionally(e)
      }
      isAuthInProgress = true
    }

  override suspend fun execute(optionalQueryParams: Map<String, String>): String {
    val uri =
      UriConfig.assembleUri(
          ssoConfig.clientId,
          RESPONSE_TYPE,
          ssoConfig.redirectUri,
          scopes = ssoConfig.scope,
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
          } ?: loadCustomtab(getSecureWebviewUri(uri))
        }
        is AuthDestination.InApp -> loadCustomtab(getSecureWebviewUri(uri))
      }
    }
    return resultDeferred.await()
  }

  private fun getSecureWebviewUri(uri: Uri) = uri.buildUpon().path(UriConfig.AUTHORIZE_PATH).build()

  override fun handleAuthCode(authCode: String) {
    resultDeferred.complete(authCode)
  }

  private fun handleResult(result: ActivityResult): String {
    return when (result.resultCode) {
      RESULT_OK -> {
        result.data?.getStringExtra(EXTRA_CODE_RECEIVED)?.ifEmpty {
          throw AuthException.ClientError(AuthException.AUTH_CODE_NOT_PRESENT)
        } ?: throw AuthException.ClientError(AuthException.NULL_RESPONSE)
      }
      RESULT_CANCELED -> {
        result.data?.getStringExtra(EXTRA_ERROR)?.let { throw AuthException.ClientError(it) }
          ?: throw AuthException.ClientError(AuthException.CANCELED)
      }
      else -> {
        // should never happen
        throw AuthException.ClientError(AuthException.UNKNOWN)
      }
    }
  }

  override fun isAuthInProgress(): Boolean {
    return isAuthInProgress
  }

  private fun loadCustomtab(uri: Uri) {
    customTabsLauncher.launch(uri)
  }

  companion object {
    /** The response type constant for the SSO authentication. */
    internal const val RESPONSE_TYPE = "code"

    private const val EXTRA_CODE_RECEIVED = "CODE_RECEIVED"
    private const val EXTRA_ERROR = "ERROR"
  }
}
