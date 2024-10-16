package me.cunzai.plugin.onlinerewards.database

import me.cunzai.plugin.onlinerewards.data.PlayerData
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Schedule
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submitAsync
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import taboolib.module.database.*

object MySQLHandler {
    @Config("config.yml")
    lateinit var config: Configuration

    private val host by lazy {
        config.getHost("mysql")
    }

    val datasource by lazy {
        host.createDataSource()
    }

    val table by lazy {
        Table("online_rewards", host) {
            add {
                id()
            }

            add("player_name") {
                type(ColumnTypeSQL.VARCHAR, 64) {
                    options(ColumnOptionSQL.KEY)
                }
            }

            add("daily_online") {
                type(ColumnTypeSQL.BIGINT)
            }

            add("daily_online_refreshed_at") {
                type(ColumnTypeSQL.BIGINT)
            }

            add("daily_rewards_claimed") {
                type(ColumnTypeSQL.LONGTEXT)
            }

            add("monthly_online") {
                type(ColumnTypeSQL.BIGINT) {
                    indexType(IndexType.BTREE)
                    indexDesc(true)
                }
            }

            add("monthly_online_refreshed_at") {
                type(ColumnTypeSQL.BIGINT)
            }

            add("monthly_rewards_claimed") {
                type(ColumnTypeSQL.LONGTEXT)
            }
        }
    }

    fun clearMonthly() {
        table.workspace(datasource) {
            update {
                set("monthly_online", 0)
                set("monthly_online_refreshed_at", System.currentTimeMillis())
            }
        }.run()
    }

    @Awake(LifeCycle.ENABLE)
    fun i() {
        table.workspace(datasource) {
            createTable(checkExists = true)
            createIndex(Index(
                "idx_monthly_online",
                listOf("monthly_online"),
                unique = false,
                checkExists = true
            ))
        }.run()
    }

    @Awake(LifeCycle.DISABLE)
    fun d() {
        for (player in Bukkit.getOnlinePlayers()) {
            val data = PlayerData.cache[player.uniqueId] ?: continue
            data.update()
        }
    }

    @Schedule(async = true, period = 20 * 60L)
    fun s() {
        for (player in Bukkit.getOnlinePlayers()) {
            val data = PlayerData.cache[player.uniqueId] ?: continue
            data.update()
        }
    }

    @SubscribeEvent
    fun e(e: PlayerQuitEvent) {
        val data = PlayerData.cache.remove(e.player.uniqueId) ?: return
        submitAsync {
            data.update()
        }
    }

    @SubscribeEvent
    fun e(e: PlayerJoinEvent) {
        val player = e.player
        submitAsync(delay = 2 * 20L) {
            if (!player.isOnline) {
                return@submitAsync
            }
            player.doLoad()
        }
    }

    fun Player.doLoad() {
        table.workspace(datasource) {
            select {
                where {
                    "player_name" eq name
                }
            }
        }.firstOrNull {
            PlayerData(name).apply {
                joinedAt = System.currentTimeMillis()
                dailyOnline = getLong("daily_online")
                dailyLastRefresh = getLong("daily_online_refreshed_at")
                claimedDailyRewards += getString("daily_rewards_claimed")?.let {
                    val split = it.split(":")
                    split.toHashSet()
                } ?: hashSetOf()

                monthlyOnline = getLong("monthly_online")
                monthlyOnlineRefreshedAt = getLong("monthly_online_refreshed_at")
                claimedMonthlyRewards += getString("monthly_rewards_claimed")?.let {
                    val split = it.split(":")
                    split.toHashSet()
                } ?: hashSetOf()
                cache(uniqueId)
            }
        } ?: run {
            table.workspace(datasource) {
                insert("player_name") {
                    value(name)
                }
            }.run()
            PlayerData(name).apply {
                joinedAt = System.currentTimeMillis()
                cache(uniqueId)
            }
        }
    }

}