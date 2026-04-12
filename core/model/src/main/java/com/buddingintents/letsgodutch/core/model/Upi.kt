package com.buddingintents.letsgodutch.core.model

import java.util.Locale

private val UPI_ID_REGEX = Regex(
    pattern = "^[a-z0-9._-]{2,256}@[a-z0-9.-]{2,64}$",
    option = RegexOption.IGNORE_CASE,
)

fun String.normalizeUpiId(): String = trim().lowercase(Locale.US)

fun String.isValidUpiId(): Boolean {
    val normalized = normalizeUpiId()
    return normalized.isBlank() || UPI_ID_REGEX.matches(normalized)
}
