package me.cunzai.plugin.onlinerewards.rewards

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.library.configuration.Path
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Configuration.Companion.toObject

object Rewards {

    @Config("rewards.yml")
    lateinit var config: Configuration

    val dailyRewards = ArrayList<RewardData>()
    val monthlyRewards = ArrayList<RewardData>()

    val leaderRewards = ArrayList<LeaderRewardData>()


    @Awake(LifeCycle.ENABLE)
    fun i() {
        dailyRewards.clear()
        val dailySection = config.getConfigurationSection("daily")!!
        for (name in dailySection.getKeys(false)) {
            dailyRewards += dailySection.getConfigurationSection(name)!!.toObject<RewardData>(ignoreConstructor = true)
        }
        dailyRewards.sortBy { it.requiredOnline }

        monthlyRewards.clear()
        val monthlySection = config.getConfigurationSection("monthly")!!
        for (rewardName in monthlySection.getKeys(false)) {
            monthlyRewards += monthlySection.getConfigurationSection(rewardName)!!.toObject<RewardData>(ignoreConstructor = true)
        }
        monthlyRewards.sortBy { it.requiredOnline }

        leaderRewards.clear()
        val leaderSection = config.getConfigurationSection("leader")!!
        for (rewardName in leaderSection.getKeys(false)) {
            leaderRewards += leaderSection.getConfigurationSection(rewardName)!!.toObject<LeaderRewardData>(ignoreConstructor = true)
        }
    }


    data class RewardData(
        var name: String,
        @Path("required_online")
        val requiredOnline: Long,
        @Path("required_online_readable")
        val requiredOnlineReadable: String,
        val rewards: List<String>
    ) {
        override fun toString(): String {
            return "RewardData(name='$name', requiredOnline=$requiredOnline, requiredOnlineReadable='$requiredOnlineReadable', rewards=$rewards)"
        }
    }

    data class LeaderRewardData(
        val name: String,
        val rewards: List<String>
    )

}