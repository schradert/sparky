plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinSerialization)
    jacoco
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            
            // Serialization
            implementation(libs.kotlinx.serialization.json)
            
            // DateTime
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")
            
            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation("io.ktor:ktor-client-logging:2.3.7")
            
            // Note: Navigation Compose is not yet fully multiplatform compatible
            // Using simple state-based navigation instead
            
            // Koin
            implementation(libs.koin.core)
            
            // Image Loading
            implementation(libs.image.loader)
        }
        
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
            implementation(libs.camera.core)
            implementation(libs.camera.camera2)
            implementation(libs.camera.lifecycle)
            implementation(libs.camera.view)
            implementation(libs.datastore.preferences)
            implementation(libs.permissions.compose)
        }
        
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotest.framework.engine)
            implementation(libs.kotest.framework.datatest)
            implementation(libs.kotest.assertions.core)
            implementation(libs.kotest.property)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.ktor.client.mock)
        }
    }
}

android {
    namespace = "com.sparky.inventory"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
        unitTests.isReturnDefaultValues = true
    }
    
    // Test coverage configuration
    testCoverage {
        jacocoVersion = "0.8.10"
    }
}

// Jacoco configuration for test coverage
jacoco {
    toolVersion = "0.8.10"
}

tasks.register<JacocoReport>("testDebugUnitTestCoverage") {
    dependsOn("testDebugUnitTest")
    
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    
    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*"
    )
    
    val debugTree = fileTree("${project.layout.buildDirectory.get().asFile}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    
    val mainSrc = "${project.projectDir}/src/commonMain/kotlin"
    
    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree("${project.layout.buildDirectory.get().asFile}").include("**/*.exec"))
}