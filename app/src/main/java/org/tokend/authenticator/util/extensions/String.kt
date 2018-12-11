package org.tokend.authenticator.util.extensions

fun String.addSlashIfNeed(): String {
    return when(endsWith("/")) {
        true -> this
        else -> this + "/"
    }
}