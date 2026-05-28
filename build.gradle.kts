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

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation ("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation ("org.springframework.boot:spring-boot-starter-security")
	implementation ("org.springframework.boot:spring-boot-starter-validation")
	implementation ("org.springframework.boot:spring-boot-starter-web")
	implementation ("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	compileOnly ("org.projectlombok:lombok")
	runtimeOnly ("org.postgresql:postgresql")
	annotationProcessor ("org.projectlombok:lombok")
	testImplementation ("org.springframework.boot:spring-boot-starter-test")
	testImplementation ("org.springframework.security:spring-security-test")
	testCompileOnly ("org.projectlombok:lombok")
	testRuntimeOnly ("org.junit.platform:junit-platform-launcher")
	testRuntimeOnly ("com.h2database:h2")
	testAnnotationProcessor ("org.projectlombok:lombok")

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
