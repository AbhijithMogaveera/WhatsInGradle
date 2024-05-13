[Version Catalogue
](https://github.com/AbhijithMogaveera/WhatsInGradle/blob/master/VersionCatalogue.md)

Version catalogue was never a replacement for `buildSrc` its just there for better mangement for dependencies version, lets see how to reduce boilerplate in our buildScript wihtout using `buildSrc` 

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
## Here is how my versin catalogue looks like
```kotlin
[versions]
agp = "8.4.0"
kotlin = "1.9.23"
coreKtx = "1.13.1"
junit = "4.13.2"
junitVersion = "1.1.5"
espressoCore = "3.5.1"
appcompat = "1.6.1"
material = "1.12.0"
constraintlayout = "2.1.4"
navigationFragmentKtx = "2.7.7"
navigationUiKtx = "2.7.7"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
androidx-constraintlayout = { group = "androidx.constraintlayout", name = "constraintlayout", version.ref = "constraintlayout" }
androidx-navigation-fragment-ktx = { group = "androidx.navigation", name = "navigation-fragment-ktx", version.ref = "navigationFragmentKtx" }
androidx-navigation-ui-ktx = { group = "androidx.navigation", name = "navigation-ui-ktx", version.ref = "navigationUiKtx" }
androidLib = { group = "com.android.library", name = "com.android.library.gradle.plugin", version.ref = "agp" }
androidApp = { group = "com.android.application", name = "com.android.application.gradle.plugin", version.ref = "agp" }
kgp = { group = "org.jetbrains.kotlin", name = "kotlin-gradle-plugin", version.ref = "kotlin" }
[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
jetbrains-kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
android-library = { id = "com.android.library", version.ref = "agp" }

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
