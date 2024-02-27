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
plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
}

android {
  namespace = "com.uber.sdk.android.rides.samples"

  buildFeatures { buildConfig = true }

  defaultConfig {
    targetSdk = libs.versions.targetSdkVersion.get().toInt()
    multiDexEnabled = true
    buildConfigField("String", "CLIENT_ID", "\"${loadSecret("UBER_CLIENT_ID")}\"")
    buildConfigField("String", "REDIRECT_URI", "\"${loadSecret("UBER_REDIRECT_URI")}\"")
  }
  sourceSets { getByName("main") { java.srcDirs("src/main/java") } }
  buildTypes { getByName("debug") { matchingFallbacks += listOf("release") } }
  testOptions {
    execution = "ANDROIDX_TEST_ORCHESTRATOR"
    unitTests { isIncludeAndroidResources = true }
  }
}

dependencies {
  implementation(libs.uberRides) { exclude(group = "org.slf4j", module = "slf4j-log4j12") }
  implementation(libs.appCompat)
  implementation(project(":core-android"))
  implementation(project(":rides-android"))
}

/**
 * Loads property from gradle.properties and ~/.gradle/gradle.properties Use to look up confidential
 * information like keys that shouldn't be stored publicly
 *
 * @param name to lookup
 * @return the value of the property or "MISSING"
 */
fun loadSecret(name: String): String {
  val gradleProperty = findProperty(name)?.toString()
  return gradleProperty ?: "MISSING"
}
