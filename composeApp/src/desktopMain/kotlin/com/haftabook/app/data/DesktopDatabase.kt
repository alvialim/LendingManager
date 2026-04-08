package com.haftabook.app.data

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.haftabook.app.data.local.MIGRATION_2_3
import com.haftabook.app.data.local.MIGRATION_3_4
import java.io.File

fun getDesktopDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dir = File(System.getProperty("user.home"), ".star-group")
    dir.mkdirs()
    val dbFile = File(dir, DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(dbFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
}
