import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt") // ✅ KAPT varsa şart
    id("com.google.gms.google-services")
}

android {
    namespace = "com.ilayda.yurtdolap"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.ilayda.yurtdolap"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    // ✅ Java 1.8'e sabitle
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // ✅ Kotlin 1.8'e sabitle
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

// ✅ Bazı projelerde kapt 21’e sapıtıyor; bunu da zorla 1.8 yapıyoruz
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")

    implementation("com.google.android.material:material:1.12.0")

    // Eğer Glide kullanıyorsan (IlanAdapter’da var):
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")
}
