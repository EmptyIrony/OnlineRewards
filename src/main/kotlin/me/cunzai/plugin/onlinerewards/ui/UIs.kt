package me.cunzai.plugin.onlinerewards.ui

import me.cunzai.plugin.onlinerewards.data.PlayerData
import me.cunzai.plugin.onlinerewards.misc.millisToRoundedTime
import me.cunzai.plugin.onlinerewards.rewards.Rewards
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.function.submitAsync
import taboolib.library.xseries.XSkull
import taboolib.library.xseries.getItemStack
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.util.getStringColored
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Chest
import taboolib.platform.util.buildItem
import taboolib.platform.util.replaceLore
import taboolib.platform.util.replaceName

object UIs {

    @Config("ui.yml")
    lateinit var config: Configuration

    fun openRewardsUI(player: Player) {
        val section = config.getConfigurationSection("rewards") ?: return
        val data = PlayerData.cache[player.uniqueId] ?: return

        player.openMenu<Chest>(section.getStringColored("title") ?: "null") {
            rows(section.getInt("rows"))
            map(*section.getStringList("format").toTypedArray())

            set('#', section.getItemStack("split")!!) {
                isCancelled = true
            }

            set('$', section.getItemStack("back")!!) {
                isCancelled = true
                Bukkit.dispatchCommand(
                    player, section.getString("back")!!
                        .replace("%player%", player.name)
                )
            }

            val rewardSlots = getSlots('1')

            for (rewardSlot in rewardSlots) {
                val rewards = Rewards.dailyRewards
                val rewardData = rewards.getOrNull(rewardSlot) ?: break
                val claimedRewards = data.claimedDailyRewards
                if (claimedRewards.contains(rewardData.name)) {
                    set(rewardSlot, section.getItemStack("claimed")!!) {
                        isCancelled = true
                    }
                } else {
                    val onlineTime = data.getDailyOnlineTime()
                    if (onlineTime >= rewardData.requiredOnline) {
                        set(rewardSlot, section.getItemStack("claim_active")!!
                            .replaceName("%reward_name%", rewardData.name)
                            .replaceName("%time%", rewardData.requiredOnlineReadable)
                            .replaceLore("%reward_name%", rewardData.name)
                            .replaceLore("%time%", rewardData.requiredOnlineReadable)) {
                            isCancelled = true

                            claimedRewards += rewardData.name
                            submitAsync {
                                data.update()
                            }

                            for (command in rewardData.rewards) {
                                Bukkit.dispatchCommand(
                                    Bukkit.getConsoleSender(),
                                    command.replace("%player%", player.name)
                                )
                            }
                            openRewardsUI(player)
                        }
                    } else {
                        set(rewardSlot, section.getItemStack("claim_inactive")!!
                            .replaceName("%reward_name%", rewardData.name)
                            .replaceName("%time%", rewardData.requiredOnlineReadable)
                            .replaceLore("%reward_name%", rewardData.name)
                            .replaceLore("%time%", rewardData.requiredOnlineReadable)) {
                            isCancelled = true
                        }
                    }
                }
            }
        }
    }
}