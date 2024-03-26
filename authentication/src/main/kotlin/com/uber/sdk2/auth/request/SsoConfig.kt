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
data class SsoConfig(val clientId: String, val redirectUri: String, val scope: String? = null) :
  Parcelable

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
