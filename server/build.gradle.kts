
plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.openapi.generator")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

springBoot {
    mainClass.set("ru.mdemidkin.server.ServerApplication")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.5")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
}

tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>("buildServer") {
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
        "interfaceOnly" to "true",
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

sourceSets["main"].java.srcDir("$projectDir/build/generated/src/main/java")

tasks.compileJava {
    dependsOn("buildServer")
}

tasks.withType<Test> {
    useJUnitPlatform()
}