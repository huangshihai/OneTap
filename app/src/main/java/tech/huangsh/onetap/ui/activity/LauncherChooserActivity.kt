package tech.huangsh.onetap.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * 桌面选择器Activity
 * 用于触发系统显示默认桌面选择器
 */
class LauncherChooserActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 立即触发桌面选择
        triggerLauncherChooser()
    }

    private fun triggerLauncherChooser() {
        try {
            // 创建Home Intent
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            // 启动Intent，这将显示桌面选择器
            startActivity(homeIntent)
            
            // 关闭当前Activity
            finish()
        } catch (e: Exception) {
            // 如果失败，关闭Activity
            finish()
        }
    }
}
