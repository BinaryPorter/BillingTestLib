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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = project.property("LIBRARY_GROUP_ID") as String
            artifactId = project.property("LIBRARY_ANNOTATION_ARTIFACT_ID") as String
            version = project.property("LIBRARY_VERSION") as String
        }
    }
}
