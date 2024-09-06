plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.clientapp"
    compileSdk = 34
    
    defaultConfig {
        applicationId = "com.example.clientapp"
        minSdk = 34
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
    
    flavorDimensions += "version"
    
    productFlavors {
        create("flavor1") {
            dimension = "version"
            applicationIdSuffix = ".flavor1"
            versionNameSuffix = "-flavor1"
        }
        
        create("flavor2") {
            dimension = "version"
            applicationIdSuffix = ".flavor2"
            versionNameSuffix = "-flavor2"
        }
    }
    buildFeatures {
        aidl = true
    }
}

dependencies {
    
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    
    // Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.1")
    
    // Material
    implementation("com.google.android.material:material:1.10.0")
    
    // Mockito
    
    androidTestImplementation("org.mockito:mockito-android:5.13.0")
    androidTestImplementation("android.arch.core:core-testing:1.1.0")
    androidTestImplementation("androidx.navigation:navigation-testing:2.3.5")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:3.2.0")


// For Robolectric tests.
    testImplementation("com.google.dagger:hilt-android-testing:2.48")
// ...with Kotlin.
    kaptTest("com.google.dagger:hilt-android-compiler:2.48")


// For instrumented tests.
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48")
// ...with Kotlin.
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.48")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
//    testImplementation(kotlin("test"))
//    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-params
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.0")
// https://mvnrepository.com/artifact/org.mockito/mockito-core
    testImplementation("org.mockito:mockito-core:3.11.1")
    testImplementation("org.mockito:mockito-junit-jupiter:3.11.1")
    testImplementation("org.mockito:mockito-inline:4.5.1") // or latest version
    
    // https://mvnrepository.com/artifact/io.mockk/mockk
    testImplementation("io.mockk:mockk:1.11.0")
// https://mvnrepository.com/artifact/org.junit.platform/junit-platform-runner
    
    testImplementation("org.junit.platform:junit-platform-suite-api:1.7.0")
    testImplementation("org.junit.platform:junit-platform-runner:1.2.0")
}