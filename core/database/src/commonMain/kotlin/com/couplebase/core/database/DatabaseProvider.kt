package com.couplebase.core.database

object DatabaseProvider {
    private var database: CouplebaseDatabase? = null

    suspend fun getDatabase(driverFactory: DriverFactory): CouplebaseDatabase {
        return database ?: run {
            val driver = driverFactory.createDriver()
            CouplebaseDatabase(driver).also { database = it }
        }
    }
}
