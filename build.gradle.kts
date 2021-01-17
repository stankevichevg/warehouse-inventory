
import com.google.protobuf.gradle.generateProtoTasks
import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.ofSourceSet
import com.google.protobuf.gradle.plugins
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc
import dev.bombinating.gradle.jooq.database
import dev.bombinating.gradle.jooq.generator
import dev.bombinating.gradle.jooq.jdbc
import dev.bombinating.gradle.jooq.target
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.math.BigDecimal.valueOf
import java.util.Collections.singletonList

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    idea
    `java-library`
    checkstyle
    jacoco
    eclipse
    `maven-publish`
    id("com.google.protobuf") version "0.8.13"
    id("org.flywaydb.flyway") version "5.2.4"
    id("dev.bombinating.jooq-codegen") version "1.7.0"
    application
}

repositories {
    maven("https://plugins.gradle.org/m2/")
}

defaultTasks("clean", "build")

project.group = "com.xxx"
project.version = file("version.txt").readText(Charsets.UTF_8).trim()

val Project.gprUsername: String? get() = this.properties["gprUsername"] as String?
val Project.gprPassword: String? get() = this.properties["gprPassword"] as String?

tasks.jar {
    enabled = false
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        mavenLocal()
    }
}

object Versions {
    const val checkstyle = "8.28"
    const val hamcrest = "2.2"
    const val mockito = "3.2.4"
    const val jacoco = "0.8.2"
    const val junit5 = "5.6.0"
    const val logback = "1.2.3"
    const val potobuf = "3.13.0"
    const val grpc = "1.33.0"
    const val jackson = "2.12.1"
    const val javaxAnnotation = "1.3.2"
    const val postgres = "42.2.18"
    const val hikaricp = "3.4.5"
    const val jooq = "3.14.4"
    const val flyway = "6.5.6"
    const val guice = "4.2.3"
}

subprojects {

    apply(plugin = "idea")
    apply(plugin = "checkstyle")
    apply(plugin = "jacoco")
    apply(plugin = "eclipse")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = project.group
    version = project.version

    dependencies {
        testImplementation("org.hamcrest", "hamcrest", Versions.hamcrest)
        testImplementation("org.mockito", "mockito-junit-jupiter", Versions.mockito)
        testImplementation("org.junit.jupiter", "junit-jupiter-api", Versions.junit5)
        testImplementation("org.junit.jupiter", "junit-jupiter-params", Versions.junit5)
        testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", Versions.junit5)

        implementation("ch.qos.logback", "logback-classic", Versions.logback)

    }

    tasks.jar {
        enabled = true
    }

    checkstyle {
        toolVersion = Versions.checkstyle
        sourceSets = singletonList(project.sourceSets.main.get())
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
        finalizedBy("jacocoTestReport")
    }
    jacoco {
        toolVersion = Versions.jacoco
    }
    tasks.jacocoTestReport {
        reports {
            xml.isEnabled = true
        }
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
        options.isDeprecation = true
    }
    tasks.compileTestJava {
        options.encoding = "UTF-8"
        options.isDeprecation = true
    }
    tasks.test {
        testLogging {
            showStandardStreams = true
            exceptionFormat = TestExceptionFormat.FULL
        }
        systemProperties(mapOf(
            // ADD COMMON SYSTEM PROPERTIES FOR TESTS HERE
            "exampleProperty" to "exampleValue"
        ))
        environment(mapOf(
            "exampleProperty" to "exampleValue"
        ))
        reports.html.isEnabled = false // Disable individual test reports
    }

    tasks.javadoc {
        title = "<h1>Price Service</h1>"
    }

    publishing {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/stankevichevg/warehouse-inventory")
                credentials {
                    username = project.gprUsername
                    password = project.gprPassword
                }
            }
        }
        publications {
            create<MavenPublication>("WarehouseInventory") {
                from(components["java"])
                pom {
                    name.set("Warehouse Inventory gRPC service")
                    url.set("https://github.com/stankevichevg/warehouse-inventory.git")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("stankevichevg")
                            name.set("Evgenii Stankevich")
                            email.set("stankevich.evg@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/stankevichevg/warehouse-inventory.git")
                        developerConnection.set("scm:git:ssh://github.com/stankevichevg/warehouse-inventory.git")
                        url.set("https://github.com/stankevichevg/warehouse-inventory")
                    }
                }
            }
        }
    }
}

project(":inventory-api") {

    apply(plugin = "java")
    apply(plugin = "com.google.protobuf")

    dependencies {
        api("javax.annotation", "javax.annotation-api", Versions.javaxAnnotation)
        api("io.grpc", "grpc-stub", Versions.grpc)
        api("io.grpc", "grpc-protobuf", Versions.grpc)
    }

    protobuf {
        protoc {
            artifact = "com.google.protobuf:protoc:" + Versions.potobuf
        }
        plugins {
            id("grpc") {
                artifact = "io.grpc:protoc-gen-grpc-java:" + Versions.grpc
            }
        }
        generateProtoTasks {
            ofSourceSet("main").forEach {
                it.plugins { id("grpc") }
            }
        }
    }
}

project(":inventory-service") {

    apply(plugin = "org.flywaydb.flyway")
    apply(plugin = "dev.bombinating.jooq-codegen")
    apply(plugin = "application")

    buildscript {
        repositories {
            mavenCentral()
        }
        dependencies {
            classpath("org.postgresql", "postgresql", Versions.postgres)
        }
    }

    dependencies {
        implementation(project(":inventory-api"))

        implementation("io.grpc", "grpc-stub", Versions.grpc)
        implementation("io.grpc", "grpc-protobuf", Versions.grpc)
        implementation("io.grpc", "grpc-netty-shaded", Versions.grpc)

        implementation("com.google.inject", "guice", Versions.guice)
        implementation("org.postgresql", "postgresql", Versions.postgres)
        implementation("com.zaxxer", "HikariCP", Versions.hikaricp)
        implementation("org.jooq", "jooq", Versions.hikaricp)

        jooqRuntime("org.postgresql", "postgresql", Versions.postgres)

    }

    flyway {
        url = System.getenv("DB_URL")
        user = System.getenv("DB_USER")
        password = System.getenv("DB_PASSWORD")
        baselineOnMigrate = true
    }

    jooq {
        jdbc {
            driver = "org.postgresql.Driver"
            url = flyway.url
            user = flyway.user
            password = flyway.password
        }
        generator {
            name = "org.jooq.codegen.DefaultGenerator"
            database {
                name = "org.jooq.meta.postgres.PostgresDatabase"
                inputSchema = "public"
                includes = ".*"
                excludes = "flyway_schema_history"
            }
            target {
                packageName = "com.xxx.inventory.sql"
                directory = "build/generated/jooq"
            }
        }
    }
    
    application {
        mainClass.set("com.xxx.inventory.InventoryServerBoot")
    }

    sourceSets.getByName("main") {
        java.srcDir("build/generated/jooq")
    }
    tasks.getByName("jooq").dependsOn(tasks.getByName("flywayMigrate"))

}

project(":inventory-data-uploader") {

    apply(plugin = "java")

    dependencies {
        api(project(":inventory-api"))
        implementation("io.grpc", "grpc-netty-shaded", Versions.grpc)
        
        implementation("com.fasterxml.jackson.core", "jackson-core", Versions.jackson)
        implementation("com.fasterxml.jackson.core", "jackson-annotations", Versions.jackson)
        implementation("com.fasterxml.jackson.core", "jackson-databind", Versions.jackson)
    }
}

val jacocoAggregateMerge by tasks.creating(JacocoMerge::class) {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    executionData(
        project(":inventory-service").buildDir.absolutePath + "/jacoco/test.exec"
    )
    dependsOn(
        ":inventory-service:test"
    )
}

@Suppress("UnstableApiUsage")
val jacocoAggregateReport by tasks.creating(JacocoReport::class) {
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    executionData(jacocoAggregateMerge.destinationFile)
    reports {
        xml.isEnabled = true
    }
    additionalClassDirs(files(subprojects.flatMap { project ->
        listOf("java", "kotlin").map { project.buildDir.path + "/classes/$it/main" }
    }))
    additionalSourceDirs(files(subprojects.flatMap { project ->
        listOf("java", "kotlin").map { project.file("src/main/$it").absolutePath }
    }))
    dependsOn(jacocoAggregateMerge)
}

tasks {
    jacocoTestCoverageVerification {
        executionData.setFrom(jacocoAggregateMerge.destinationFile)
        violationRules {
            rule {
                limit {
                    minimum = valueOf(0.3)
                }
            }
        }
        additionalClassDirs(files(subprojects.flatMap { project ->
            listOf("java", "kotlin").map { project.buildDir.path + "/classes/$it/main" }
        }))
        additionalSourceDirs(files(subprojects.flatMap { project ->
            listOf("java", "kotlin").map { project.file("src/main/$it").absolutePath }
        }))
        dependsOn(jacocoAggregateReport)
    }
    check {
        finalizedBy(jacocoTestCoverageVerification)
    }
}

tasks.register<Copy>("copyTestLogs") {
    from(".")
    include("**/build/test-output/**")
    include("**/*.log")
    exclude("build")
    into("build/test_logs")
    includeEmptyDirs = false
}