Version catelog was never a replacement for `buildSrc` its just there for better mangement for dependencies version, lets see how to reduce boilerplate in our buildScript wihtout using `buildSrc` 

Why avoid buildSrc and use this method ..? Making changes in buildSrc cause the entire project to be recompiled this way we can solve this problem

## Before
```kotlin
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension

plugins {
   alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.abhijith.gradleplugin"
    compileSdk = 34

    defaultConfig {
        ...
    }

    buildTypes {
       ...
    }
    compileOptions {... }
    kotlinOptions {...}
    buildFeatures {...}
    compileOptions {...}
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```
# After
```kotlin
plugins {
    id("android_lib")
}

android {
    namespace = "com.abhijith.mylibrary"
}

```

# Step 1 
create a folder   `build-logic`
add file `build-logic/settings.gradle.kts`
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":plugin")
```

# setp 2
add file `build-logic/build.gradle.kts`
```kotlin
import org.gradle.kotlin.dsl.extra

buildscript {
    val kotlinVersion by extra("1.9.20")

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

```

# step 3 
lets create a module `plugin` to reduce plugin boiler plate 
## step 3.1 
add this file in `build-logic/plugin/build.gradle.kts`
```kotlin
plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kgp)
    implementation(libs.androidLib)
    implementation(libs.androidApp)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
kotlinDslPluginOptions {
    jvmTarget.set("17")
}
```

# step 3.2 
lets extract common logic for android-app module for android-lib you can check source code in this project
add file at `build-logic/plugin/src/main/java/android_app.gradle.kts`
```kotlin
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.JavaVersion

plugins {
    id("com.android.application")
    id("kotlin-android")
}
val libs = the<LibrariesForLibs>()


android {
    compileSdk = 34

    defaultConfig {
        applicationId = "com.abhijith.gradleplugin"
        minSdk = 21
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

# step 4 
changes to our project level `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    includeBuild("build-logic") // 👈
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "WhatsInGradle"
include(":app")
include(":mylibrary")
```

# implementation in `app`
```kotlin
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension

plugins {
   id("android_app")
}

android {
    namespace = "com.abhijith.gradleplugin"

}

dependencies {
  
}
```
