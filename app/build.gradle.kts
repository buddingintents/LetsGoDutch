import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.kotlin.compose)
}

val keystoreProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("keystore.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun signingProperty(propertyName: String, envName: String): String? =
    (keystoreProperties.getProperty(propertyName) ?: System.getenv(envName))
        ?.takeIf { it.isNotBlank() }

val releaseStoreFilePath = signingProperty("storeFile", "ANDROID_STORE_FILE")
    ?.removeSurrounding("\"")
    ?.removeSurrounding("'")
val releaseStorePassword = signingProperty("storePassword", "ANDROID_STORE_PASSWORD")
val releaseKeyAlias = signingProperty("keyAlias", "ANDROID_KEY_ALIAS")
val releaseKeyPassword = signingProperty("keyPassword", "ANDROID_KEY_PASSWORD")

val hasReleaseSigning = listOf(
    releaseStoreFilePath,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }

val isReleaseArtifactTaskRequested = gradle.startParameter.taskNames.any { taskName ->
    val normalizedTaskName = taskName.lowercase()
    normalizedTaskName.contains("assemblerelease") ||
        normalizedTaskName.contains("bundlerelease") ||
        normalizedTaskName.contains("packagerelease") ||
        normalizedTaskName.contains("publish")
}

if (!hasReleaseSigning && isReleaseArtifactTaskRequested) {
    throw GradleException(
        "Release signing is not configured. Add keystore.properties at the project root " +
            "or set ANDROID_STORE_FILE, ANDROID_STORE_PASSWORD, ANDROID_KEY_ALIAS, " +
            "and ANDROID_KEY_PASSWORD.",
    )
}

val gitCommitCount: Int? = runCatching {
    providers.exec {
        commandLine("git", "rev-list", "--count", "HEAD")
        isIgnoreExitValue = true
    }.standardOutput.asText.get().trim().toInt()
}.getOrNull()

val minimumPlayVersionCode = 17
val computedVersionCode = System.getenv("VERSION_CODE")?.toIntOrNull()
    ?: gitCommitCount
    ?: minimumPlayVersionCode
val resolvedVersionCode: Int = maxOf(computedVersionCode, minimumPlayVersionCode)

val resolvedVersionName: String = System.getenv("VERSION_NAME")
    ?: "1.2.$resolvedVersionCode"

android {
    namespace = "com.buddingintents.letsgodutch"
    compileSdk = 35

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = rootProject.file(requireNotNull(releaseStoreFilePath))
                storePassword = requireNotNull(releaseStorePassword)
                keyAlias = requireNotNull(releaseKeyAlias)
                keyPassword = requireNotNull(releaseKeyPassword)
            }
        }
    }

    defaultConfig {
        applicationId = "com.buddingintents.letsgodutch"
        minSdk = 26
        targetSdk = 35
        versionCode = resolvedVersionCode
        versionName = resolvedVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget("17")
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:designsystem"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:groups"))
    implementation(project(":feature:expenses"))
    implementation(project(":feature:ledger"))
    implementation(project(":feature:insights"))
    implementation(project(":feature:settlement"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.coil.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.google.play.services.auth)
    implementation(libs.google.play.services.ads)
    implementation(libs.google.play.app.update)
    implementation(libs.google.play.app.update.ktx)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
