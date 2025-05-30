plugins {
    java
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.12.0"
}

group = "ru.mdemidkin"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.postgresql:r2dbc-postgresql:1.0.7.RELEASE")
    implementation("io.github.daggerok:liquibase-r2dbc-spring-boot-starter:3.1.3")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.5")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    runtimeOnly("org.postgresql:postgresql:42.7.2")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:r2dbc")
    testImplementation("com.github.codemonstur:embedded-redis:1.4.3")
    testImplementation("com.redis:testcontainers-redis:2.2.2")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("$projectDir/src/main/resources/api-spec.yaml")
    outputDir.set("$projectDir/build/generated")
    ignoreFileOverride.set(".openapi-generator-java-sources.ignore")
    modelPackage.set("ru.mdemidkin.intershop.server.domain")
    invokerPackage.set("ru.mdemidkin.intershop.server")
    apiPackage.set("ru.mdemidkin.intershop.server.api")
    configOptions.set(mapOf(
        "hideGenerationTimestamp" to "true",
        "requestMappingMode" to "controller",
        "interfaceOnly" to "true", // false для генерации кода
        "library" to "spring-boot",
        "reactive" to "true",
        "useSpringBoot3" to "true",
        "useJakartaEe" to "true",
        "useTags" to "true",
        "dateLibrary" to "java8",
        "openApiNullable" to "false",
        "serializableModel" to "true",
        "returnSuccessCode" to "true"
    ))
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("buildClient") {
    generatorName.set("java")
    inputSpec.set("$projectDir/src/main/resources/api-spec.yaml")
    outputDir.set("$projectDir/build/generated")
    ignoreFileOverride.set(".openapi-generator-java-sources.ignore")
    modelPackage.set("ru.mdemidkin.intershop.client.domain")
    invokerPackage.set("ru.mdemidkin.intershop.client")
    apiPackage.set("ru.mdemidkin.intershop.client.api")
    configOptions.set(mapOf(
        "hideGenerationTimestamp" to "true",
        "library" to "webclient",
        "useJakartaEe" to "true",
        "useTags" to "true",
        "openApiNullable" to "false",
        "serializableModel" to "true"
    ))
}

sourceSets["main"].java.srcDir("$projectDir/build/generated/src/main/java")
