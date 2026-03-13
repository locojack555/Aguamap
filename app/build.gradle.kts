plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    signingConfigs {
        create("release_config") {
            storeFile = file("C:\\Users\\luisb\\Documents\\ClaveAguaMap")
            keyAlias = "key0"
            keyPassword = "123456"
            storePassword = "123456"
        }
    }
    namespace = "cat.copernic.aguamap1"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "cat.copernic.aguamap1"
        minSdk = 24
        targetSdk = 36
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
            signingConfig = signingConfigs.getByName("release_config")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    packaging {
        jniLibs {
            // Esta es la clave para solucionar el error de alineación
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(libs.androidx.navigation.compose)
    implementation(libs.geofire.android.common)
    implementation(platform(libs.firebase.bom))
    //implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.auth)
    implementation(libs.osmdroid.android)
    implementation(libs.accompanist.permissions)
    implementation(libs.play.services.location)
    implementation(libs.firebase.firestore)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.coil.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.compose.material3)
    implementation(libs.firebase.database.ktx)
    implementation(libs.androidx.compose.ui.text)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation("com.google.code.gson:gson:2.10.1")

    // Para calcular los Geohashes (necesarios para que las fuentes aparezcan en tu mapa)
    implementation("com.firebase:geofire-android-common:3.2.0")



    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.room.ktx)
// Cloudinary
    implementation(libs.cloudinary.android)

    // Para seleccionar imágenes de la galería
    implementation(libs.androidx.activity.compose.v1124)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}