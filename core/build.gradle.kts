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
}

android {
  namespace = "com.uber.sdk2.core"
  buildFeatures.buildConfig = true
  defaultConfig {
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    buildConfigField("String", "VERSION_NAME", "\"${project.property("VERSION_NAME").toString()}\"")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  buildFeatures { compose = true }
  composeOptions { kotlinCompilerExtensionVersion = "1.5.11" }
}

dependencies {
  val composeBom = platform(libs.compose.bom)
  implementation(composeBom)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.material3)
  implementation(libs.chrometabs)
  implementation(libs.core.ktx)
  implementation(libs.appCompat)
  debugImplementation(libs.androidx.ui.tooling)
  testImplementation(libs.junit.junit)
  testImplementation(libs.robolectric)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.espresso.espresso.core)
}
