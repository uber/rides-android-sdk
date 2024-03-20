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
import com.uber.sdk2.auth.AuthActivity
import com.uber.sdk2.auth.RobolectricTestBase
import com.uber.sdk2.auth.api.request.AuthContext
import com.uber.sdk2.auth.api.request.AuthDestination
import com.uber.sdk2.auth.api.request.AuthType
import com.uber.sdk2.auth.internal.shadow.SsoConfigProviderShadow
import org.junit.Test
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@Config(shadows = [SsoConfigProviderShadow::class])
class SsoLinkFactoryTest : RobolectricTestBase() {
  @Test
  fun `test generateSsoLink`() {
    val authContext = AuthContext(AuthDestination.InApp, AuthType.AuthCode, null)
    val intent: Intent = Intent().apply { putExtra("auth_context", authContext) }
    val ssoLink =
      SsoLinkFactory.generateSsoLink(
        Robolectric.buildActivity(AuthActivity::class.java, intent).create().get(),
        authContext,
      )
    assert(ssoLink is UniversalSsoLink)
  }
}
