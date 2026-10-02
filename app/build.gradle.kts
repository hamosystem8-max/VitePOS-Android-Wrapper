// Version control: 0.2.1 | 2026-10-03
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.independentpostools.viteposwrapper"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.independentpostools.viteposwrapper"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "0.2.1"
    }
    buildFeatures { viewBinding = false }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
