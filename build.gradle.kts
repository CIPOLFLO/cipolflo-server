import org.gradle.kotlin.dsl.creating

plugins {
	java
	jacoco
	id ("org.springframework.boot") version "3.5.14"
	id ("io.spring.dependency-management") version "1.1.7"
	id ("org.sonarqube") version "7.2.3.7755"
}

group = "com.cipolflo"
version = "0.0.1-SNAPSHOT"

// El driver postgresql 42.7.5+ rompe la detección de la tabla databasechangelog
// de Liquibase (cambio en el manejo del catálogo). Fijamos 42.7.4, última versión
// sin la regresión. Ver: https://github.com/liquibase/liquibase/issues/6666
extra["postgresql.version"] = "42.7.4"

// Spring AI 2.0.0 exige Spring Boot 4.x. Mientras el proyecto esté en Boot 3.5.x
// se fija la última versión estable de la línea 1.x.
val springAiVersion = "1.1.8"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.apache.poi:poi-ooxml:5.4.0")
	implementation("org.apache.pdfbox:pdfbox:3.0.4")
	implementation ("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation ("org.springframework.boot:spring-boot-starter-security")
	implementation ("org.springframework.boot:spring-boot-starter-validation")
	implementation ("org.springframework.boot:spring-boot-starter-web")
	implementation ("org.springframework.boot:spring-boot-starter-mail")
	implementation ("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation ("org.springframework.boot:spring-boot-starter-actuator")
	implementation ("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
	compileOnly ("org.projectlombok:lombok")
	runtimeOnly ("org.postgresql:postgresql")
	implementation ("org.liquibase:liquibase-core")
	annotationProcessor ("org.projectlombok:lombok")
	testImplementation ("org.springframework.boot:spring-boot-starter-test")
	testImplementation ("org.springframework.security:spring-security-test")
	testCompileOnly ("org.projectlombok:lombok")
	testRuntimeOnly ("org.junit.platform:junit-platform-launcher")
	testRuntimeOnly ("com.h2database:h2")
	testAnnotationProcessor ("org.projectlombok:lombok")
	implementation("com.azure:azure-ai-documentintelligence:1.0.0-beta.4")
	implementation("com.azure:azure-core:1.53.0")
	implementation("org.springframework.ai:spring-ai-starter-model-openai")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.ai:spring-ai-bom:$springAiVersion")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required = true
	}
}

sonar {
	properties {
		property("sonar.projectKey", "CIPOLFLO_cipolflo-server")
		property("sonar.organization", "cipolflo")
		property(
			"sonar.coverage.jacoco.xmlReportPaths",
			"${layout.buildDirectory.get()}/reports/jacoco/test/jacocoTestReport.xml"
		)
	}
}
