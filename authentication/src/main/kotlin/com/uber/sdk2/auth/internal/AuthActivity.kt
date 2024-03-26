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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.uber.sdk2.auth.AuthProviding
import com.uber.sdk2.auth.exception.AuthException
import com.uber.sdk2.auth.exception.AuthException.Companion.AUTH_CODE_INVALID
import com.uber.sdk2.auth.request.AuthContext
import com.uber.sdk2.auth.response.AuthResult
import com.uber.sdk2.core.utils.CustomTabsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

  private var authProvider: AuthProviding? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val authContext =
      intent.getParcelableExtra<AuthContext>(AUTH_CONTEXT)
        ?: throw IllegalStateException("AuthContext is required")

    authProvider = AuthProvider(this, authContext)
    init()
  }

  private fun init() {
    authProvider?.let {
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
    // Check if the intent has the auth code. This happens when user has authenticated using custom
    // tabs
    intent?.data?.let {
      if (isAuthorizationCodePresent(it)) {
        val authCode = it.getQueryParameter(KEY_AUTHENTICATION_CODE)
        if (authCode.isNullOrEmpty()) {
          throw AuthException.ClientError(AUTH_CODE_INVALID)
        }
        authProvider?.handleAuthCode(authCode)
      }
    }
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
