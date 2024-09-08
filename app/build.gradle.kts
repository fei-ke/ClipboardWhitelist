import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    id("org.lsposed.lsplugin.jgit") version "1.1"
}

val repo = jgit.repo()
val commitCount = (repo?.commitCount("refs/remotes/origin/master") ?: 1)
val latestTag = repo?.latestTag?.removePrefix("v") ?: "0.0.0"
val verCode by extra(commitCount)
val verName by extra(latestTag)

android {
    namespace = "one.yufz.clipboard"
    compileSdk = 34

    defaultConfig {
        applicationId = "one.yufz.clipboard"
        minSdk = 29
        targetSdk = 34
        versionCode = verCode
        versionName = verName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    signingConfigs {
        create("release") {
            val localProperties = File(rootDir, "local.properties")
            if (localProperties.exists()) {
                val properties = Properties()
                properties.load(localProperties.inputStream())
                val path = properties.getProperty("STORE_FILE_PATH")
                if (path != null && file(path).exists()) {
                    storeFile = file(path)
                    storePassword = properties.getProperty("STORE_PASSWORD")
                    keyAlias = properties.getProperty("KEY_ALIAS")
                    keyPassword = properties.getProperty("KEY_PASSWORD")
                }
            }
        }
    }

    buildTypes {
        debug {
            signingConfigs["release"].apply {
                if (storeFile?.exists() == true) {
                    signingConfig = this
                }
            }

        }
        release {
            signingConfigs["release"].apply {
                if (storeFile?.exists() == true) {
                    signingConfig = this
                }
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    compileOnly(fileTree("compile-libs") { include("*.jar") })

    implementation("com.airbnb.android:mavericks:3.0.9")
    implementation("com.airbnb.android:mavericks-compose:3.0.9")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("com.google.accompanist:accompanist-drawablepainter:0.34.0")
}