package tech.huangsh.onetap.utils

import com.nlf.calendar.Lunar
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author huangshihai
 */
object DateUtils {

    /**
     * 获取格式化的时间
     */
    fun formatTime(date: Date): String {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return timeFormat.format(date)
    }

    /**
     * 获取格式化的日期
     */
    fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault())
        return dateFormat.format(date)
    }

    /**
     * 获取星期
     */
    fun getWeekDay(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date

        val weekday = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "星期日"
            Calendar.MONDAY -> "星期一"
            Calendar.TUESDAY -> "星期二"
            Calendar.WEDNESDAY -> "星期三"
            Calendar.THURSDAY -> "星期四"
            Calendar.FRIDAY -> "星期五"
            Calendar.SATURDAY -> "星期六"
            else -> ""
        }
        return weekday
    }

    fun getLunar(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar 的月份从 0 开始，所以要 +1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val lunar = Lunar.fromYmd(year, month, day)

        // 获取农历月日（中文）
        val lunarYear = lunar.yearInGanZhi // 农历年
        val lunarMonth = lunar.monthInChinese // 农历月（中文，如 "正"、"二"）
        val lunarDay = lunar.dayInChinese // 农历日（中文，如 "初一"、"十五"）

        return "${lunarYear}年${lunarMonth}月${lunarDay}"
    }
}