plugins {
    java
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.sonarqube") version "5.1.0.4882"
}

group = "me.cozo"
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

sonar {
    properties {
        property("sonar.projectKey", "crizin_cozo-api")
        property("sonar.organization", "crizin")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-integration")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    implementation("net.javacrumbs.shedlock:shedlock-spring:5.16.0")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:5.16.0")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.4")
    implementation("org.apache.httpcomponents.core5:httpcore5:5.3")
    implementation("org.apache.httpcomponents.core5:httpcore5-h2:5.3")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.apache.commons:commons-lang3:3.17.0")
    implementation("org.apache.commons:commons-text:1.12.0")
    implementation("com.google.guava:guava:33.3.1-jre")
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.jsoup:jsoup:1.18.1")
    implementation("org.yaml:snakeyaml:2.3")
    implementation(project(":webs"))
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    annotationProcessor("org.projectlombok:lombok")
}
