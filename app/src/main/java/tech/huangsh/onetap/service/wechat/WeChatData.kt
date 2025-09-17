package tech.huangsh.onetap.service.wechat

object WeChatData {
    var value: String = ""
    fun updateValue(newValue: String) {
        value = newValue
    }

    var index: Int = 0
    fun updateIndex(newValue: Int) {
        index = newValue
    }

    var video: Boolean = true
    fun updateVideo(newValue: Boolean) {
        video = newValue
    }

    fun findText(options: Boolean): String {
        return if (video || !options) {
            "视频通话"
        } else {
            "语音通话"
        }
    }
}