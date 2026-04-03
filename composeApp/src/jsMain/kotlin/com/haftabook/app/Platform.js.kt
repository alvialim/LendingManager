package com.haftabook.app

private class JsPlatform : Platform {
    override val name: String = "Web (Kotlin/JS)"
}

actual fun getPlatform(): Platform = JsPlatform()
