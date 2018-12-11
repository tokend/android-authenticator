package org.tokend.authenticator.test

import org.junit.Assert
import org.junit.Test
import org.tokend.authenticator.util.LongUid

class LongUid {
    @Test
    fun uniqueness() {
        val size = 1000
        val ids = (1..size).map { LongUid.get() }.toHashSet()

        Assert.assertEquals(size, ids.size)
    }

    @Test
    fun chronology() {
        val size = 1000
        val ids = (1..size).map { LongUid.get() }

        Assert.assertArrayEquals(ids.sorted().toTypedArray(), ids.toTypedArray())
    }
}