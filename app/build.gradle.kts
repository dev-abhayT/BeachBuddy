import java.util.Properties
import java.io.FileInputStream

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if(localPropertiesFile.exists()){
    localProperties.load(FileInputStream(localPropertiesFile))
}

fun getLocalProperty(key: String, fallback: String = ""): String {
    return localProperties.getProperty(key) ?: fallback
}


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android) version "2.1.0"
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
}



android {
    namespace = "com.example.beachbuddy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.beachbuddy"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {

        debug {
            val apiKey = getLocalProperty("GOOGLE_MAPS_API_KEY")
            val goApiKey = getLocalProperty("GO_MAPS_API_KEY")
            val fsqApiKey = getLocalProperty("FSQ_API_KEY")
            val fsqOldApiKey = getLocalProperty("FSQ_OLD_API_KEY")
            val geminiApiKey = getLocalProperty("GEMINI_API_KEY")
            buildConfigField("String","FSQ_OLD_API_KEY", "\"$fsqOldApiKey\"")
            buildConfigField("String", "GO_MAPS_API_KEY", "\"$goApiKey\"")
            buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"$apiKey\"")
            buildConfigField("String", "FSQ_API_KEY", "\"$fsqApiKey\"")
            buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
            manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = getLocalProperty("GOOGLE_MAPS_API_KEY")
            isDebuggable = true
        }
        release {
            val apiKey = getLocalProperty("GOOGLE_MAPS_API_KEY")
            val goApiKey = getLocalProperty("GO_MAPS_API_KEY")
            val fsqApiKey = getLocalProperty("FSQ_API_KEY")
            val fsqOldApiKey = getLocalProperty("FSQ_OLD_API_KEY")
            val geminiApiKey = getLocalProperty("GEMINI_API_KEY")
            buildConfigField("String","FSQ_OLD_API_KEY", "\"$fsqOldApiKey\"")
            buildConfigField("String", "GO_MAPS_API_KEY", "\"$goApiKey\"")
            buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"$apiKey\"")
            buildConfigField("String", "FSQ_API_KEY", "\"$fsqApiKey\"")
            buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")
            manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = getLocalProperty("GOOGLE_MAPS_API_KEY")

            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation(platform("com.google.firebase:firebase-bom:33.14.0")) //Firebase BoM
    implementation("com.google.firebase:firebase-analytics") //Firebase Analytics
    implementation("com.google.firebase:firebase-auth") //Firebase Auth Libraries
    implementation("androidx.viewpager2:viewpager2:1.1.0") //ViewPager2
    implementation(libs.material.v140) //Material Library

    //Credential Manager for Google Sign-In
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    //Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    //Maps SDK
    implementation("com.google.android.gms:play-services-maps:19.0.0")

    //Room DB
    implementation("androidx.room:room-runtime:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")

    //WorkManager
    implementation("androidx.work:work-runtime:2.10.2")
    implementation("androidx.work:work-runtime-ktx:2.10.2")

    //FusedLocationProviderClient
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    //Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    //ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")
    implementation("androidx.fragment:fragment-ktx:1.8.8")

    //Gemini
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.play.services.maps)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.fragment)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}