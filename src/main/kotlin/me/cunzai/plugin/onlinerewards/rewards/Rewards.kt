package me.cunzai.plugin.onlinerewards.rewards

import org.bukkit.EntityEffect
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.configuration.Path
import taboolib.library.xseries.getItemStack
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Configuration.Companion.toObject

object Rewards {

    @Config("rewards.yml")
    lateinit var config: Configuration

    val dailyRewards = ArrayList<RewardData>()


    @Awake(LifeCycle.ENABLE)
    fun i() {
        dailyRewards.clear()
        val dailySection = config.getConfigurationSection("daily")!!
        for (name in dailySection.getKeys(false)) {
            dailyRewards += dailySection.getConfigurationSection(name)!!.toObject<RewardData>(ignoreConstructor = true).apply {
                icon = dailySection.getConfigurationSection(name)!!.getItemStack("icon")!!
            }
        }
        dailyRewards.sortBy { it.requiredOnline }
    }


    data class RewardData(
        var name: String,
        @Path("required_online")
        val requiredOnline: Long,
        @Path("required_online_readable")
        val requiredOnlineReadable: String,
        val rewards: List<String>,
        var icon: ItemStack
    ) {
        override fun toString(): String {
            return "RewardData(name='$name', requiredOnline=$requiredOnline, requiredOnlineReadable='$requiredOnlineReadable', rewards=$rewards)"
        }
    }

}