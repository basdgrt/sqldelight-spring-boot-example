plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.20"
	id("org.springframework.boot") version "3.5.6"
	id("io.spring.dependency-management") version "1.1.7"
	id("app.cash.sqldelight") version "2.1.0"
}

group = "com.github.basdgrt"
version = "0.0.1-SNAPSHOT"
description = "How to use SqlDelight in a Spring Boot project"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("app.cash.sqldelight:jdbc-driver:2.1.0")

	runtimeOnly("org.postgresql:postgresql")
	developmentOnly("org.springframework.boot:spring-boot-docker-compose")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}

	sourceSets {
		main {
			kotlin.srcDir("build/generated/sqldelight/code/PostgresDatabase")
		}
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

sqldelight {
	databases {
		create(name = "PostgresDatabase") {
			// Set the package name for the generated database class
			packageName.set("com.github.basdgrt.sqldelight")

			// Target the postgresql dialect
			dialect("app.cash.sqldelight:postgresql-dialect:2.1.0")

			// Derive the schema for our database from the .sqm files
			deriveSchemaFromMigrations.set(true)
		}
	}
}