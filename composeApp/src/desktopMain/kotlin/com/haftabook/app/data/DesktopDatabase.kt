package com.haftabook.app.data

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.haftabook.app.data.local.MIGRATION_2_3
import java.io.File

fun getDesktopDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dir = File(System.getProperty("user.home"), ".haftabook")
    dir.mkdirs()
    val dbFile = File(dir, DATABASE_NAME)
    return Room.databaseBuilder<AppDatabase>(dbFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .addMigrations(MIGRATION_2_3)
}
