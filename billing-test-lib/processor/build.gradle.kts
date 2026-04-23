plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":billing-test-lib:annotation"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.20-1.0.24")
}
