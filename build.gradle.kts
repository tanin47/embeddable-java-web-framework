import org.jreleaser.model.Active
import org.jreleaser.model.Signing.Mode

plugins {
    `java-library`
    application
    `maven-publish`
    id("org.jreleaser") version "1.21.0"
    id("com.gradleup.shadow") version "9.2.2"
}

group = "tanin.ejwf"
version = "0.4.0"

description = "Embeddable Java Web Framework (EJWF)"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.renomad:minum:8.2.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.seleniumhq.selenium:selenium-java:4.36.0")
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    maxHeapSize = "1G"

    testLogging {
        events("passed")
    }
}

application {
    mainClass.set("tanin.ejwf.Main")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.tanin47"
            artifactId = "embeddable-java-web-framework"
            version = project.version.toString()
            artifact(tasks.shadowJar)
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set("Embeddable Java Web Framework")
                description.set("An example of Embeddable Java Web Framework that you can embed into your larger application.")
                url.set("https://github.com/tanin47/embeddable-java-web-framework")
                inceptionYear.set("2025")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://spdx.org/licenses/MIT.html")
                    }
                }
                developers {
                    developer {
                        id.set("tanin47")
                        name.set("Tanin Na Nakorn")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/tanin47/embeddable-java-web-framework.git")
                    developerConnection.set("scm:git:ssh://github.com/tanin47/embeddable-java-web-framework.git")
                    url.set("http://github.com/tanin47/embeddable-java-web-framework")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}

jreleaser {
    signing {
        mode = Mode.COMMAND
        active = Active.ALWAYS
        armored = true
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    setActive("ALWAYS")
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("build/staging-deploy")
                }
            }
        }
    }
}

tasks.shadowJar {
    archiveClassifier.set("") // Remove the suffix -all.
    relocate("com", "tanin.ejwf.com")
    exclude("META-INF/MANIFEST.MF")
    exclude("local_dev_marker.ejwf")

}

tasks.jar {
    manifest.attributes["Main-Class"] = "tanin.ejwf.Main"
}
