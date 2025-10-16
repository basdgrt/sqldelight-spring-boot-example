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