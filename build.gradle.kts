import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.31"
    `maven-publish`
    signing
    id("io.gitlab.arturbosch.detekt") version "1.16.0"
}

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    version = System.getenv("RELEASE_VERSION") ?: "local"
    group = "br.com.guiabolso"

    dependencies {
        // Kotlin
        implementation(kotlin("reflect"))

        // Events Protocol
        implementation("br.com.guiabolso:events-core:5.0.8")

        // SLF4J
        implementation("org.slf4j:slf4j-api:1.7.30")

        // JUnit
        testImplementation("junit:junit:4.+")
        
        // Mockito
        testImplementation("org.mockito:mockito-core:3.5.11")
        testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    }
    
    repositories {
        mavenCentral()
    }
    

    tasks.withType<KotlinCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
        kotlinOptions {
            useIR = true
            jvmTarget = "1.8"
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    }

    val sourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.getByName("main").allSource)
    }

    val javadoc = tasks.named("javadoc")
    val javadocsJar by tasks.creating(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles java doc to jar"
        archiveClassifier.set("javadoc")
        from(javadoc)
    }

    publishing {

        repositories {
            maven {
                url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                credentials {
                    username = System.getenv("OSSRH_USERNAME")
                    password = System.getenv("OSSRH_PASSWORD")
                }
            }
        }

        publications.register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(javadocsJar)
            artifact(sourcesJar.get())
            artifactId = "hyperloop-${this@allprojects.name}"

            pom {
                name.set("Hyperloop")
                description.set("Hyperloop")
                url.set("https://github.com/GuiaBolso/Hyperloop")

                scm {
                    url.set("https://github.com/GuiaBolso/Hyperloop")
                    connection.set("scm:git:https://github.com/GuiaBolso/Hyperloop")
                }

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }

                developers {
                    developer {
                        id.set("Guiabolso")
                        name.set("Guiabolso")
                    }
                }
            }
        }

    }

    signing {
        val signingKey: String? by project
        val signingPassword: String? by project

        useGpgCmd()
        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
        }

        sign((extensions.getByName("publishing") as PublishingExtension).publications)
    }
}

repositories {
    mavenCentral()
}