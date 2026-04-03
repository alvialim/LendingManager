package com.haftabook.app.data.sync

/**
 * Firestore customer document id: name with spaces removed + mobile digits + [createdDate] (epoch ms).
 * Example: `RajuKumar9876543210173456789000`
 */
fun firestoreCustomerDocId(name: String, mobile: String, createdDate: Long): String {
    val namePart = name.replace(Regex("\\s+"), "").filter { it != '/' }
    val mobilePart = mobile.filter { it.isDigit() }
    return "$namePart$mobilePart$createdDate"
}
