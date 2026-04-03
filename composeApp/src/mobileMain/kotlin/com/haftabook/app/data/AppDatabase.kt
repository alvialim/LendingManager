package com.haftabook.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.ConstructedBy
import androidx.room.RoomDatabaseConstructor
import com.haftabook.app.data.local.dao.CustomerDao
import com.haftabook.app.data.local.dao.EmiDao
import com.haftabook.app.data.local.dao.LoanDao
import com.haftabook.app.data.local.dao.SyncOutboxDao
import com.haftabook.app.data.local.dao.SyncStateDao
import com.haftabook.app.data.local.entity.CustomerEntity
import com.haftabook.app.data.local.entity.EmiEntity
import com.haftabook.app.data.local.entity.LoanEntity
import com.haftabook.app.data.local.entity.SyncOutboxEntity
import com.haftabook.app.data.local.entity.SyncStateEntity

@Database(
    entities = [
        CustomerEntity::class,
        LoanEntity::class,
        EmiEntity::class,
        SyncOutboxEntity::class,
        SyncStateEntity::class
    ],
    version = 3,
    exportSchema = false
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun loanDao(): LoanDao
    abstract fun emiDao(): EmiDao
    abstract fun syncOutboxDao(): SyncOutboxDao
    abstract fun syncStateDao(): SyncStateDao
}

expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

const val DATABASE_NAME = "haftabook_db"
