package org.tokend.authenticator.base.extensions

fun String.addSlashIfNeed(): String {
    return when(endsWith("/")) {
        true -> this
        else -> this + "/"
    }
}