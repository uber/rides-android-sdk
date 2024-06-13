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
package com.uber.sdk2.auth.internal.shadow

import android.content.Context
import com.uber.sdk2.auth.request.SsoConfig
import com.uber.sdk2.auth.request.SsoConfigProvider
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements

@Implements(SsoConfigProvider::class)
class ShadowSsoConfigProvider {
  companion object {
    @JvmStatic
    @Implementation
    fun getSsoConfig(context: Context): SsoConfig {
      return SsoConfig("clientId", "redirectUri", "profile")
    }
  }
}
