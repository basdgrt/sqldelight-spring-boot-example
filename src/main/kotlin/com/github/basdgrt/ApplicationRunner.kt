package com.github.basdgrt

import com.github.basdgrt.sqldelight.tables.FormulaOneDriver
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class ApplicationRunner(
    private val repository: FormulaOneDriverRepository
): ApplicationRunner {

    override fun run(args: ApplicationArguments?) {
        repository.deleteAll()

        insertDrivers()

        val count = repository.findAllDrivers().size
        println("The database contains $count drivers")

        println("Finding driver with driver number 81")
        val result = repository.findByDriverNumber(81)
        println("Found driver: ${result?.full_name}")
    }

    private fun insertDrivers() {
        listOf(
            FormulaOneDriver(1, "Max Verstappen", "Red Bull"),
            FormulaOneDriver(81, "Oscar Piastri", "McLaren"),
            FormulaOneDriver(16, "Charles Leclerc", "Ferrari")
        ).forEach { driver ->
            println("Inserting: $driver")
            repository.insert(driver)
        }
    }
}