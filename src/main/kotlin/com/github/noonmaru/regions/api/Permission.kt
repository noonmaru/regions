package com.github.noonmaru.regions.api

import com.github.noonmaru.regions.util.Indexable
import com.google.common.collect.ImmutableList

enum class Permission : Indexable {
    OWNERSHIP,
    ADMINISTRATION,
    ENTRANCE,
    EXIT,
    INTERACTION,
    ENTITY_ATTACK,
    PLAYER_ATTACK,
    PROJECTILE_LAUNCH,
    BLOCK_BREAK,
    BLOCK_PLACE,
    BLOCK_IGNITING,
    BUCKET_FILL,
    BUCKET_EMPTY,
    HANGING_BREAK,
    HANGING_PLACE,
    DROP_ITEM,
    PICKUP_ITEM;

    override val offset: Int
        get() = ordinal

    override val raw: Int = super.raw

    companion object {
        val byOffset = ImmutableList.copyOf(values())

        fun getByOffset(offset: Int): Permission {
            return byOffset[offset]
        }
    }
}

val Array<Permission>.rawPermissions: Int
    get() {
        var raw = 0

        forEach {
            raw = (raw or it.raw)
        }

        return raw
    }

val Iterable<Permission>.rawPermissions: Int
    get() {
        var raw = 0

        forEach {
            raw = (raw or it.raw)
        }

        return raw
    }