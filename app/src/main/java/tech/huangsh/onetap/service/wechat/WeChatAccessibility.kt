package tech.huangsh.onetap.service.wechat

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class WeChatAccessibility : AccessibilityService() {

    private val tag: String = "WechatAccessibilityTag"

    override fun onInterrupt() {
        WeChatData.updateIndex(0)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val currentActivity = event?.className ?: return
        android.util.Log.d(tag, event.toString())
        android.util.Log.d(tag, String.format("%d", WeChatData.index))
        if (WeChatData.index == 1) {
            if (currentActivity == WeChatActivity.INDEX.id) {
                // 底部导航栏有4个，到首页微信页面
                var tables =
                    rootInActiveWindow.findAccessibilityNodeInfosByViewId(WeChatId.TABLES.id)
                while (tables.isEmpty()) {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    Thread.sleep(500)
                    tables =
                        rootInActiveWindow.findAccessibilityNodeInfosByViewId(WeChatId.TABLES.id)
                }
                Thread.sleep(100)
                tables[0].click()
                WeChatData.updateIndex(2)
            } else if (currentActivity.contains("dialog")) {
                // 有弹窗，返回2次
                performGlobalAction(GLOBAL_ACTION_BACK)
                Thread.sleep(500)
                performGlobalAction(GLOBAL_ACTION_BACK)
                Thread.sleep(500)
            } else {
                // 不在页面，全局返回
                performGlobalAction(GLOBAL_ACTION_BACK)
                Thread.sleep(500)
            }
        }
        if (WeChatData.index == 2) {
            // 点击搜索
            val searchIcon =
                rootInActiveWindow.findAccessibilityNodeInfosByViewId(WeChatId.SEARCH.id)
            if (searchIcon.isNotEmpty()) {
                searchIcon.first().click()
                Thread.sleep(500)
                WeChatData.updateIndex(3)
            }
        }
        if (WeChatData.index == 3) {
            // 输入文字
            val input =
                rootInActiveWindow.findAccessibilityNodeInfosByViewId(WeChatId.INPUT.id)
            if (input.isNotEmpty()) {
                input.first().input(WeChatData.value)
                Thread.sleep(1000)
                WeChatData.updateIndex(4)
            }
        }
        if (WeChatData.index == 4) {
            // 点击搜到的第一个联系人
            if (currentActivity == WeChatActivity.SEARCH.id) {
                val contact = rootInActiveWindow.findAccessibilityNodeInfosByViewId(WeChatId.LIST.id)
                if (contact.isNotEmpty()) {
                    contact.first().click()
                    Thread.sleep(500)
                    WeChatData.updateIndex(5)
                }
            }
        }
        if (WeChatData.index == 5) {
            // 聊天界面点击更多
            val more = rootInActiveWindow.findAccessibilityNodeInfosByViewId(WeChatId.MORE.id)
            if (more.isNotEmpty()) {
                more.first().click()
                Thread.sleep(1000)
                WeChatData.updateIndex(6)
            }
        }
        if (WeChatData.index == 6) {
            // 点击视频通话菜单
            if (currentActivity == WeChatActivity.CHAT.id) {
                val menu = rootInActiveWindow.findAccessibilityNodeInfosByText(WeChatData.findText(false))
                if (menu.isNotEmpty()) {
                    val rect = Rect()
                    menu.first().getBoundsInScreen(rect)
                    performClick(rect.exactCenterX(), rect.exactCenterY())
                    Thread.sleep(500)
                    WeChatData.updateIndex(7)
                }
            }
        }
        if (WeChatData.index == 7) {
            // 点击视频/语音通话选项
            if (currentActivity.contains(WeChatActivity.DIALOG.id)
                || currentActivity == WeChatActivity.DIALOG_OLD.id
            ) {
                val options = rootInActiveWindow.findAccessibilityNodeInfosByText(WeChatData.findText(true))
                if (options.isNotEmpty()) {
                    options.first().click()
                    Thread.sleep(500)
                    WeChatData.updateIndex(0)
                }
            }
        }
    }

    private fun AccessibilityNodeInfo?.click(): Boolean {
        this ?: return false
        return if (isClickable) {
            performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            parent?.click() == true
        }
    }

    private fun AccessibilityNodeInfo?.input(text: String): Boolean {
        this ?: return false
        return if (isEditable) {
            val arguments: Bundle = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        } else {
            parent?.input(text) == true
        }
    }

    private fun performClick(x: Float, y: Float) {
        val gestureBuilder = GestureDescription.Builder()
        val path = Path()
        path.moveTo(x, y)
        gestureBuilder.addStroke(StrokeDescription(path, 0, 1))
        val gestureDescription = gestureBuilder.build()
        dispatchGesture(gestureDescription, null, null)
    }
}