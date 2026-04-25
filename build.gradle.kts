plugins {
	java
	id ("org.springframework.boot") version "3.5.14-SNAPSHOT"
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
	maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
	implementation ("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation ("org.springframework.boot:spring-boot-starter-security")
	implementation ("org.springframework.boot:spring-boot-starter-validation")
	implementation ("org.springframework.boot:spring-boot-starter-web")
	compileOnly ("org.projectlombok:lombok")
	runtimeOnly ("org.postgresql:postgresql")
	annotationProcessor ("org.projectlombok:lombok")
	testImplementation ("org.springframework.boot:spring-boot-starter-test")
	testImplementation ("org.springframework.security:spring-security-test")
	testCompileOnly ("org.projectlombok:lombok")
	testRuntimeOnly ("org.junit.platform:junit-platform-launcher")
	testAnnotationProcessor ("org.projectlombok:lombok")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

sonar {
	properties {
		property("sonar.projectKey", "CIPOLFLO_cipolflo-server")
		property("sonar.organization", "cipolflo")
	}
}
