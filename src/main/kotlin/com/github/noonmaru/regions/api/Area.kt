package com.github.noonmaru.regions.api

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.util.*

interface Area {
    val name: String

    val type: Type

    val protections: Set<Protection>

    val publicRole: Role

    val roles: List<Role>

    val members: List<Member>

    fun hasProtection(protection: Protection): Boolean

    fun hasProtections(vararg protections: Protection): Boolean {
        return hasProtections(protections.asList())
    }

    fun hasProtections(protections: Collection<Protection>): Boolean

    fun addProtections(vararg protections: Protection) {
        addProtections(protections.asList())
    }

    fun addProtections(protections: Collection<Protection>): Boolean

    fun removeProtections(vararg protections: Protection) {
        removeProtections(protections.asList())
    }

    fun removeProtections(protections: Collection<Protection>): Boolean

    fun getRole(name: String): Role?

    fun getOrRegisterNewRole(name: String): Role

    fun registerNewRole(name: String): Role

    fun removeRole(name: String): Role?

    fun addPermissionsToRole(role: Role, vararg permissions: Permission): Boolean {
        return addPermissionsToRole(role, permissions.asList())
    }

    fun addPermissionsToRole(role: Role, permissions: Collection<Permission>): Boolean

    fun removePermissionsFromRole(role: Role, vararg permissions: Permission): Boolean {
        return removePermissionsFromRole(role, permissions.asList())
    }

    fun removePermissionsFromRole(role: Role, permissions: Collection<Permission>): Boolean

    fun getMember(user: User): Member?

    fun getMember(name: String): Member?

    fun getMember(uniqueId: UUID): Member?

    fun getMembersWithRoles(vararg roles: Role): List<Member> {
        return getMembersWithRoles(roles.asList())
    }

    fun getMembersWithRoles(roles: Collection<Role>): List<Member>

    fun addMember(user: User): Member

    fun addOrGetMember(user: User): Member

    fun removeMember(user: User): Member?

    fun addRoleToMember(member: Member, role: Role): Boolean

    fun removeRoleFromMember(member: Member, role: Role): Boolean

    fun getPlayerPermissions(player: Player): Set<Permission>

    fun testPermission(player: Player, permission: Permission): Boolean

    fun testPermissions(player: Player, vararg permissions: Permission): Boolean {
        return testPermissions(player, permissions.asList())
    }

    fun testPermissions(player: Player, permissions: Collection<Permission>): Boolean

    fun save(): Boolean

    fun save(config: ConfigurationSection)

    enum class Type(private val toString: String) {
        WORLD("world"),
        REGION("region");

        override fun toString(): String {
            return toString
        }
    }
}