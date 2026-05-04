package com.haftabook.app.data.remote

private fun envOrProp(envName: String, propName: String = envName.lowercase().replace('_', '.')): String {
    val fromEnv = System.getenv(envName)?.trim().orEmpty()
    if (fromEnv.isNotEmpty()) return fromEnv
    return System.getProperty(propName)?.trim().orEmpty()
}

