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
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  //  alias(libs.plugins.mavenPublish)
}

android {
  namespace = "com.uber.sdk.android.core"
  buildFeatures { buildConfig = true }

  defaultConfig {
    testApplicationId = "com.uber.sdk.android.core"
    buildConfigField("String", "VERSION_NAME", "\"${project.property("VERSION_NAME").toString()}\"")
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
  implementation(libs.uberCore) { exclude(group = "org.slf4j", module = "slf4j-log4j12") }
  implementation(libs.jsr305)
  implementation(libs.appCompat)
  implementation(libs.annotations)
  implementation(libs.chrometabs)

  testImplementation(libs.junit)
  testImplementation(libs.assertj)
  testImplementation(libs.mockito)
  testImplementation(libs.robolectric)
  testImplementation(project(":core-android"))
}
