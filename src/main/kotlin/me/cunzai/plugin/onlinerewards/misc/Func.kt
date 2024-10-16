package me.cunzai.plugin.onlinerewards.misc

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

fun Long.isToday(): Boolean {
    val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
    val localDate = localDateTime.toLocalDate()
    return localDate == LocalDate.now()
}

fun Long.isThisMonth(): Boolean {
    // 将Long类型的时间戳转换为Instant对象
    val instant = Instant.ofEpochMilli(this)
    // 使用系统默认时区将Instant转换为LocalDate
    val localDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

    // 获取当前日期
    val now = LocalDate.now(ZoneId.systemDefault())

    // 检查年份和月份是否相同
    return localDate.year == now.year && localDate.month == now.month
}

fun Long.millisToRoundedTime(): String {
    var millis = this
    millis += 1L
    val seconds = millis / 1000L
    val minutes = seconds / 60L
    val hours = minutes / 60L
    val days = hours / 24L
    return if (days > 0) {
        days.toString() + " 天 " + (hours - 24 * days) + " 小时"
    } else if (hours > 0) {
        hours.toString() + " 小时 " + (minutes - 60 * hours) + " 分钟"
    } else if (minutes > 0) {
        minutes.toString() + " 分钟 " + (seconds - 60 * minutes) + " 秒"
    } else {
        "$seconds 秒"
    }
}

fun getStartOfMonthTimeStamp(): Long {
    // 获取当前日期的LocalDate实例
    val currentDate = LocalDate.now()

    // 获取本月的第一天
    val firstDayOfMonth = currentDate.withDayOfMonth(1)

    // 将LocalDate转换为时间戳（毫秒）
    return firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}