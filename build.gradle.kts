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


subprojects {

    val commonAndroidConfig: CommonExtension<*, *, *, *>.() -> Unit = {
        compileSdk = compileSdkVersionInt

        defaultConfig {
            minSdk = minSdkVersion
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            testApplicationId = "autodispose2.androidTest"
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
        testOptions { execution = "ANDROIDX_TEST_ORCHESTRATOR" }
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
                includes.from("README.md")
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
                val moduleMd = project.layout.projectDirectory.file("Module.md")
                if (moduleMd.asFile.exists()) {
                    includes.from(moduleMd)
                }
            }
        }

        configure<MavenPublishBaseExtension> {
            publishToMavenCentral(automaticRelease = true)
            signAllPublications()
        }
    }
}