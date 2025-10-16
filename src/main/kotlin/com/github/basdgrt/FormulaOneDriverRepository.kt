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