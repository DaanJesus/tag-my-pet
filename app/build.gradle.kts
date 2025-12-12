plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    // Plugins adicionais necessários para o Hilt (Injeção de Dependência)
    alias(libs.plugins.ksp)
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    // Plugin opcional mas recomendado para gerenciar chaves de API (Maps) com segurança
    // id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.tagmypet"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tagmypet"
        minSdk = 26
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    // --- DEPENDÊNCIAS ORIGINAIS (DO CATALOG) ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.play.services.location)

    // Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // ======================================================
    // === NOVAS DEPENDÊNCIAS (TAG MY PET) ===
    // ======================================================

    // 1. INJEÇÃO DE DEPENDÊNCIA (HILT)
    // Essencial para MVVM e organização da arquitetura
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // 2. NETWORKING (RETROFIT + MOSHI)
    // Para comunicação com sua API NodeJS
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.logging.interceptor)

    // 3. Socket.io
    // Para tornar a rede em real time
    implementation(libs.socket.io.client)

    // DataStore
    // Para salvar o token
    implementation(libs.androidx.datastore.preferences)

    // 4. IMAGENS (COIL)
    // Carregamento assíncrono de fotos dos pets
    implementation(libs.coil.compose)

    // 5. CÂMERA E QR CODE (CAMERAX + ML KIT)
    // Para a funcionalidade "Escanear Tag"
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.barcode.scanning)
    implementation(libs.guava)

    // 6. MAPAS (GOOGLE MAPS)
    // Para rastreamento Premium e busca de comércios
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)

    // 7. UTILITÁRIOS DE UI
    // Ícones extras (necessário para patinhas, configurações, etc)
    implementation(libs.androidx.material.icons.extended)

    // 8. Stripe
    // Gateway de pagamento
    implementation(libs.stripe.android)

    // 9. Ads Google
    // Serviços de Propagandas
    implementation(libs.play.services.ads)

    // 10. Firebase
    // Push Notifications
    implementation(libs.firebase.messaging.ktx)
    implementation(platform(libs.google.firebase.bom))
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.kotlinx.coroutines.play.services)
}