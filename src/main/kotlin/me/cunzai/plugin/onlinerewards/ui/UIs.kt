package me.cunzai.plugin.onlinerewards.ui

import me.cunzai.plugin.onlinerewards.data.PlayerData
import me.cunzai.plugin.onlinerewards.misc.LeaderboardHandler
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

    fun openMainUI(player: Player) {
        val section = config.getConfigurationSection("main") ?: return
        val data = PlayerData.cache[player.uniqueId] ?: return
        player.openMenu<Chest>(section.getStringColored("title") ?: "null") {
            rows(section.getInt("rows"))
            map(*section.getStringList("format").toTypedArray())

            set('#', section.getItemStack("split")!!) {
                isCancelled = true
            }
            set('$', section.getItemStack("close")!!) {
                isCancelled = true
                player.closeInventory()
            }
            set('1', buildItem(section.getItemStack("description")!!) {
                skullOwner = player.name
            }.replaceName(
                mapOf(
                    "%player_name%" to player.name,
                    "%daily_played%" to data.getDailyOnlineTime().millisToRoundedTime(),
                    "%monthly_played%" to data.getMonthlyOnlineTime().millisToRoundedTime()
                )
            ).replaceLore(
                mapOf(
                    "%player_name%" to player.name,
                    "%daily_played%" to data.getDailyOnlineTime().millisToRoundedTime(),
                    "%monthly_played%" to data.getMonthlyOnlineTime().millisToRoundedTime()
                )
            )) {
                isCancelled = true
            }
            set('2', buildItem(section.getItemStack("today_rewards")!!)) {
                isCancelled = true
                openRewardsUI(player, false)
            }

            set('3', buildItem(section.getItemStack("monthly_rewards")!!)) {
                isCancelled = true
                openRewardsUI(player, true)
            }

            set('4', buildItem(section.getItemStack("leader")!!)) {
                isCancelled = true
                openLeaderboardUI(player)
            }

        }
    }

    private fun openRewardsUI(player: Player, monthly: Boolean) {
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
                openMainUI(player)
            }

            val rewardSlots = getSlots('1')

            for (rewardSlot in rewardSlots) {
                val rewards = if (monthly) Rewards.monthlyRewards else Rewards.dailyRewards
                val rewardData = rewards.getOrNull(rewardSlot) ?: break
                val claimedRewards = if (monthly) data.claimedMonthlyRewards else data.claimedDailyRewards
                if (claimedRewards.contains(rewardData.name)) {
                    set(rewardSlot, section.getItemStack("claimed")!!) {
                        isCancelled = true
                    }
                } else {
                    val onlineTime = if (monthly) data.getMonthlyOnlineTime() else data.getDailyOnlineTime()
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
                            openRewardsUI(player, monthly)
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

    private fun openLeaderboardUI(player: Player) {
        val section = config.getConfigurationSection("leader") ?: return
        player.openMenu<Chest>(section.getStringColored("title") ?: "null") {
            rows(section.getInt("rows"))
            map(*section.getStringList("format").toTypedArray())

            set('#', section.getItemStack("split")!!) {
                isCancelled = true
            }

            set('!', section.getItemStack("back")!!) {
                isCancelled = true
                openMainUI(player)
            }

            for ((index, leaderSlot) in getSlots('$').withIndex()) {
                val entry = LeaderboardHandler.leaders.getOrNull(index)
                val sortIndex = index + 1
                val name = entry?.name ?: "虚位以待"
                val time = entry?.onlineTime ?: 0L

                val reward = Rewards.leaderRewards.getOrNull(index)
                val rewardName = reward?.name ?: "没有奖励"

                set(leaderSlot, buildItem(section.getItemStack("leader")!!) {
                  skullOwner = name
                }.replaceName(
                    mapOf(
                        "%index%" to sortIndex.toString(),
                        "%name%" to name
                    )
                ).replaceLore(
                    mapOf(
                        "%time%" to time.millisToRoundedTime(),
                        "%reward%" to rewardName
                    )
                )) {
                    isCancelled = true
                }
            }
        }
    }

}