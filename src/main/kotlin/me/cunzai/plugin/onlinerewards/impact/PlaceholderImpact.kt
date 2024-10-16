package me.cunzai.plugin.onlinerewards.impact

import me.cunzai.plugin.onlinerewards.data.PlayerData
import me.cunzai.plugin.onlinerewards.misc.millisToRoundedTime
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import taboolib.platform.compat.PlaceholderExpansion

object PlaceholderImpact: PlaceholderExpansion {
    override val identifier: String
        get() = "onlinerewards"

    override val autoReload: Boolean
        get() = true
    override val enabled: Boolean
        get() = true

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        return onPlaceholderRequest(player as? OfflinePlayer?, args)
    }

    override fun onPlaceholderRequest(player: OfflinePlayer?, args: String): String {
        val data = PlayerData.cache[player?.uniqueId] ?: return "null"
        return when (args.lowercase()) {
            "daily" -> {
                data.getDailyOnlineTime().millisToRoundedTime()
            }
            "monthly" -> {
                data.getMonthlyOnlineTime().millisToRoundedTime()
            }
            else -> {
                return "null"
            }
        }
    }
}