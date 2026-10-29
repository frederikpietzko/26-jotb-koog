plugins {
    kotlin("jvm") version "2.4.20"
    kotlin("plugin.spring") version "2.4.20"
    kotlin("plugin.jpa") version "2.4.20"
    kotlin("plugin.serialization") version "2.4.20"
    kotlin("plugin.power-assert") version "2.4.20"
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
    maven("https://repo.spring.io/milestone")
}

extra["springAiVersion"] = "2.1.0-M1"
val koogVersion = "1.3.0"

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("com.h2database:h2")

    // Spring AI - provides the ChatModel Koog talks through
    implementation("org.springframework.ai:spring-ai-starter-model-openai")

    // Koog - talks to the LLM through Spring AI's ChatModel
    implementation("ai.koog:koog-agents-jvm:$koogVersion")
    implementation("ai.koog:koog-spring-ai-v2-starter-model-chat:$koogVersion-beta")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.awaitility:awaitility")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
powerAssert {
    functions = listOf(
        "kotlin.assert",
        "kotlin.test.assertEquals",
        "kotlin.test.assertTrue",
        "kotlin.test.assertNotNull",
        "kotlin.test.assertContains",
    )
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// forward -Dkoog.* from the Gradle command line, e.g. ./gradlew bootRun -Dkoog.langfuse.enabled=true
tasks.bootRun {
    systemProperties(System.getProperties().filterKeys { it.toString().startsWith("koog.") }.mapKeys { it.key.toString() })
}
