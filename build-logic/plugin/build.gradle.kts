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