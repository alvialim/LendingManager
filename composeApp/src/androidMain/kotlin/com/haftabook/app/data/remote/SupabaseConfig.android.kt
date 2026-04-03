package com.haftabook.app.data.remote

import com.haftabook.app.BuildConfig

actual fun loadSupabaseConfig(): SupabaseConfig? {
    val url = BuildConfig.SUPABASE_URL
    val key = BuildConfig.SUPABASE_ANON_KEY
    if (url.isBlank() || key.isBlank()) return null
    return SupabaseConfig(url, key)
}
