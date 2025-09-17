package tech.huangsh.onetap.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 应用信息数据模型
 */
@Entity(tableName = "apps")
data class AppInfo(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val iconBytes: ByteArray? = null,
    val isEnabled: Boolean = true,
    val order: Int = 0,
    val installTime: Long = 0,
    val lastUpdateTime: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AppInfo
        return packageName == other.packageName
    }

    override fun hashCode(): Int {
        return packageName.hashCode()
    }
}