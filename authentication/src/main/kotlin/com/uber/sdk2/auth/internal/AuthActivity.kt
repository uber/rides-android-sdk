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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uber.sdk2.auth.AuthProviding
import com.uber.sdk2.auth.exception.AuthException.Companion.CANCELED
import com.uber.sdk2.auth.request.AuthContext
import com.uber.sdk2.auth.response.AuthResult
import com.uber.sdk2.core.utils.CustomTabsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

  private var authProvider: AuthProviding? = null
  private var authStarted: Boolean = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val authContext =
      intent.getParcelableExtra<AuthContext>(AUTH_CONTEXT)
        ?: throw IllegalStateException("AuthContext is required")

    authProvider = AuthProvider(this, authContext)
  }

  private fun startAuth() {
    authProvider?.let {
      authStarted = true
      lifecycleScope.launch(Dispatchers.Main) {
        when (val authResult = it.authenticate()) {
          is AuthResult.Success -> {
            val intent = Intent().apply { putExtra("EXTRA_UBER_TOKEN", authResult.uberToken) }
            setResult(RESULT_OK, intent)
            finish()
          }
          is AuthResult.Error -> {
            val intent =
              Intent().apply { putExtra("EXTRA_ERROR", authResult.authException.message) }
            setResult(RESULT_CANCELED, intent)
            finish()
          }
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    this.intent = intent
  }

  override fun onResume() {
    super.onResume()
    // Check if the intent has the auth code.
    intent?.data?.let { handleResponse(it) }
      ?: run {
        // if intent does not have auth code and auth has not started then start the auth flow
        if (!authStarted) {
          startAuth()
          return
        }
        // otherwise finish the auth flow with "Canceled" error
        finishAuthWithError(CANCELED)
      }
  }

  private fun handleResponse(uri: Uri) {
    val authCode =
      uri.getQueryParameter(KEY_AUTHENTICATION_CODE)
        ?: uri.fragment
          ?.takeIf { it.isNotEmpty() }
          ?.let {
            Uri.Builder().encodedQuery(it).build().getQueryParameter(KEY_AUTHENTICATION_CODE)
          }
        ?: ""

    if (authCode.isNotEmpty()) {
      authProvider?.handleAuthCode(authCode)
    } else {
      val error =
        uri.getQueryParameter(KEY_ERROR)
          ?: uri.fragment
            ?.takeIf { it.isNotEmpty() }
            ?.let { Uri.Builder().encodedQuery(it).build().getQueryParameter(KEY_ERROR) }
          ?: CANCELED
      finishAuthWithError(error)
    }
  }

  private fun finishAuthWithError(error: String) {
    // If the intent does not have the auth code, then the user has cancelled the authentication
    intent.putExtra("EXTRA_ERROR", error)
    setResult(RESULT_CANCELED, intent)
    finish()
  }

  private fun isAuthorizationCodePresent(uri: Uri): Boolean {
    return !uri.getQueryParameter(KEY_AUTHENTICATION_CODE).isNullOrEmpty()
  }

  override fun onDestroy() {
    super.onDestroy()
    CustomTabsHelper.onDestroy(this)
  }

  companion object {
    private const val AUTH_CONTEXT = "auth_context"
    private const val KEY_AUTHENTICATION_CODE = "code"
    private const val KEY_ERROR = "error"

    fun newIntent(context: Context, authContext: AuthContext): Intent {
      val intent = Intent(context, AuthActivity::class.java)
      intent.putExtra(AUTH_CONTEXT, authContext)
      return intent
    }

    fun newResponseIntent(context: Context, responseUri: Uri?): Intent {
      val intent = Intent(context, AuthActivity::class.java)
      intent.setData(responseUri)
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
      return intent
    }
  }
}
