import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "one.yufz.clipboard"
    compileSdk = 34

    defaultConfig {
        applicationId = "one.yufz.clipboard"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
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
    compileOnly("de.robv.android.xposed:api:82")
    implementation("com.airbnb.android:mavericks:3.0.8")
    implementation("com.airbnb.android:mavericks-compose:3.0.8")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("com.google.accompanist:accompanist-drawablepainter:0.32.0")
}