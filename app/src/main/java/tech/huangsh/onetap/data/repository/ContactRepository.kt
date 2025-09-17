package tech.huangsh.onetap.data.repository

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.provider.ContactsContract
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.core.net.toUri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import tech.huangsh.onetap.data.local.dao.ContactDao
import tech.huangsh.onetap.data.model.Contact
import tech.huangsh.onetap.service.wechat.WeChatAccessibility
import tech.huangsh.onetap.service.wechat.WeChatData
import tech.huangsh.onetap.utils.ImageUtils

/**
 * 联系人数据仓库
 */
class ContactRepository(
    private val contactDao: ContactDao,
    private val context: Context
) {
    val allContacts: Flow<List<Contact>> = contactDao.getAllContacts()
    
    fun getContactsCount(): Flow<Int> = contactDao.getContactCount()

    suspend fun getContactById(id: Long): Contact? = contactDao.getContactById(id)

    suspend fun insertContact(contact: Contact): Long {
        val maxOrder = contactDao.getMaxOrder() ?: -1
        val newContact = contact.copy(order = maxOrder + 1)
        return contactDao.insertContact(newContact)
    }

    suspend fun updateContact(contact: Contact) {
        contactDao.updateContact(contact.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteContact(contact: Contact) {
        // 删除头像文件
        ImageUtils.deleteImageFile(contact.avatarUri)
        contactDao.deleteContact(contact)
    }

    suspend fun deleteContactById(id: Int) {
        // 先获取联系人信息以删除头像文件
        val contact = contactDao.getContactById(id.toLong())
        contact?.let {
            ImageUtils.deleteImageFile(it.avatarUri)
        }
        contactDao.deleteContactById(id)
    }

    suspend fun moveContact(contactId: Long, fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return

        // 获取所有联系人并更新顺序
        val contacts = contactDao.getAllContacts().map { it }.first()

        if (fromPosition < toPosition) {
            // 向下移动：将中间的联系人向上移动
            for (i in fromPosition + 1..toPosition) {
                contactDao.updateContactOrder(contacts[i].id, i - 1)
            }
        } else {
            // 向上移动：将中间的联系人向下移动
            for (i in toPosition until fromPosition) {
                contactDao.updateContactOrder(contacts[i].id, i + 1)
            }
        }

        // 更新被拖动的联系人的位置
        contactDao.updateContactOrder(contactId, toPosition)
    }

    fun searchContacts(query: String): Flow<List<Contact>> {
        return contactDao.searchContacts(query)
    }

    /**
     * 从系统通讯录获取联系人
     */
    fun getSystemContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val contentResolver = context.contentResolver

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use { c ->
            val nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (c.moveToNext()) {
                val name = c.getString(nameIndex)
                val phone = c.getString(phoneIndex)

                contacts.add(
                    Contact(
                        name = name,
                        phone = phone,
                        hasPhoneCall = true
                    )
                )
            }
        }

        // 去重并按中文拼音A-Z排序
        return contacts
            .distinctBy { it.name + it.phone }
            .sortedWith(compareBy(tech.huangsh.onetap.utils.ChineseUtils.pinyinComparator) { it.name })
    }

    /**
     * 发起电话呼叫
     */
    fun makePhoneCall(phoneNumber: String): Intent {
        return Intent(Intent.ACTION_CALL).apply {
            data = "tel:$phoneNumber".toUri()
            flags = FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * 发起微信视频通话
     * @return 操作是否成功启动
     */
    fun startWeChatVideoCall(wechatNickname: String?): Boolean {
        if (wechatNickname.isNullOrEmpty()) {
            return false
        }

        // 检查无障碍服务是否启用
        if (!isAccessibilityServiceEnabled()) {
            // 跳转到无障碍服务设置页面
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return false
        }

        // 检查微信应用是否安装弄璋之喜
        if (!isWeChatInstalled()) {
            return false
        }

        // 设置微信视频通话参数
        WeChatData.updateValue(wechatNickname)
        WeChatData.updateVideo(true)
        WeChatData.updateIndex(1)

        // 启动微信应用
        return startWechat()
    }

    /**
     * 发起微信语音通话
     * @return 操作是否成功启动
     */
    fun startWeChatVoiceCall(wechatNickname: String?): Boolean {
        if (wechatNickname.isNullOrEmpty()) {
            return false
        }

        // 检查无障碍服务是否启用
        if (!isAccessibilityServiceEnabled()) {
            // 跳转到无障碍服务设置页面
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return false
        }

        // 检查微信应用是否安装
        if (!isWeChatInstalled()) {
            return false
        }

        // 设置微信语音通话参数
        WeChatData.updateValue(wechatNickname)
        WeChatData.updateVideo(false)
        WeChatData.updateIndex(1)

        // 启动微信应用
        return startWechat()
    }

    /**
     * 启动微信应用
     * @return 是否成功启动
     */
    private fun startWechat(): Boolean {
        return try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage("com.tencent.mm")
            if (launchIntent != null) {
                launchIntent.flags = FLAG_ACTIVITY_NEW_TASK
                launchIntent.setClassName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI")
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查微信应用是否安装
     * @return 微信是否已安装
     */
    private fun isWeChatInstalled(): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.tencent.mm", 0)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "未找到微信应用", Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * 检查无障碍服务是否启用
     */
    private fun isAccessibilityServiceEnabled(): Boolean {
        var accessibilityEnabled: Int
        val serviceId = context.packageName + "/" + WeChatAccessibility::class.java.canonicalName
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                context.applicationContext.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {
            return false
        }
        val colonSplitter: TextUtils.SimpleStringSplitter = TextUtils.SimpleStringSplitter(':')
        if (accessibilityEnabled == 1) {
            val settingValue: String? = Settings.Secure.getString(
                context.applicationContext.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingValue != null) {
                colonSplitter.setString(settingValue)
                while (colonSplitter.hasNext()) {
                    val accessibilityService: String = colonSplitter.next()
                    if (accessibilityService.equals(serviceId, true)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    suspend fun getContactCount(): Int = contactDao.getContactCount().first()

    suspend fun deleteAllContacts() {
        // 先获取所有联系人以删除头像文件
        val contacts = contactDao.getAllContacts().map { it }.first()
        contacts.forEach { contact ->
            ImageUtils.deleteImageFile(contact.avatarUri)
        }
        contactDao.deleteAllContacts()
    }
}