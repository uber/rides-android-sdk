plugins {
    alias(libs.plugins.android.library)
}

android { namespace = "com.uber.sdk2.auth" }

dependencies {

    implementation(libs.appcompat.v7)
    testImplementation(libs.junit.junit)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.espresso.core)
}