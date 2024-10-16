package me.cunzai.plugin.onlinerewards.data

import me.cunzai.plugin.onlinerewards.misc.isThisMonth
import me.cunzai.plugin.onlinerewards.misc.isToday
import me.cunzai.plugin.onlinerewards.database.MySQLHandler
import taboolib.common.platform.function.submitAsync
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class PlayerData(val name: String) {

    companion object {
        @JvmStatic
        val cache = ConcurrentHashMap<UUID, PlayerData>()

    }

    var joinedAt = -1L

    var dailyOnline = 0L
    val claimedDailyRewards = HashSet<String>()
    var dailyLastRefresh = -1L

    var monthlyOnline = 0L
    var monthlyOnlineRefreshedAt = -1L
    val claimedMonthlyRewards = HashSet<String>()

    private fun checkAndRefresh() {
        if (!dailyLastRefresh.isToday()) {
            joinedAt = System.currentTimeMillis()
            dailyOnline = 0L
            dailyLastRefresh = System.currentTimeMillis()
            claimedDailyRewards.clear()

            if (!monthlyOnlineRefreshedAt.isThisMonth()) {
                monthlyOnline = 0L
                monthlyOnlineRefreshedAt = System.currentTimeMillis()
                claimedMonthlyRewards.clear()
            }

            submitAsync {
                update()
            }
        }

        // 写入新的数据
        val now = System.currentTimeMillis()
        monthlyOnline += now - joinedAt
        dailyOnline += now - joinedAt
        joinedAt = System.currentTimeMillis()
    }

    fun getMonthlyOnlineTime(): Long {
        checkAndRefresh()
        return monthlyOnline
    }

    fun getDailyOnlineTime(): Long {
        checkAndRefresh()
        return dailyOnline
    }

    fun cache(uuid: UUID) = run { cache[uuid] = this }

    fun update() {
        MySQLHandler.table.workspace(MySQLHandler.datasource) {
            update {
                set("daily_online", getDailyOnlineTime())
                set("monthly_online", getMonthlyOnlineTime())
                set("daily_online_refreshed_at", dailyLastRefresh)
                set("monthly_online_refreshed_at", monthlyOnlineRefreshedAt)
                set("daily_rewards_claimed", claimedDailyRewards.joinToString(":"))
                set("monthly_rewards_claimed", claimedMonthlyRewards.joinToString(":"))
                where {
                    "player_name" eq name
                }
            }
        }.run()
    }

}