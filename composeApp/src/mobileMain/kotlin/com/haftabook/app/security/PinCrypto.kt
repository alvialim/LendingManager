package com.haftabook.app.security

import java.security.MessageDigest

/** Local-only PIN hashing (not for server auth). */
object PinCrypto {
    private const val SALT = "haftabook.pin.v1"

    fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(SALT.toByteArray(Charsets.UTF_8))
        md.update(pin.toByteArray(Charsets.UTF_8))
        return md.digest().joinToString("") { b -> "%02x".format(b) }
    }
}
