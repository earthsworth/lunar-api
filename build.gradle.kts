plugins {
	kotlin("jvm") version "2.1.0"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.4.2"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.google.protobuf") version "0.9.4"
    id("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"
}

val springCloudVersion by extra("2024.0.0")

group = "org.cubewhy"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

avro {
    outputCharacterEncoding = "UTF-8"
}

repositories {
    maven("https://packages.confluent.io/maven/")
    mavenCentral()
}

dependencies {
    protobuf(files("proto"))

    implementation("com.opencsv:opencsv:5.10")
    implementation("com.auth0:java-jwt:4.4.0")
    implementation("io.confluent:kafka-streams-avro-serde:7.8.0")
    implementation("io.confluent:kafka-schema-registry-client:7.8.0")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
	implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
	implementation("com.google.protobuf:protobuf-kotlin:4.30.0-RC1")
    implementation("com.google.protobuf:protobuf-java-util:4.30.0-RC1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.springframework.cloud:spring-cloud-stream")
    implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka-reactive")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.54")
    implementation("org.springframework.boot:spring-boot-starter-security")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Jar> {
    exclude("**/*.proto")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

protobuf {
	protoc {
		artifact = "com.google.protobuf:protoc:4.30.0-RC1"
	}
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

tasks.withType<Test> {
	useJUnitPlatform()
}
