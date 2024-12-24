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
package com.uber.sdk2.auth.request

import android.content.Context
import android.content.res.Resources
import android.os.Parcelable
import com.uber.sdk2.auth.exception.AuthException
import com.uber.sdk2.core.config.UriConfig.CLIENT_ID_PARAM
import com.uber.sdk2.core.config.UriConfig.REDIRECT_PARAM
import com.uber.sdk2.core.config.UriConfig.SCOPE_PARAM
import java.io.IOException
import java.nio.charset.Charset
import kotlinx.parcelize.Parcelize
import okio.Buffer
import okio.BufferedSource
import okio.buffer
import okio.source
import org.json.JSONException
import org.json.JSONObject

@Parcelize
data class SsoConfig
@JvmOverloads
constructor(val clientId: String, val redirectUri: String, val scope: String? = null) : Parcelable

object SsoConfigProvider {
  fun getSsoConfig(context: Context): SsoConfig {
    val resources: Resources = context.resources
    val resourceId = resources.getIdentifier(SSO_CONFIG_FILE, "raw", context.packageName)
    val configSource: BufferedSource = resources.openRawResource(resourceId).source().buffer()
    configSource.use {
      val configData = Buffer()
      try {
        configSource.readAll(configData)
        val configJson = JSONObject(configData.readString(Charset.forName("UTF-8")))
        val clientId = getRequiredConfigString(configJson, CLIENT_ID_PARAM)
        val scope = getConfigString(configJson, SCOPE_PARAM)
        val redirectUri = getRequiredConfigString(configJson, REDIRECT_PARAM)
        return SsoConfig(clientId, redirectUri, scope)
      } catch (ex: IOException) {
        throw AuthException.ClientError("Failed to read configuration: " + ex.message)
      } catch (ex: JSONException) {
        throw AuthException.ClientError("Failed to read configuration: " + ex.message)
      }
    }
  }

  @Throws(AuthException::class)
  private fun getRequiredConfigString(configJson: JSONObject, propName: String): String {
    return getConfigString(configJson, propName)
      ?: throw AuthException.ClientError(
        "$propName is required but not specified in the configuration"
      )
  }

  private fun getConfigString(configJson: JSONObject, propName: String): String? {
    val value: String = configJson.optString(propName) ?: return null
    return value.trim { it <= ' ' }.takeIf { it.isNotEmpty() }
  }

  private const val SSO_CONFIG_FILE = "sso_config"
}
