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
import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessExtensionPredeclare
import com.diffplug.spotless.LineEnding
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import java.net.URI
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

plugins {
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.mavenPublish) apply false
  alias(libs.plugins.kotlin.parcelize) apply false
  alias(libs.plugins.dokka)
  alias(libs.plugins.spotless)
}

val compileSdkVersionInt: Int = libs.versions.compileSdkVersion.get().toInt()
val targetSdkVersion: Int = libs.versions.targetSdkVersion.get().toInt()
val minSdkVersion: Int = libs.versions.minSdkVersion.get().toInt()
val jvmTargetVersion = libs.versions.jvmTarget

tasks.dokkaHtmlMultiModule {
  outputDirectory.set(rootDir.resolve("docs/api/2.x"))
  includes.from(project.layout.projectDirectory.file("README.md"))
}

val ktfmtVersion = libs.versions.ktfmt.get()

allprojects {
  apply(plugin = "com.diffplug.spotless")
  val spotlessFormatters: SpotlessExtension.() -> Unit = {
    lineEndings = LineEnding.PLATFORM_NATIVE

    format("misc") {
      target("**/*.md", "**/.gitignore")
      indentWithSpaces(2)
      trimTrailingWhitespace()
      endWithNewline()
    }
    kotlin {
      target("**/src/**/*.kt")
      targetExclude("spotless/copyright.kt")
      ktfmt(ktfmtVersion).googleStyle()
      licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
      trimTrailingWhitespace()
      endWithNewline()
    }
    kotlinGradle {
      target("*.kts")
      targetExclude("spotless/copyright.kt")
      ktfmt(ktfmtVersion).googleStyle()
      trimTrailingWhitespace()
      endWithNewline()
      licenseHeaderFile(
        rootProject.file("spotless/copyright.kt"),
        "(import|plugins|buildscript|dependencies|pluginManagement|dependencyResolutionManagement)",
      )
    }
  }
  configure<SpotlessExtension> {
    spotlessFormatters()
    if (project.rootProject == project) {
      predeclareDeps()
    }
  }
  if (project.rootProject == project) {
    configure<SpotlessExtensionPredeclare> { spotlessFormatters() }
  }
}

subprojects {
  val configureKotlin =
    Action<AppliedPlugin> {
      configure<KotlinProjectExtension> {
        val jvmCompilerOptions: KotlinJvmCompilerOptions.() -> Unit = {
          jvmTarget.set(jvmTargetVersion.map(JvmTarget::fromTarget))
          freeCompilerArgs.addAll("-Xjsr305=strict")
        }
        when (this) {
          is KotlinJvmProjectExtension -> compilerOptions(jvmCompilerOptions)
          is KotlinAndroidProjectExtension -> compilerOptions(jvmCompilerOptions)
        }
      }
    }
  pluginManager.withPlugin("org.jetbrains.kotlin.jvm", configureKotlin)
  pluginManager.withPlugin("org.jetbrains.kotlin.android", configureKotlin)
  val commonAndroidConfig: CommonExtension<*, *, *, *, *>.() -> Unit = {
    compileSdk = compileSdkVersionInt

    defaultConfig {
      minSdk = minSdkVersion
      testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
      sourceCompatibility = JavaVersion.toVersion(jvmTargetVersion.get())
      targetCompatibility = JavaVersion.toVersion(jvmTargetVersion.get())
    }
    lint {
      checkTestSources = true
      val lintXml = file("lint.xml")
      if (lintXml.exists()) {
        lintConfig = lintXml
      }
    }
  }

  pluginManager.withPlugin("com.android.library") {
    project.configure<LibraryExtension> {
      commonAndroidConfig()
      defaultConfig { consumerProguardFiles("consumer-proguard-rules.txt") }
      testBuildType = "release"
      configure<LibraryAndroidComponentsExtension> {
        beforeVariants(selector().withBuildType("debug")) { builder -> builder.enable = false }
      }
    }
  }

  pluginManager.withPlugin("com.android.application") {
    project.configure<ApplicationExtension> {
      commonAndroidConfig()
      configure<ApplicationAndroidComponentsExtension> {
        // Only debug enabled for this one
        beforeVariants { builder ->
          builder.enable = builder.buildType != "release"
          builder.enableAndroidTest = false
        }

        buildTypes { getByName("debug") { enableUnitTestCoverage = true } }
      }
    }
  }

  pluginManager.withPlugin("com.vanniktech.maven.publish") {
    project.apply(plugin = "org.jetbrains.dokka")

    tasks.withType<DokkaTaskPartial>().configureEach {
      outputDirectory.set(buildDir.resolve("docs/partial"))
      moduleName.set(project.property("POM_ARTIFACT_ID").toString())
      moduleVersion.set(project.property("VERSION_NAME").toString())
      dokkaSourceSets.configureEach {
        skipDeprecated.set(true)
        suppressGeneratedFiles.set(true)
        suppressInheritedMembers.set(true)
        externalDocumentationLink {
          url.set(URI("https://kotlin.github.io/kotlinx.coroutines/index.html").toURL())
        }
        perPackageOption {
          // language=RegExp
          matchingRegex.set(".*\\.internal\\..*")
          suppress.set(true)
        }
        val moduleMd = project.layout.projectDirectory.file("README.md")
        if (moduleMd.asFile.exists()) {
          includes.from(moduleMd)
        }
      }
    }

    configure<MavenPublishBaseExtension> {
      publishToMavenCentral(automaticRelease = false)
      signAllPublications()
    }
  }
}
