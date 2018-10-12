package org.tokend.authenticator.base.util

import java.util.concurrent.atomic.AtomicLong

object LongUid {
    private val sequence = AtomicLong(System.currentTimeMillis())

    fun get(): Long = sequence.getAndAdd(1)
}