plugins {
    id("com.android.application")
}

android {
    namespace = "cl.crm.clientes2"
    compileSdk = 35

    defaultConfig {
        applicationId = "cl.crm.clientes2"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "2.0-IA"
    }
}

dependencies {
    implementation("androidx.core:core:1.15.0")
}
