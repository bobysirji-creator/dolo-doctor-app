plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val prototypeSigningStore = providers.environmentVariable("DOLO_SIGNING_STORE_FILE").orNull
val prototypeSigningStorePassword = providers.environmentVariable("DOLO_SIGNING_STORE_PASSWORD").orNull
val prototypeSigningKeyAlias = providers.environmentVariable("DOLO_SIGNING_KEY_ALIAS").orNull
val prototypeSigningKeyPassword = providers.environmentVariable("DOLO_SIGNING_KEY_PASSWORD").orNull
val prototypeSigningAvailable = listOf(
    prototypeSigningStore,
    prototypeSigningStorePassword,
    prototypeSigningKeyAlias,
    prototypeSigningKeyPassword
).all { !it.isNullOrBlank() }

android {
    namespace = "com.dolo.doctor"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dolo.doctor"
        minSdk = 26
        targetSdk = 35
        versionCode = 34
        versionName = "0.21.0-stage25c"
        buildConfigField("String", "DOLO_API_BASE_URL", "\"https://dolo-platform-api-prototype.onrender.com\"")
    }
    signingConfigs {
        if (prototypeSigningAvailable) {
            create("prototype") {
                storeFile = file(prototypeSigningStore!!)
                storePassword = prototypeSigningStorePassword
                keyAlias = prototypeSigningKeyAlias
                keyPassword = prototypeSigningKeyPassword
                storeType = "PKCS12"
            }
        }
    }

    buildTypes {
        if (prototypeSigningAvailable) {
            getByName("debug") { signingConfig = signingConfigs.getByName("prototype") }
            getByName("release") { signingConfig = signingConfigs.getByName("prototype") }
        }
    }

    buildFeatures { compose = true; buildConfig = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    lint {
        abortOnError = true
        checkDependencies = true
        htmlReport = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
