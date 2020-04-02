package com.github.noonmaru.regions.commands

import com.github.noonmaru.regions.api.Permission
import com.github.noonmaru.regions.api.Region
import com.github.noonmaru.tap.command.ArgumentList
import com.github.noonmaru.tap.command.tabComplete
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import java.util.*

class CommandRegionRole : CommandComponentRegion {
    override val argsCount: Int
        get() = super.argsCount + 2

    override fun onCommand(
        sender: CommandSender,
        label: String,
        componentLabel: String,
        region: Region,
        args: ArgumentList
    ): Boolean {
        val sub = args.next()
        val roleName = args.next()

        if (sub.equals("add", true)) {
            region.runCatching {
                createRole(roleName)
                region.save()
                sender.sendMessage("${region.name} 구역에 $roleName 역할을 추가했습니다.")
            }.onFailure {
                sender.sendMessage("$roleName(은)는 ${region.name} 구역에 이미 등록된 역할 이름입니다.")
            }
        } else if (sub.equals("remove", true)) {
            val role = region.removeRole(roleName)

            if (role == null) {
                sender.sendMessage("${region.name} 구역에서 $roleName 역할을 찾지 못했습니다.")
                return true
            }

            region.save()
            sender.sendMessage("${region.name} 구역에서 ${role.name} 역할을 제거했습니다.")

        } else if (sub.equals("permission", true)) {

            val role = region.getRole(roleName)

            if (role == null) {
                sender.sendMessage("${region.name} 구역에서 $roleName 역할을 찾지 못했습니다.")
                return true
            }

            if (args.hasNext()) {
                val op = args.next()

                if (args.hasNext()) {
                    if (op.equals("add", true) || op.equals("remove", true)) {
                        val permissions = TreeSet<Permission>()

                        while (args.hasNext()) {
                            val key = args.next()
                            val permission = Permission.getByKey(key)

                            if (permission == null) {
                                sender.sendMessage("알 수 없는 권한 이름입니다 ${ChatColor.GRAY}${key}")
                                return true
                            }

                            permissions += permission
                        }

                        if (op.equals("add", true)) {
                            region.addPermissionToRole(role, permissions)
                            sender.sendMessage("[${role.name}] 역할에 권한을 추가했습니다.")
                        } else if (op.equals("remove", true)) {
                            region.removePermissionFromRole(role, permissions)
                            sender.sendMessage("[${role.name}] 역할에서 권한을 제거했습니다.")
                        }

                        region.save()

                        permissions.forEach {
                            sender.sendMessage("  - $it")
                        }
                    } else {
                        sender.sendMessage("알 수 없는 명령입니다.")
                    }
                } else {
                    sender.sendMessage("알 수 없는 명령입니다.")
                }
            } else {
                sender.sendMessage("[${region.name}] 구역 [${role.name}] 역할의 권한 목록입니다.")
                role.permissions.forEach {
                    sender.sendMessage("  - $it")
                }
            }
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        label: String,
        componentLabel: String,
        region: Region?,
        args: ArgumentList
    ): List<String> {
        val arg = args.last()
        return when (args.remain()) {
            1 -> listOf("add", "remove", "permission").tabComplete(arg)
            2 -> {
                region?.roles?.tabComplete(arg) { it.name } ?: emptyList()
            }
            3 -> {
                val sub = args.next()

                if (sub.equals("permission", true)) {
                    listOf("add", "remove").tabComplete(arg)
                } else {
                    emptyList()
                }
            }
            else -> {
                val sub = args.next()

                if (sub.equals("permission", true)) {
                    Permission.values().asList().tabComplete(arg) { it.key }
                } else {
                    emptyList()
                }
            }
        }
    }
}