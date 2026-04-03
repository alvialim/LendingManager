package com.haftabook.app.data.remote

private fun envOrProp(envName: String, propName: String = envName.lowercase().replace('_', '.')): String {
    val fromEnv = System.getenv(envName)?.trim().orEmpty()
    if (fromEnv.isNotEmpty()) return fromEnv
    return System.getProperty(propName)?.trim().orEmpty()
}

actual fun loadSupabaseConfig(): SupabaseConfig? {
    val url = envOrProp("SUPABASE_URL", "supabase.url")
    val key = envOrProp("SUPABASE_ANON_KEY", "supabase.anon.key")
    if (url.isBlank() || key.isBlank()) return null
    return SupabaseConfig(url, key)
}
