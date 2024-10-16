package me.cunzai.plugin.onlinerewards.misc

import me.cunzai.plugin.onlinerewards.database.MySQLHandler
import taboolib.common.platform.Schedule
import taboolib.module.database.Order
import java.util.concurrent.CopyOnWriteArrayList

object LeaderboardHandler {
    val leaders = CopyOnWriteArrayList<LeaderboardEntry>()


    @Schedule(period = 20 * 60L, async = true)
    fun s() {
        val list = ArrayList<LeaderboardEntry>()
        MySQLHandler.table.workspace(MySQLHandler.datasource) {
            select {
                limit(10)
                orderBy("monthly_online", Order.Type.DESC)
                where {
                    "monthly_online_refreshed_at" gte getStartOfMonthTimeStamp()
                }
            }
        }.forEach {
            list += LeaderboardEntry(
                getString("player_name"),
                getLong("monthly_online")
            )
        }

        leaders.clear()
        leaders += list
    }


    class LeaderboardEntry(
        val name: String,
        val onlineTime: Long,
    )
}