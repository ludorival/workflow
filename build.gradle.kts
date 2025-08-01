plugins {
    kotlin("jvm") version "2.1.10"
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("org.jreleaser") version "1.16.0"
}

group = "io.github.ludorival"
version = "1.0-SNAPSHOT"

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    
    // Kotlinx Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.13.4")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.13.4")
}

tasks.test {
    useJUnitPlatform()
}

// Publishing configuration
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "workflow"
            from(components["java"])
            
            versionMapping {
                usage("java-api") {
                    fromResolutionOf("runtimeClasspath")
                }
                usage("java-runtime") {
                    fromResolutionResult()
                }
            }
            
            pom {
                name.set("Workflow")
                description.set("A Kotlin workflow library")
                url.set("https://github.com/ludorival/workflow")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/ludorival/workflow/blob/main/LICENSE")
                    }
                }
                
                developers {
                    developer {
                        id.set("ludorival")
                        name.set("Ludovic Dorival")
                        email.set("ludorival@gmail.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/ludorival/workflow.git")
                    developerConnection.set("scm:git:ssh://github.com/ludorival/workflow.git")
                    url.set("https://github.com/ludorival/workflow")
                }
            }
        }
    }
    
    repositories {
        maven {
            url = uri("$buildDir/staging-deploy")
        }
    }
}

// Signing configuration
signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["mavenJava"])
    }
}

// JReleaser configuration
jreleaser {
    project {
        name.set("workflow")
        description.set("A Kotlin workflow library")
        longDescription.set("""
            A Kotlin library that provides workflow utilities and patterns
            for building robust and maintainable applications
        """.trimIndent())
        authors.set(listOf("Ludovic Dorival"))
        license.set("MIT License")
        copyright.set("Copyright (c) 2024 Ludovic Dorival")
        links {
            homepage.set("https://github.com/ludorival/workflow")
        }
    }
    
    signing {
        active.set(org.jreleaser.model.Active.ALWAYS)
        armored.set(true)
    }
} 