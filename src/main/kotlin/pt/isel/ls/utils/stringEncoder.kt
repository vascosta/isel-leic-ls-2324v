package pt.isel.ls.utils

import java.util.Base64

fun encode(str: String): String {
    return Base64.getEncoder().encodeToString(str.toByteArray())
}

fun decode(str: String): String {
    return String(Base64.getDecoder().decode(str))
}
