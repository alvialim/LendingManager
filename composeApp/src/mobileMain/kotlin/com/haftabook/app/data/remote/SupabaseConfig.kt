package com.haftabook.app.data.remote

data class SupabaseConfig(
    val url: String,
    val anonKey: String
) {
    val restBase: String
        get() = url.trimEnd('/') + "/rest/v1"
}

