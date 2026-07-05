import com.google.protobuf.gradle.*

plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.protobuf") version "0.9.6"
    id("com.diffplug.spotless") version "8.4.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

val grpcVersion = "1.78.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
    all {
        resolutionStrategy.eachDependency {
            if (requested.group == "io.grpc") {
                useVersion(grpcVersion)
            }
        }
    }
}

repositories {
    mavenCentral()
}

val protocVersion = "4.35.1"
val grpcSpringBootVersion = "3.1.0.RELEASE"
val mapstructVersion = "1.6.3"
val logstashEncoderVersion = "8.0"

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // gRPC Spring Boot Starter
    implementation("net.devh:grpc-spring-boot-starter:$grpcSpringBootVersion")
    implementation("net.devh:grpc-server-spring-boot-starter:$grpcSpringBootVersion")

    // gRPC & Protobuf
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")
    implementation("io.grpc:grpc-services:$grpcVersion")
    implementation("com.google.protobuf:protobuf-java:$protocVersion")
    implementation("com.google.protobuf:protobuf-java-util:$protocVersion")

    // Jakarta annotation (for generated gRPC code)
    implementation("jakarta.annotation:jakarta.annotation-api")

    // Database
    runtimeOnly("com.h2database:h2")

    // MapStruct
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")

    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

    // Micrometer
    implementation("io.micrometer:micrometer-core")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("net.devh:grpc-client-spring-boot-starter:$grpcSpringBootVersion")
    testImplementation("io.grpc:grpc-testing:$grpcVersion")
    testImplementation("org.junit.platform:junit-platform-launcher")

    // JaCoCo runtime API for per-test coverage mapping
    testImplementation("org.jacoco:org.jacoco.core:0.8.15")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protocVersion"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                create("grpc")
            }
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        csv.required = true
    }
}

spotless {
    java {
        target("src/*/java/**/*.java")
        importOrder("java", "javax", "jakarta", "", "com.example", "\\#").wildcardsLast()
        removeUnusedImports()
        formatAnnotations()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.named("check") {
    dependsOn("spotlessCheck")
}

tasks.register("format") {
    group = "formatting"
    description = "Apply Spotless code formatting to all Java sources"
    dependsOn("spotlessApply")
}

tasks.register<Test>("coverageMap") {
    group = "verification"
    description = "Generate test-to-production-class coverage mapping reports"
    useJUnitPlatform()
    testClassesDirs = tasks.test.get().testClassesDirs
    classpath = tasks.test.get().classpath
    systemProperty("coverage.tracking.enabled", "true")
    outputs.upToDateWhen { false }
    configure<JacocoTaskExtension> {
        includes = listOf("com.example.filestore.*")
        isJmx = true
    }
}
