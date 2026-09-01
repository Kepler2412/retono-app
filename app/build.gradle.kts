import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

/*
 * El plugin google-services aborta la compilación si falta google-services.json.
 * Aplicarlo de forma incondicional haría que el repositorio no se pueda clonar
 * y ejecutar sin credenciales, así que se aplica solo cuando el archivo existe.
 *
 * Sin el archivo: la app compila y funciona en modo local; los registros quedan
 * en PENDIENTE. Con el archivo: se activa la sincronización con Firestore.
 */
val hayCredencialesFirebase = file("google-services.json").exists()
if (hayCredencialesFirebase) {
    apply(plugin = "com.google.gms.google-services")
} else {
    logger.warn(
        "AVISO: no se encontró app/google-services.json. " +
        "La app compilará en modo local, sin sincronización remota."
    )
}

// La URL del backend no se codifica en el fuente: se lee de local.properties,
// archivo excluido del control de versiones.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    namespace = "co.edu.ucn.retono"
    compileSdk = 34

    defaultConfig {
        applicationId = "co.edu.ucn.retono"
        minSdk = 26          // Android 8.0: cubre el parque de dispositivos objetivo
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0-MVP"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "API_BASE_URL",
            localProperties.getProperty("API_BASE_URL") ?: "\"https://localhost/api/v1/\""
        )

        // Permite que el código sepa en tiempo de ejecución si hay backend.
        buildConfigField("boolean", "FIREBASE_HABILITADO", hayCredencialesFirebase.toString())
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            // Sin applicationIdSuffix a propósito. El plugin google-services
            // busca en google-services.json un cliente cuyo package_name
            // coincida con el applicationId final; con el sufijo ".debug" la
            // compilación falla con "No matching client found".
            //
            // La alternativa sería registrar una segunda app Android en la
            // consola de Firebase para co.edu.ucn.retono.debug. Para un
            // proyecto de un solo desarrollador no compensa la complejidad.
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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

    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

// Room exporta el esquema a /schemas: permite versionar las migraciones y
// revisarlas en el control de versiones. Es una extensión de nivel superior,
// no va dentro del bloque android.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)
    // Provee Task.await(), el puente entre las APIs de Firebase y las corrutinas
    implementation(libs.kotlinx.coroutines.play.services)

    // Interfaz declarativa
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.ui.tooling)

    // Persistencia local: fuente única de verdad
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Cifrado en reposo
    implementation(libs.sqlcipher)
    implementation(libs.androidx.sqlite.ktx)
    implementation(libs.androidx.security.crypto)

    // Inyección de dependencias
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Sincronización diferida
    implementation(libs.androidx.work.runtime.ktx)

    // Backend: Firestore como receptor de la sincronización
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)

    // Hardware y multimedia
    implementation(libs.play.services.location)
    implementation(libs.coil.compose)
    implementation(libs.androidx.exifinterface)

    // Pruebas de la capa de dominio (JVM pura, sin emulador)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
}
