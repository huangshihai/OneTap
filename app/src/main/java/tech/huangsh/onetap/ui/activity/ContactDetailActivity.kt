package tech.huangsh.onetap.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import tech.huangsh.onetap.data.model.Contact
import tech.huangsh.onetap.ui.screens.contact.ContactDetailScreen
import tech.huangsh.onetap.ui.theme.OneTapTheme
import tech.huangsh.onetap.utils.ImageUtils
import tech.huangsh.onetap.viewmodel.ContactViewModel
import tech.huangsh.onetap.viewmodel.SettingsViewModel
import tech.huangsh.onetap.data.model.Settings

@AndroidEntryPoint
class ContactDetailActivity : ComponentActivity() {
    
    private val viewModel: ContactViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var copiedImagePath: String? = null
    private var oldImagePath: String? = null
    
    // 用于UI显示的头像路径状态
    private var currentAvatarPath by mutableStateOf<String?>(null)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_CONTACT, Contact::class.java);
        } else {
            intent.getParcelableExtra(EXTRA_CONTACT);
        }
        val isEditMode = intent.getBooleanExtra(EXTRA_IS_EDIT_MODE, false)
        
        // 保存旧的图片路径，用于后续清理
        oldImagePath = contact?.avatarUri
        currentAvatarPath = contact?.avatarUri
        
        setContent {
            val settings by settingsViewModel.settings.collectAsState(initial = Settings())
            OneTapTheme(
                darkTheme = false,
                highContrast = settings.highContrast,
                fontSize = settings.fontSize,
                themeMode = settings.themeMode
            ) {
                var currentContact by remember { 
                    mutableStateOf(
                        contact?.copy(avatarUri = currentAvatarPath) ?: 
                        Contact(name = "", avatarUri = currentAvatarPath)
                    ) 
                }
                
                // 当currentAvatarPath变化时，更新contact对象
                LaunchedEffect(currentAvatarPath) {
                    currentContact = if (contact != null) {
                        contact.copy(avatarUri = currentAvatarPath)
                    } else {
                        Contact(name = "", avatarUri = currentAvatarPath)
                    }
                    Log.d("ContactDetailActivity", "Updated contact avatar path: $currentAvatarPath")
                }
                
                ContactDetailScreen(
                    contact = currentContact,
                    isEditMode = isEditMode,
                    onBack = { finish() },
                    onSave = { updatedContact ->
                        // 使用复制后的图片路径，如果没有新图片则使用原来的路径
                        val finalContact = updatedContact.copy(
                            avatarUri = copiedImagePath ?: updatedContact.avatarUri
                        )
                        
                        // 如果是新图片并且更新成功，删除旧图片文件
                        if (copiedImagePath != null && oldImagePath != null && 
                            oldImagePath != copiedImagePath && 
                            ImageUtils.isImageFileExists(oldImagePath)) {
                            ImageUtils.deleteImageFile(oldImagePath)
                        }
                        
                        if (contact != null) {
                            viewModel.updateContact(finalContact)
                        } else {
                            viewModel.addContact(finalContact)
                        }
                        finish()
                    },
                    onDelete = { contactToDelete ->
                        viewModel.deleteContact(contactToDelete)
                        finish()
                    },
                    onImageSelected = { uri ->
                        selectedImageUri = uri
                        // 复制图片到应用私有目录
                        copiedImagePath = ImageUtils.copyImageToAppDirectory(this, uri)
                        // 更新UI显示的头像路径
                        if (copiedImagePath != null) {
                            currentAvatarPath = copiedImagePath
                            Log.d("ContactDetailActivity", "Avatar copied to: $copiedImagePath")
                        }
                    }
                )
            }
        }
    }
    
    companion object {
        private const val EXTRA_CONTACT = "contact"
        private const val EXTRA_IS_EDIT_MODE = "is_edit_mode"
        
        fun start(context: Context, contact: Contact? = null, isEditMode: Boolean = contact != null) {
            val intent = Intent(context, ContactDetailActivity::class.java).apply {
                putExtra(EXTRA_CONTACT, contact)
                putExtra(EXTRA_IS_EDIT_MODE, isEditMode)
            }
            context.startActivity(intent)
        }
    }
}