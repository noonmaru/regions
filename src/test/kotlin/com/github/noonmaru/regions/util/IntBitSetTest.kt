package com.github.noonmaru.regions.util

import com.github.noonmaru.regions.api.Protection
import org.junit.Assert.*
import org.junit.Test

class IntBitSetTest {
    @Test
    fun testAdd() {
        val set = IntBitSet { Protection.getByOffset(it) }

        assertTrue(set.add(Protection.DAMAGE))
        assertFalse(set.add(Protection.DAMAGE))
    }

    @Test
    fun testAddAll() {
        val set = IntBitSet { Protection.getByOffset(it) }

        assertTrue(set.addAll(listOf(Protection.DAMAGE, Protection.GROWTH)))
        assertFalse(set.addAll(listOf(Protection.DAMAGE, Protection.GROWTH)))
    }

    @Test
    fun testIterator() {
        val set = IntBitSet { Protection.getByOffset(it) }

        set.addAll(
            listOf(
                Protection.DAMAGE,
                Protection.POTION,
                Protection.IGNITION
            )
        )

        val iter = set.iterator()
        assertTrue(iter.hasNext())
        assertEquals(iter.next(), Protection.DAMAGE)
        assertEquals(iter.next(), Protection.POTION)
        assertEquals(iter.next(), Protection.IGNITION)
        assertFalse(iter.hasNext())
    }
}