package com.haftabook.app.data.local

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE customers ADD COLUMN remote_id TEXT")
        connection.execSQL("ALTER TABLE customers ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE loans ADD COLUMN remote_id TEXT")
        connection.execSQL("ALTER TABLE loans ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE emis ADD COLUMN remote_id TEXT")
        connection.execSQL("ALTER TABLE emis ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS sync_outbox (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                entity_type TEXT NOT NULL,
                operation TEXT NOT NULL,
                payload_json TEXT NOT NULL,
                created_at_epoch_ms INTEGER NOT NULL,
                attempt_count INTEGER NOT NULL DEFAULT 0,
                last_error TEXT
            )
            """.trimIndent()
        )
        connection.execSQL(
            """
            CREATE TABLE IF NOT EXISTS sync_state (
                `key` TEXT NOT NULL PRIMARY KEY,
                value TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE customers ADD COLUMN photoPath TEXT")
    }
}
