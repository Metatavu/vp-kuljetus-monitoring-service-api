import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.allopen") version "1.6.10"
    id("io.quarkus")
    id("org.openapi.generator") version "5.4.0"
}

repositories {
    mavenLocal()
    mavenCentral()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project
val jaxrsFunctionalTestBuilderVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))

    implementation("io.quarkus:quarkus-hibernate-orm")
    implementation("io.quarkus:quarkus-container-image-docker")
    implementation("io.quarkus:quarkus-hibernate-validator")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-oidc")
    implementation("io.quarkus:quarkus-resteasy-jackson")
    implementation("io.quarkus:quarkus-liquibase")
    implementation("io.quarkus:quarkus-jdbc-mysql")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    implementation("org.apache.commons:commons-lang3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("com.squareup.okhttp3:okhttp")
    testImplementation("fi.metatavu.jaxrs.testbuilder:jaxrs-functional-test-builder:$jaxrsFunctionalTestBuilderVersion")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:mysql")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

group = "fi.metatavu.example"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

sourceSets["main"].java {
    srcDir("build/generated/api-spec/src/main/kotlin")
}
sourceSets["test"].java {
    srcDir("build/generated/api-client/src/main/kotlin")
}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("javax.enterprise.context.RequestScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
    annotation("javax.persistence.Entity")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.toString()
    kotlinOptions.javaParameters = true
}

val generateApiSpec = tasks.register("generateApiSpec",GenerateTask::class){
    setProperty("generatorName", "kotlin-server")
    setProperty("inputSpec",  "$rootDir/spec/swagger.yaml")
    setProperty("outputDir", "$buildDir/generated/api-spec")
    setProperty("apiPackage", "${project.group}.api.spec")
    setProperty("invokerPackage", "${project.group}.api.invoker")
    setProperty("modelPackage", "${project.group}.api.model")
    this.configOptions.put("library", "jaxrs-spec")
    this.configOptions.put("dateLibrary", "java8")
    this.configOptions.put("interfaceOnly", "true")
    this.configOptions.put("useCoroutines", "true")
    this.configOptions.put("returnResponse", "true")
    this.configOptions.put("useSwaggerAnnotations", "false")
    this.configOptions.put("additionalModelTypeAnnotations", "@io.quarkus.runtime.annotations.RegisterForReflection")
}

val generateApiClient = tasks.register("generateApiClient",GenerateTask::class){
    setProperty("generatorName", "kotlin")
    setProperty("library", "jvm-okhttp3")
    setProperty("inputSpec",  "$rootDir/spec/swagger.yaml")
    setProperty("outputDir", "$buildDir/generated/api-client")
    setProperty("packageName", "${project.group}.test.client")
    this.configOptions.put("dateLibrary", "string")
    this.configOptions.put("collectionType", "array")
    this.configOptions.put("serializationLibrary", "jackson")
}

tasks.named("compileKotlin") {
    dependsOn(generateApiSpec)
}

tasks.named("compileTestKotlin") {
    dependsOn(generateApiClient)
}
