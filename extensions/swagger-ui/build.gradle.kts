val edcVersion: String by project
val edcGroup: String by project

plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api("${edcGroup}:core-spi:${edcVersion}")
    api("${edcGroup}:policy-engine-spi:${edcVersion}")
    api("${edcGroup}:control-plane-spi:${edcVersion}")
    api("${edcGroup}:management-api-configuration:${edcVersion}")
    implementation("${edcGroup}:api-core:${edcVersion}")

    testImplementation("${edcGroup}:control-plane-core:${edcVersion}")
    testImplementation("${edcGroup}:junit:${edcVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")

    // Swagger dependencies
    implementation("io.swagger.core.v3:swagger-jaxrs2:2.2.6")
    implementation("io.swagger.core.v3:swagger-core:2.2.6")
    implementation("org.webjars:swagger-ui:3.51.1")
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
        }
    }
}
