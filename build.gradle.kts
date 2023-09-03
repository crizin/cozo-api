plugins {
    java
    id("org.springframework.boot") version "3.1.3"
    id("io.spring.dependency-management") version "1.1.3"
    id("org.sonarqube") version "4.2.1.3168"
}

group = "me.cozo"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
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
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    implementation("net.javacrumbs.shedlock:shedlock-spring:5.6.0")
    implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:5.6.0")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.2.1")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("org.apache.commons:commons-lang3:3.13.0")
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("com.google.guava:guava:32.1.2-jre")
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.jsoup:jsoup:1.16.1")
    implementation("org.yaml:snakeyaml:2.1")
    implementation(project(":webs"))
    compileOnly("org.projectlombok:lombok")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    annotationProcessor("org.projectlombok:lombok")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
