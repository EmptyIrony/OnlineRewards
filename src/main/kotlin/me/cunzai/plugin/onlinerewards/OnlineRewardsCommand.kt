package me.cunzai.plugin.onlinerewards

import me.cunzai.plugin.onlinerewards.database.MySQLHandler
import me.cunzai.plugin.onlinerewards.database.RedisHandler
import me.cunzai.plugin.onlinerewards.rewards.Rewards
import me.cunzai.plugin.onlinerewards.ui.UIs
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import taboolib.common.platform.command.*
import taboolib.common.platform.function.submitAsync
import taboolib.expansion.createHelper
import taboolib.module.chat.colored

@CommandHeader(name = "onlineReward")
object OnlineRewardsCommand {

    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val open = mainCommand {
        execute<Player> { sender, _, _ ->
            UIs.openRewardsUI(sender)
        }
    }

    @CommandBody(permission = "onlinereward.rewards")
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            UIs.config.reload()
            Rewards.config.reload()
            Rewards.i()

            sender.sendMessage("ok")
        }
    }

}