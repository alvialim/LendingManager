package com.haftabook.app.data.remote

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class PinType {
    MONTHLY,
    DAILY,
}

@Serializable
private data class PinManagementRemote(
    @SerialName("monthly_pin") val monthlyPinSnake: String = "",
    @SerialName("daily_pin") val dailyPinSnake: String = "",
    @SerialName("pinMonthly") val monthlyPinCamel: String = "",
    @SerialName("pinDaily") val dailyPinCamel: String = "",
)

object PinManagementConfig {
    private const val COLLECTION = "pin_management"
    private val candidateDocIds = listOf("default", "JQPADNVK4vbpxX4nmPZ5")

    suspend fun fetchPin(type: PinType): String {
        val firestore = Firebase.firestore
        for (docId in candidateDocIds) {
            val parsed = runCatching {
                firestore.collection(COLLECTION).document(docId).get()
                    .data(PinManagementRemote.serializer())
            }.getOrNull()
            if (parsed != null) {
                return when (type) {
                    PinType.MONTHLY -> {
                        parsed.monthlyPinSnake.trim().ifBlank { parsed.monthlyPinCamel.trim() }
                    }
                    PinType.DAILY -> {
                        parsed.dailyPinSnake.trim().ifBlank { parsed.dailyPinCamel.trim() }
                    }
                }
            }
        }
        return ""
    }
}
