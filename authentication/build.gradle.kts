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
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.parcelize)
  alias(libs.plugins.spotless)
  alias(libs.plugins.mavenPublish)
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions {
    // Lint forces its embedded kotlin version, so we need to match it.
    apiVersion.set(KotlinVersion.KOTLIN_1_9)
    languageVersion.set(KotlinVersion.KOTLIN_1_9)
    jvmTarget.set(libs.versions.jvmTarget.map(JvmTarget::fromTarget))
  }
}

android {
  namespace = "com.uber.sdk2.auth"
  buildFeatures { buildConfig = true }

  defaultConfig {
    buildConfigField("String", "VERSION_NAME", "\"${project.property("VERSION_NAME").toString()}\"")
  }
  defaultConfig {
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    consumerProguardFiles("consumer-proguard-rules.txt")
  }

  buildTypes { release { isMinifyEnabled = false } }

  buildFeatures { compose = true }
  composeOptions { kotlinCompilerExtensionVersion = "1.5.11" }
}

dependencies {
  implementation(libs.androidx.ui.tooling.preview.android)
  val composeBom = platform(libs.compose.bom)
  implementation(composeBom)
  implementation(libs.androidx.compose.foundation)
  implementation(libs.material3)
  implementation(libs.appCompat)
  implementation(libs.chrometabs)
  implementation(libs.material)
  implementation(libs.moshi.kotlin)
  implementation(libs.retrofit)
  implementation(libs.retrofit.moshi)
  implementation(project(":core"))
  testImplementation(libs.junit.junit)
  testImplementation(libs.mockito.kotlin)
  testImplementation(libs.robolectric)
  testImplementation(libs.kotlin.coroutines.test)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.espresso.espresso.core)
}
