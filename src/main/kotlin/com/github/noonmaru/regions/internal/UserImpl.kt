package com.github.noonmaru.regions.internal

import com.github.noonmaru.regions.api.Member
import com.github.noonmaru.regions.api.Region
import com.github.noonmaru.regions.api.User
import com.google.common.collect.ImmutableList
import org.bukkit.entity.Player
import java.util.*

class UserImpl(
    override val uniqueId: UUID,
    override var name: String
) : User {

    override var bukkitPlayer: Player? = null
        internal set

    override val regionMembers: List<Member>
        get() = ImmutableList.copyOf(_regionMembers)

    private val _regionMembers = HashSet<Member>()

    override fun getMemberByRegion(region: Region): Member? {
        return _regionMembers.find { it.parent === region }
    }

    internal fun addMember(member: Member) {
        _regionMembers.add(member)
    }
}

internal fun User.toImpl(): UserImpl {
    return this as UserImpl
}