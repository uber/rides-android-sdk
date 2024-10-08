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
    buildConfigField("String", "VERSION_NAME", "\"${project.property("VERSION_NAME").toString()}\"")
  }
  sourceSets { getByName("main") { java.srcDirs("src/main/java") } }
  buildTypes { getByName("debug") { matchingFallbacks += listOf("release") } }
  testOptions {
    execution = "ANDROIDX_TEST_ORCHESTRATOR"
    unitTests { isIncludeAndroidResources = true }
  }
}

dependencies {
  implementation(libs.appCompat)
  implementation(libs.retrofit)
  implementation(libs.retrofit.moshi)
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
