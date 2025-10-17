# SqlDelight Spring Boot Example

A minimal Kotlin/Spring Boot 3 project that demonstrates how to integrate SqlDelight with a PostgreSQL database. The app
boots a Spring context, and runs a simple `ApplicationRunner`. The runner writes a few Formula 1 drivers into the 
database using SqlDelight-generated code and reads them back.

## Running the app

1. Ensure your Docker daemon is running.
2. Build via `./gradlew build`
3. Run via `./gradlew bootRun`

You should see the following output:

```text
Inserting: FormulaOneDriver(driver_number=1, full_name=Max Verstappen, constructor_=Red Bull)
Inserting: FormulaOneDriver(driver_number=81, full_name=Oscar Piastri, constructor_=McLaren)
Inserting: FormulaOneDriver(driver_number=16, full_name=Charles Leclerc, constructor_=Ferrari)
The database contains 3 drivers
Finding driver with driver number 81
Found driver: Oscar Piastri
```

## SqlDelight
- Migrations are located under: `src/main/sqldelight/.../tables/*.sqm`.
- Queries are located under: `src/main/sqldelight/.../queries/*.sq`
- Generated code package: `com.github.basdgrt.sqldelight` with database name `PostgresDatabase` (see `build.gradle.kts`).
- Code generation happens during a Gradle build. No extra step is required.

Running a clean build will regenerate interfaces:
```
./gradlew clean build
```
