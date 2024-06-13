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
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.mavenPublish)
}

android {
  namespace = "com.uber.sdk.android.rides"
  buildFeatures { buildConfig = true }

  defaultConfig {
    buildConfigField("String", "VERSION_NAME", "\"${project.property("VERSION_NAME").toString()}\"")
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

dependencies {
  implementation(libs.uberRides) { exclude(group = "org.slf4j", module = "slf4j-log4j12") }
  implementation(libs.jsr305)
  implementation(libs.appCompat)
  implementation(libs.annotations)
  implementation(libs.chrometabs)
  implementation(project(":core-android"))

  testImplementation(libs.junit)
  testImplementation(libs.assertj)
  testImplementation(libs.mockito)
  testImplementation(libs.robolectric)
  testImplementation(libs.guava)
  testImplementation(libs.wiremock)
  testImplementation(project(":core-android"))
}
