plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.tathanhloc.lokistore"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tathanhloc.lokistore"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

implementation("com.github.bumptech.glide:glide:4.12.0")
implementation("com.google.android.material:material:1.5.0")
implementation("androidx.slidingpanelayout:slidingpanelayout:1.1.0")
implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.google.android.gms:play-services-maps:18.0.1")
    implementation("androidx.biometric:biometric:1.1.0")


}