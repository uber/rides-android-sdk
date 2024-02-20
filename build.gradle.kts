/*
 * Copyright (C) 2024. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.net.URI

plugins {
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.mavenPublish) apply false
    alias(libs.plugins.dokka)
}

val compileSdkVersionInt: Int = libs.versions.compileSdkVersion.get().toInt()
val targetSdkVersion: Int = libs.versions.targetSdkVersion.get().toInt()
val minSdkVersion: Int = libs.versions.minSdkVersion.get().toInt()
val jvmTargetVersion = libs.versions.jvmTarget

tasks.dokkaHtmlMultiModule {
    outputDirectory.set(rootDir.resolve("docs/api/2.x"))
    includes.from(project.layout.projectDirectory.file("README.md"))
}

subprojects {

    val commonAndroidConfig: CommonExtension<*, *, *, *>.() -> Unit = {
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
                    builder.enableUnitTest = false
                }
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