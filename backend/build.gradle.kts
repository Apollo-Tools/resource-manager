import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "7.0.0"
  jacoco
  id("io.freefair.lombok") version "6.5.1"
  pmd
}

group = "Apollo-Tools-RM"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.4.1"
val flywayVersion = "8.5.12"
val junitJupiterVersion = "5.7.0"
val postgresVersion = "42.6.0"
val hibernateVersion = "1.1.9.Final"

val launcherClassName = "at.uibk.dps.rm.Main"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  mainClass.set(launcherClassName)
}

dependencies {
  // vert.x
  implementation("io.vertx:vertx-core:$vertxVersion")
  implementation("io.vertx:vertx-web:$vertxVersion")
  implementation("io.vertx:vertx-pg-client:$vertxVersion")
  implementation("io.vertx:vertx-auth-oauth2:$vertxVersion")
  implementation("io.vertx:vertx-auth-jwt:$vertxVersion")
  implementation("io.vertx:vertx-config:$vertxVersion")
  implementation("io.vertx:vertx-service-proxy:$vertxVersion")
  implementation("io.vertx:vertx-web-openapi:$vertxVersion")
  implementation("io.vertx:vertx-rx-java3:$vertxVersion")
  implementation("io.vertx:vertx-rx-gen:$vertxVersion")
  annotationProcessor("io.vertx:vertx-rx-java3-gen:$vertxVersion")
  annotationProcessor("io.vertx:vertx-codegen:$vertxVersion:processor")
  annotationProcessor("io.vertx:vertx-service-proxy:$vertxVersion")

  // Password hashing
  implementation("de.mkammerer:argon2-jvm:2.11")

  // Json parsing
  implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-hibernate5:2.15.2")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
  // YAML parsing
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2")

  // K8S
  implementation("io.kubernetes:client-java:18.0.0")

  // DB
  implementation("com.ongres.scram:client:2.1")
  implementation("org.hibernate.reactive:hibernate-reactive-core:$hibernateVersion")
  implementation("org.flywaydb:flyway-core:$flywayVersion")
  implementation("org.postgresql:postgresql:$postgresVersion")

  // testing
  testImplementation("io.vertx:vertx-junit5:$vertxVersion")
  testImplementation("io.vertx:vertx-junit5-rx-java3:$vertxVersion")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
  testImplementation("org.assertj:assertj-core:3.23.1")
  testImplementation("org.mockito:mockito-inline:3.+")
  testImplementation("org.mockito:mockito-junit-jupiter:4.8.0")
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

jacoco {
  toolVersion = "0.8.8"
}

pmd {
  toolVersion="6.34.0"
  isConsoleOutput=true
  ruleSets = emptyList()
  ruleSetFiles = files("./ruleset.xml", "./ruleset_test.xml")
}

tasks.withType<ShadowJar> {
  manifest {
    attributes["Main-Class"] = "at.uibk.dps.rm.Main"
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.test {
  finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

tasks.jacocoTestReport {
  dependsOn(tasks.test) // tests are required to run before generating the report
  reports {
    xml.required.set(true)
    classDirectories.setFrom(
      files(classDirectories.files.map {
        fileTree(it) {
          exclude("**/*VertxEBProxy.*", "**/*VertxProxyHandler.*", "**/rxjava3/**")
        }
      }
    ))
  }
}
