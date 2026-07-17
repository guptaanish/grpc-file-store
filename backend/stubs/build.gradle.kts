import com.google.protobuf.gradle.*

// Publishable artifact containing the generated gRPC/Protobuf Java stubs for the
// FileStore service. Other JVM services depend on this jar for service-to-service
// integration instead of copying the .proto or regenerating stubs themselves.
plugins {
    `java-library`
    `maven-publish`
    id("com.google.protobuf") version "0.9.6"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

val grpcVersion = "1.78.0"
val protocVersion = "4.35.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "io.grpc") {
            useVersion(grpcVersion)
        }
    }
}

dependencies {
    // Exposed as `api` so consumers get the gRPC/Protobuf runtime transitively
    // and can compile against and invoke the generated stubs.
    api("io.grpc:grpc-protobuf:$grpcVersion")
    api("io.grpc:grpc-stub:$grpcVersion")
    api("com.google.protobuf:protobuf-java:$protocVersion")
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

// Bundle the .proto sources in the jar so consumers can regenerate stubs for
// other languages if needed.
tasks.named<Jar>("jar") {
    from("src/main/proto") {
        into("proto")
    }
}

publishing {
    publications {
        create<MavenPublication>("stubs") {
            artifactId = "grpc-file-store-stubs"
            from(components["java"])
        }
    }
    repositories {
        // Default target: the local Maven repo (~/.m2). Override with a real
        // repository (Nexus/Artifactory/GitHub Packages) for shared consumption.
        mavenLocal()
    }
}
