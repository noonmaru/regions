package com.github.noonmaru.regions.api

import org.bukkit.entity.Player


interface Region : Protectible, Checkable {
    val name: String

    val world: RegionWorld

    val box: RegionBox

    val parents: List<Region>

    val children: List<Region>

    val roles: List<Role>

    val publicRole: Role

    val members: List<Member>

    fun relocate(newWorld: RegionWorld, newBox: RegionBox)

    fun addParent(region: Region): Boolean

    fun removeParent(region: Region): Boolean

    fun createRole(name: String): Role

    fun removeRole(name: String): Role?

    fun getRole(name: String): Role?

    fun addPermissionToRole(role: Role, vararg permissions: Permission): Boolean {
        return addPermissionToRole(role, permissions.asList())
    }

    fun addPermissionToRole(role: Role, permissions: Collection<Permission>): Boolean

    fun removePermissionFromRole(role: Role, vararg permissions: Permission): Boolean {
        return removePermissionFromRole(role, permissions.asList())
    }

    fun removePermissionFromRole(role: Role, permissions: Collection<Permission>): Boolean

    fun addMember(user: User): Member

    fun removeMember(user: User): Member?

    fun getMember(user: User): Member?

    fun addRoleToMember(member: Member, role: Role): Boolean

    fun removeRoleFromMember(member: Member, role: Role): Boolean

    fun getMembersWithRoles(vararg roles: Role): List<Member> {
        return getMembersWithRoles(listOf(*roles))
    }

    fun getMembersWithRoles(roles: Collection<Role>): List<Member>

    fun getPermissions(player: Player): Set<Permission>

    fun save(): Boolean

    fun delete()
}