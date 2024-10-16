package me.cunzai.plugin.onlinerewards

import me.cunzai.plugin.onlinerewards.database.MySQLHandler
import me.cunzai.plugin.onlinerewards.database.RedisHandler
import me.cunzai.plugin.onlinerewards.misc.LeaderboardHandler
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
    val open = subCommand {
        execute<Player> { sender, _, _ ->
            UIs.openMainUI(sender)
        }
    }

    @CommandBody(permission = "onlinereward.admin")
    val sendLeaderboardRewards = subCommand {
        execute<CommandSender> { sender, _, _ ->
            for ((index, leader) in LeaderboardHandler.leaders.withIndex()) {
                Rewards.leaderRewards.getOrNull(index)?.rewards?.forEach { reward ->
                    Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        reward.replace("%player%", leader.name)
                    )
                }
            }

            submitAsync {
                MySQLHandler.clearMonthly()
                RedisHandler.redisConnection.connection().use { connection ->
                    connection.publish("online_rewards_broadcast", RedisHandler.ClearCacheAndReload(System.currentTimeMillis()))
                }
                sender.sendMessage("&a月记录清理完成".colored())
            }

            sender.sendMessage("&a奖励发放完成".colored())
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

    @CommandBody
    val helper = mainCommand {
        createHelper(checkPermissions = true)
    }

}