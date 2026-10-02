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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = false
    }
}
