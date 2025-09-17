package tech.huangsh.onetap.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * 联系人数据模型
 */
@Entity(tableName = "contacts")
@Parcelize
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String? = null,
    val wechatNickname: String? = null,
    val avatarUri: String? = null,
    val order: Int = 0,
    val hasVideoCall: Boolean = false,
    val hasVoiceCall: Boolean = false,
    val hasPhoneCall: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {
    val supportedActions: List<String>
        get() = mutableListOf<String>().apply {
            if (hasVideoCall) add("video")
            if (hasVoiceCall) add("voice")
            if (hasPhoneCall) add("phone")
        }
}