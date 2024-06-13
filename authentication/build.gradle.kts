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
}

dependencies {
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
