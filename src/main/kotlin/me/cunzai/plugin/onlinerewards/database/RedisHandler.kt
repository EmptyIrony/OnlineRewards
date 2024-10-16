package me.cunzai.plugin.onlinerewards.database

import me.cunzai.plugin.onlinerewards.data.PlayerData
import me.cunzai.plugin.onlinerewards.database.MySQLHandler.doLoad
import org.bukkit.Bukkit
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.expansion.*
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

object RedisHandler {

    @Config(value = "config.yml")
    lateinit var config: Configuration

    val redisConnection: SingleRedisConnector by lazy {
        AlkaidRedis.create()
            .fromConfig(config.getConfigurationSection("redis")!!)
            .connect()
    }

    @Awake(LifeCycle.ENABLE)
    fun i() {
        redisConnection.connection().apply {
            subscribe("online_rewards_broadcast", patternMode = false) {
                val message: ClearCacheAndReload = get(ignoreConstructor = true)
                PlayerData.cache.clear()
                for (player in Bukkit.getOnlinePlayers()) {
                    player.doLoad()
                }
            }
        }
    }

    class ClearCacheAndReload(val timestamp: Long)

}