package com.github.noonmaru.regions.api

import com.github.noonmaru.regions.util.Indexable
import com.google.common.collect.ImmutableList


enum class Protection : Indexable {
    DAMAGE,
    POTION,
    BLOCK_IGNITE,
    BLOCK_MELT,
    BLOCK_GROW,
    BLOCK_FORM,
    BLOCK_SPREAD,
    BLOCK_REDSTONE,
    BLOCK_OVERFLOW,
    BLOCK_FLOOD,
    ENTITY_CHANGE_BLOCK,
    EXPLOSION;

    override val offset: Int
        get() = ordinal

    override val raw: Int = super.raw

    companion object {
        val BY_OFFSET = ImmutableList.copyOf(values())

        fun getByOffset(offset: Int): Protection {
            return BY_OFFSET[offset]
        }
    }
}

interface Protectible {
    val protections: Set<Protection>

    fun hasProtection(protection: Protection): Boolean
    fun addProtection(vararg protections: Protection) {
        addProtection(protections.toList())
    }

    fun addProtection(protections: Collection<Protection>)

    fun removeProtection(vararg protections: Protection) {
        removeProtection(protections.toList())
    }

    fun removeProtection(protections: Collection<Protection>)
}