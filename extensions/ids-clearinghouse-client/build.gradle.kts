plugins {
    `java-library`
    `maven-publish`
}

val edcVersion: String by project
val edcGroup: String by project
val jupiterVersion: String by project
val mockitoVersion: String by project
val assertj: String by project
val okHttpVersion: String by project
val jsonVersion: String by project

repositories {
    flatDir {
        dirs("libs/fraunhofer")
    }
}

dependencies {
    implementation("${edcGroup}:control-plane-core:${edcVersion}")
    //implementation("${edcGroup}:ids-spi:${edcVersion}")
    //implementation("${edcGroup}:ids-api-multipart-dispatcher-v1:${edcVersion}")
    //implementation("${edcGroup}:ids-api-configuration:${edcVersion}")
    //implementation("${edcGroup}:ids-jsonld-serdes:${edcVersion}")
    implementation("${edcGroup}:http-spi:${edcVersion}")

    implementation(":infomodel-java-4.1.3")
    implementation(":infomodel-util-4.0.4")

    implementation("com.squareup.okhttp3:okhttp:${okHttpVersion}")
    implementation("org.json:json:${jsonVersion}")
    implementation("javax.validation:validation-api:2.0.1.Final")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
    implementation("org.glassfish.jersey.media:jersey-media-multipart:3.1.3")

    testImplementation("org.assertj:assertj-core:${assertj}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${jupiterVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
}


tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

val sovityEdcExtensionGroup: String by project
group = sovityEdcExtensionGroup

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
        }
    }
}
