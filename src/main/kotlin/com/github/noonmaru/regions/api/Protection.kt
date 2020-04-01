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
        val byOffset = ImmutableList.copyOf(values())

        fun getByOffset(offset: Int): Protection {
            return byOffset[offset]
        }
    }
}

interface Protectible {
    val protections: Set<Protection>

    fun hasProtection(protection: Protection): Boolean

    fun addProtection(vararg protections: Protection) {
        addProtection(protections.asList())
    }

    fun addProtection(protections: Collection<Protection>)

    fun removeProtection(vararg protections: Protection) {
        removeProtection(protections.asList())
    }

    fun removeProtection(protections: Collection<Protection>)
}