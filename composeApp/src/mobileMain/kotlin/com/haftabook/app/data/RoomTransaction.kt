package com.haftabook.app.data

import androidx.room.immediateTransaction
import androidx.room.useWriterConnection

suspend fun <R> AppDatabase.withTransactionCompat(block: suspend AppDatabase.() -> R): R =
    useWriterConnection { transactor ->
        transactor.immediateTransaction {
            this@withTransactionCompat.block()
        }
    }
