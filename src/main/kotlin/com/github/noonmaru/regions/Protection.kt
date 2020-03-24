package com.github.noonmaru.regions

interface Protectible {
    fun getProtection(protection: Protection): Boolean
    fun addProtection(protection: Protection): Boolean
    fun removeProtection(protection: Protection): Boolean
}

enum class Protection {
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
    EXPLOSION
}