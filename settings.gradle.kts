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
pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://plugins.gradle.org/m2/") }
  }
  resolutionStrategy {
    eachPlugin {
      if (requested.id.id == "com.ncorti.ktfmt.gradle") {
        useModule("com.ncorti.ktfmt.gradle:ktfmt-gradle:${requested.version}")
      }
    }
  }
}

dependencyResolutionManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://plugins.gradle.org/m2/") }
  }
}

rootProject.name = "uber-android-sdk"

include(
  ":authentication",
  ":core",
  ":core-android",
  ":rides-android",
  ":samples:auth-demo",
  ":samples:request-button-sample",
  ":samples:login-sample",
  ":samples:login-with-auth-code-demo",
)
