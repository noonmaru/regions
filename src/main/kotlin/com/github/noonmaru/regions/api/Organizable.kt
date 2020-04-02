package com.github.noonmaru.regions.api

import java.util.*

interface Organizable {
    val publicRole: Role

    val roles: List<Role>

    val members: List<Member>

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

    fun addRoleToMember(member: Member, role: Role)

    fun removeRoleFromMember(member: Member, role: Role)

}