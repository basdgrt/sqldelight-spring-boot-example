# Type-Safe SQL for Spring Boot: A Practical SQLDelight Guide

If you've ever stared at a cryptic ORM stack trace wondering, "What SQL just broke my app?", you're not alone. SQLDelight flips the script. Instead of surrendering to auto-generated chaos, you just write plain SQL.

The magic? SQLDelight generates fully type-safe Kotlin APIs at compile time.

In this guide, I'll walk you through integrating SQLDelight with Spring Boot and PostgreSQL. From connecting the driver to writing migrations and executing queries, you'll see how SQLDelight keeps your schema, queries, and code in check, without the usual friction.

## What is SQLDelight?

SQLDelight is a code generator that transforms your SQL statements into type-safe Kotlin APIs. You write standard SQL. SQLDelight validates it against your schema and generates Kotlin code that matches your database structure perfectly.

Unlike traditional ORMs, SQLDelight doesn't hide SQL behind layers of abstraction. You maintain full control over your queries. The compiler catches errors before runtime. No more mysterious query failures in production.

## Why Choose SQLDelight?

**Compile-time safety**
SQLDelight validates your SQL at compile time. Typos in column names? The build fails. Wrong data types? The build fails. This catches bugs before they reach production.

**No runtime reflection**
ORMs rely on reflection to map objects to database tables. This adds overhead and can fail in unexpected ways. SQLDelight generates direct, optimized code at build time.

**Full SQL control**
Want to use PostgreSQL-specific features? Write a complex join? Optimize a query? Just write the SQL you need. SQLDelight doesn't limit you to a subset of SQL features.

**Migration-first workflow**
SQLDelight derives your schema from migration files. This ensures your migrations are always the source of truth. No drift between your migration scripts and your ORM models.

**Small footprint**
The generated code is lean. No heavy framework to load. This means faster startup times and lower memory usage.

## Step 1: Create Your Spring Boot Project

Start by creating a new Spring Boot project at [start.spring.io](https://start.spring.io). Configure it with these settings:

- Project: Gradle (Kotlin)
- Language: Kotlin
- Spring Boot: 3.5.x or later
- Java: 21

Add these dependencies:
- Spring Web
- Spring Data JDBC
- Docker Compose Support

Generate and download the project. Extract it to your workspace.

**Why these dependencies?**

Spring Data JDBC provides the DataSource that SQLDelight will use. Docker Compose Support lets Spring Boot automatically start your PostgreSQL container during development. This eliminates manual Docker commands.

## Step 2: Configure Docker Compose

Create a `compose.yaml` file in your project root:

```yaml
services:
  postgres-sqldelight-demo:
    image: postgres:18-alpine
    restart: always
    environment:
      POSTGRES_USER: user
      POSTGRES_DB: postgres
      POSTGRES_PASSWORD: secret
    ports:
      - "5432:5432"
```

This configuration spins up a PostgreSQL 18 container. The Alpine image keeps it lightweight. Spring Boot's Docker Compose support will detect this file and start the container when you run your application.

**Why PostgreSQL?**

PostgreSQL offers robust features and excellent SQLDelight support. The postgresql-dialect in SQLDelight understands PostgreSQL-specific syntax and types.

## Step 3: Configure SQLDelight in build.gradle.kts

Open `build.gradle.kts` and add the SQLDelight plugin:

```kotlin
plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.spring") version "2.2.20"
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("app.cash.sqldelight") version "2.1.0"
}
```

Add the SQLDelight dependencies:

```kotlin
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
```

Configure Kotlin to include the generated SQLDelight code:

```kotlin
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
```

Finally, add the SQLDelight configuration block:

```kotlin
sqldelight {
    databases {
        create(name = "PostgresDatabase") {
            // Set the package name for the generated database class
            packageName.set("com.github.basdgrt.sqldelight")

            // Target the postgresql dialect
            dialect("app.cash.sqldelight:postgresql-dialect:2.1.0")

            // Derive the schema from migration files
            deriveSchemaFromMigrations.set(true)
        }
    }
}
```

**Understanding the configuration**

The `sourceSets` configuration tells Kotlin where to find the generated code. This lets your IDE provide autocompletion and error checking for the generated APIs.

The `sqldelight` block defines your database. The `packageName` sets where the generated classes live. The `dialect` tells SQLDelight you're using PostgreSQL. This enables PostgreSQL-specific syntax validation.

The `deriveSchemaFromMigrations` setting is crucial. It tells SQLDelight to build your schema from `.sqm` migration files instead of `.sq` files. This migration-first approach ensures your migrations are always correct.

## Step 4: Create Migration and Query Files

Create the following directory structure:

```
src/main/sqldelight/com/github/basdgrt/sqldelight/
├── tables/
│   ├── V1_0_0__create-table.sqm
│   └── V1_0_1__add-constructor.sqm
└── queries/
    └── Driver.sq
```

**Why this structure?**

The `tables/` directory holds migration files that define your schema. The `queries/` directory contains your SQL queries. This separation keeps migrations and queries organized.

Create your first migration `V1_0_0__create-table.sqm`:

```sql
CREATE TABLE IF NOT EXISTS formulaOneDriver (
  driver_number INTEGER PRIMARY KEY NOT NULL,
  full_name TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS formulaOneDriver_full_name ON formulaOneDriver(full_name);
```

This creates a table for Formula One drivers with an index on the full_name column for faster lookups.

Create a second migration `V1_0_1__add-constructor.sqm`:

```sql
ALTER TABLE formulaOneDriver ADD COLUMN IF NOT EXISTS constructor TEXT;
```

This adds a constructor column to track which team each driver races for.

**Why separate migration files?**

Each migration represents a schema change. Keeping them separate creates a clear history of how your schema evolved. SQLDelight applies them in order based on the version numbers.

Now create your query file `Driver.sq`:

```sql
findAll:
SELECT *
FROM formulaOneDriver;

findByDriverNumber:
SELECT *
FROM formulaOneDriver
WHERE driver_number = ?;

insert:
INSERT INTO formulaOneDriver(driver_number, full_name, constructor)
VALUES ?;

deleteAll:
DELETE FROM formulaOneDriver;
```

**Understanding query files**

Each query has a label (like `findAll:`) followed by SQL. SQLDelight generates a Kotlin function for each labeled query. The function name matches the label.

The `?` placeholders become function parameters. SQLDelight infers the parameter types from your schema.

**Generate the code**

Run the Gradle task to generate the Kotlin APIs:

```bash
./gradlew generateSqlDelightInterface
```

SQLDelight generates several classes:
- `PostgresDatabase`: The main database class
- `DriverQueries`: Contains your query functions
- `FormulaOneDriver`: A data class representing a row

The generated code lives in `build/generated/sqldelight/code/PostgresDatabase/`. Your IDE should now recognize these classes.

## Step 5: Configure the Database Connection

Create a `DatabaseConfiguration.kt` file:

```kotlin
package com.github.basdgrt

import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import com.github.basdgrt.sqldelight.PostgresDatabase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class DatabaseConfiguration {

    @Bean
    fun database(datasource: DataSource): PostgresDatabase = datasource.asJdbcDriver()
        .also { jdbcDriver ->
            PostgresDatabase.Schema.migrate(
                driver = jdbcDriver,
                oldVersion = 0,
                newVersion = PostgresDatabase.Schema.version
            )
        }
        .let { jdbcDriver -> PostgresDatabase(jdbcDriver) }
}
```

**What's happening here?**

First, we convert Spring's `DataSource` to a SQLDelight JDBC driver using `asJdbcDriver()`. This adapter lets SQLDelight work with Spring's connection pooling.

Second, we run migrations. The `migrate()` function applies all migrations from version 0 to the latest version. This happens automatically when your application starts.

Finally, we create and return the `PostgresDatabase` instance. Spring will manage this as a singleton bean.

**Why handle migrations here?**

This ensures your database schema is always up to date when the application starts. No separate migration tool needed. No manual SQL scripts to run.

## Step 6: Create the Repository

Create a `FormulaOneDriverRepository.kt` file:

```kotlin
package com.github.basdgrt

import com.github.basdgrt.sqldelight.PostgresDatabase
import com.github.basdgrt.sqldelight.tables.FormulaOneDriver
import org.springframework.stereotype.Repository

@Repository
class FormulaOneDriverRepository(
    database: PostgresDatabase
) {

    private val query = database.driverQueries

    fun findAllDrivers(): List<FormulaOneDriver> = query.findAll().executeAsList()

    fun findByDriverNumber(driverNumber: Int): FormulaOneDriver? =
        query.findByDriverNumber(driverNumber).executeAsOneOrNull()

    fun insert(driver: FormulaOneDriver) = query.insert(driver).value

    fun deleteAll(): Long = query.deleteAll().value
}
```

**Understanding the repository**

Spring injects the `PostgresDatabase` bean. We extract `driverQueries` which contains all the query functions SQLDelight generated from `Driver.sq`.

Each repository method calls a generated query function:
- `findAll()` returns all drivers as a list
- `findByDriverNumber()` returns one driver or null
- `insert()` adds a new driver
- `deleteAll()` removes all drivers

Notice the return types. SQLDelight knows that `findAll()` returns a list of `FormulaOneDriver` objects. It knows that `findByDriverNumber()` might return null. All this is inferred from your SQL and schema.

**Type safety in action**

Try calling `findByDriverNumber()` with a String instead of an Int. The code won't compile. Try accessing a column that doesn't exist on `FormulaOneDriver`. The code won't compile. This is the power of compile-time safety.

## What You've Built

You now have a fully functional Spring Boot application with SQLDelight. Your queries are type-safe. Your migrations are automated. Your SQL is first-class.

The key benefits you get:
- Compile-time verification of all SQL queries
- Automatic migration management
- Type-safe data access with zero reflection
- Full control over your SQL
- Clear separation between schema and queries

## Next Steps

Ready to see it in action? Clone the complete example repository at [github.com/basdgrt/sqldelight-spring-boot-example](https://github.com/basdgrt/sqldelight-spring-boot-example).

The repository includes a full working application with REST endpoints. You can see how to use the repository in a Spring controller. You can run the application and test the queries yourself.

Try adding your own tables and queries. Experiment with PostgreSQL-specific features like JSON columns or full-text search. SQLDelight supports them all.

## Wrapping Up

SQLDelight brings the best of both worlds. You get the power and flexibility of raw SQL. You also get the safety and convenience of generated, type-safe code.

No more runtime query errors. No more reflection overhead. No more wondering what SQL your ORM is running. Just plain SQL, validated at compile time, with clean Kotlin APIs.

Give it a try in your next Spring Boot project. Your future self will thank you.
