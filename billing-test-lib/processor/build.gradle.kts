plugins {
    kotlin("jvm")
    `maven-publish`
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = project.property("LIBRARY_GROUP_ID") as String
            artifactId = project.property("LIBRARY_PROCESSOR_ARTIFACT_ID") as String
            version = project.property("LIBRARY_VERSION") as String
        }
    }
}
