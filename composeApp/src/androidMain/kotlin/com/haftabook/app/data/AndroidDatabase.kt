package com.haftabook.app.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.haftabook.app.data.local.MIGRATION_2_3

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    return Room.databaseBuilder<AppDatabase>(
        context = context.applicationContext,
        name = DATABASE_NAME
    )
    .addMigrations(MIGRATION_2_3)
}
