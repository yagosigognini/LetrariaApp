plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "br.com.letrariaapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "br.com.letrariaapp"
        minSdk = 29
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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Dependências padrão do Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended-android:1.6.7")

    // Material Design - ADICIONE ESTA LINHA
    implementation(libs.material)

    // ViewModel - Essencial para a arquitetura MVVM
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")

    // NAVEGAÇÃO CORRETA PARA JETPACK COMPOSE
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Coil - Para carregar imagens da internet
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Dependências de teste
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}