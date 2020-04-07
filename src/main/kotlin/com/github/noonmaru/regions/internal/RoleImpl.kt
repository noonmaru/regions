package com.github.noonmaru.regions.internal

import com.github.noonmaru.regions.api.Permission
import com.github.noonmaru.regions.api.Role
import com.github.noonmaru.regions.api.warning
import com.github.noonmaru.regions.toEnumList
import com.github.noonmaru.regions.toStringList
import com.github.noonmaru.regions.util.UpstreamReference
import org.bukkit.configuration.ConfigurationSection
import java.util.*

class RoleImpl(
    parent: AreaImpl,
    override val name: String,
    override val isPublic: Boolean = false
) : Role {
    private val parentRef = UpstreamReference(parent)
    override val parent: AreaImpl
        get() = parentRef.get()

    override val permissions: Set<Permission>
        get() = Collections.unmodifiableSet(_permissions.clone())

    override var valid: Boolean = true
        private set

    internal val _permissions = PermissionSet()

    override fun hasPermission(permission: Permission): Boolean {
        return _permissions.contains(permission)
    }

    override fun hasPermissions(permissions: Collection<Permission>): Boolean {
        return _permissions.containsAll(permissions)
    }

    internal fun addPermissions(permissions: Collection<Permission>): Boolean {
        return _permissions.addAll(permissions)
    }

    internal fun removePermissions(permissions: Collection<Permission>): Boolean {
        return _permissions.removeAll(permissions)
    }

    companion object {
        private const val CFG_PERMISSIONS = "permissions"
    }

    internal fun save(config: ConfigurationSection) {
        config[CFG_PERMISSIONS] = _permissions.toStringList()
    }

    internal fun load(config: ConfigurationSection) {
        _permissions.addAll(config.getStringList(CFG_PERMISSIONS).toEnumList({ Permission.getByKey(it) }) { name ->
            warning("Unknown role in [$name] at ${parent.type} [${parent.name}]")
        })
    }

    override fun delete() {
        checkState()
        require(!isPublic) { "Cannot delete public role" }

        parent.removeRole(name)
    }

    internal fun destroy() {
        valid = false
    }

    override fun toString(): String {
        return "$name ${permissions.joinToString(prefix = "[", postfix = "]") { it.key }}"
    }
}

internal fun Role.toImpl(): RoleImpl {
    return this as RoleImpl
}