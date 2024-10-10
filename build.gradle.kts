import org.asciidoctor.gradle.jvm.AsciidoctorTask

plugins {
    java
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
}

group = "io.hhplus"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}


repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("com.h2database:h2")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

val snippetsDir = file("build/generated-snippets")

tasks.named<Test>("test") {
    outputs.dir(snippetsDir)
//    doFirst {
//        delete(snippetsDir)
//    }
}

tasks.named<AsciidoctorTask>("asciidoctor") {
    inputs.dir(snippetsDir)
    dependsOn(tasks.test)
    attributes(
        mapOf("snippets" to snippetsDir.absolutePath)
    )
    outputOptions {
        setOutputDir(file("build/docs/asciidoc"))
    }
}

tasks.register<Copy>("copyRestDocs") {
    from("build/docs/asciidoc")
    into("src/main/resources/static/docs")
    dependsOn(tasks.withType<AsciidoctorTask>())
}
