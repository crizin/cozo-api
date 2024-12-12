plugins {
	java
	id("org.springframework.boot") version "3.4.0"
	id("io.spring.dependency-management") version "1.1.6"
	id("org.sonarqube") version "6.0.1.5171"
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

val springdocOpenapiVersion = "2.7.0"
val shedlockVersion = "6.0.2"
val httpClientVersion = "5.4.1"
val httpCoreVersion = "5.3.1"
val commonsCollectionsVersion = "4.4"
val commonsLangVersion = "3.17.0"
val commonsTextVersion = "1.12.0"
val guavaVersion = "33.3.1-jre"
val reflectionsVersion = "0.10.2"
val jsoupVersion = "1.18.3"
val snakeyamlVersion = "2.3"

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
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocOpenapiVersion")
	implementation("net.javacrumbs.shedlock:shedlock-spring:$shedlockVersion")
	implementation("net.javacrumbs.shedlock:shedlock-provider-jdbc-template:$shedlockVersion")
	implementation("org.apache.httpcomponents.client5:httpclient5:$httpClientVersion")
	implementation("org.apache.httpcomponents.core5:httpcore5:$httpCoreVersion")
	implementation("org.apache.httpcomponents.core5:httpcore5-h2:$httpCoreVersion")
	implementation("org.apache.commons:commons-collections4:$commonsCollectionsVersion")
	implementation("org.apache.commons:commons-lang3:$commonsLangVersion")
	implementation("org.apache.commons:commons-text:$commonsTextVersion")
	implementation("com.google.guava:guava:$guavaVersion")
	implementation("org.reflections:reflections:$reflectionsVersion")
	implementation("org.jsoup:jsoup:$jsoupVersion")
	implementation("org.yaml:snakeyaml:$snakeyamlVersion")
	implementation(project(":webs"))
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("com.mysql:mysql-connector-j")
	runtimeOnly("io.micrometer:micrometer-registry-prometheus")
	annotationProcessor("org.projectlombok:lombok")
}
