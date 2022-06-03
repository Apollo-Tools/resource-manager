import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "Apollo-Tools-resource-manager"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
}

val vertxVersion = "4.3.1"
val mutinyVersion = "2.22.0"
val flywayVersion = "8.5.12"
val junitJupiterVersion = "5.7.0"
val postgresVersion = "42.3.3"
val hibernateVersion = "1.1.6.Final"

val mainVerticleName = "at.uibk.dps.rm.verticle.MainVerticle"
val launcherClassName = "io.vertx.core.Launcher"

val watchForChange = "src/**/*"
val doOnChange = "${projectDir}/gradlew classes"

application {
  mainClass.set(launcherClassName)
}

dependencies {
  // vert.x
  implementation("io.smallrye.reactive:smallrye-mutiny-vertx-core:$mutinyVersion")
  implementation("io.smallrye.reactive:smallrye-mutiny-vertx-web:$mutinyVersion")
  implementation("io.smallrye.reactive:smallrye-mutiny-vertx-pg-client:$mutinyVersion")
  implementation("io.smallrye.reactive:smallrye-mutiny-vertx-auth-oauth2:$mutinyVersion")
  implementation("io.smallrye.reactive:smallrye-mutiny-vertx-config:$mutinyVersion")

  // DB
  implementation("org.flywaydb:flyway-core:$flywayVersion")
  implementation("org.postgresql:postgresql:$postgresVersion")
  implementation("org.hibernate.reactive:hibernate-reactive-core:$hibernateVersion")

  // testing
  testImplementation("io.smallrye.reactive:smallrye-mutiny-vertx-junit5:$mutinyVersion")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<ShadowJar> {
  archiveClassifier.set("fat")
  manifest {
    attributes(mapOf("Main-Verticle" to mainVerticleName))
  }
  mergeServiceFiles()
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

tasks.withType<JavaExec> {
  args = listOf("run", mainVerticleName, "--redeploy=$watchForChange", "--launcher-class=$launcherClassName", "--on-redeploy=$doOnChange")
}
