package tech.huangsh.onetap.ui.screens.contact

import android.Manifest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import tech.huangsh.onetap.R
import tech.huangsh.onetap.data.model.Contact
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.OnPermissionCallback
import tech.huangsh.onetap.utils.rememberCameraAppIcon
import tech.huangsh.onetap.utils.rememberGalleryAppIcon
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    contact: Contact?,
    isEditMode: Boolean,
    onBack: () -> Unit,
    onSave: (Contact) -> Unit,
    onDelete: (Contact) -> Unit,
    onImageSelected: (Uri) -> Unit
) {
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var phone by remember { mutableStateOf(contact?.phone ?: "") }
    var wechatNickname by remember { mutableStateOf(contact?.wechatNickname ?: "") }
    var hasVideoCall by remember { mutableStateOf(contact?.hasVideoCall ?: false) }
    var hasVoiceCall by remember { mutableStateOf(contact?.hasVoiceCall ?: false) }
    var hasPhoneCall by remember { mutableStateOf(contact?.hasPhoneCall ?: false) }
    var avatarUri by remember { mutableStateOf(contact?.avatarUri) }
    
    // 当contact对象更新时，更新avatarUri状态
    LaunchedEffect(contact?.avatarUri) {
        avatarUri = contact?.avatarUri
    }

    val context = LocalContext.current
    
    // 头像选择底部弹出框状态
    var showAvatarPickerBottomSheet by remember { mutableStateOf(false) }
    
    // 临时相机拍照文件
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    
    // 相册选择启动器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // 立即更新本地avatarUri状态，用于预览
            avatarUri = it.toString()
            Log.d("ContactDetailScreen", "Selected avatar URI: $avatarUri")
            // 通知Activity处理图片复制
            onImageSelected(it)
        }
    }

    // 相机拍照启动器
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            // 相机拍照成功，使用临时URI
            avatarUri = tempCameraUri.toString()
            Log.d("ContactDetailScreen", "Camera photo taken: $tempCameraUri")
            // 通知Activity处理图片复制
            onImageSelected(tempCameraUri!!)
        }
    }
    

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (contact == null) stringResource(R.string.add_contact)
                        else if (isEditMode) stringResource(R.string.edit_contact)
                        else stringResource(R.string.contact_detail),
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    if (contact != null && isEditMode) {
                        IconButton(onClick = { onDelete(contact) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(20.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 头像选择
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // 头像容器，可点击
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable(enabled = isEditMode) {
                                        showAvatarPickerBottomSheet = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (avatarUri != null) {
                                    AsyncImage(
                                        model = avatarUri,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            modifier = Modifier.size(50.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            if (isEditMode) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                        .clickable {
                                            showAvatarPickerBottomSheet = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isEditMode) {
                            Text(
                                text = if (avatarUri == null) stringResource(R.string.tap_to_add_avatar) else stringResource(R.string.tap_to_change_avatar),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 姓名
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(stringResource(R.string.contact_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = isEditMode || contact == null
                    )
                }

                // 手机号
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(stringResource(R.string.phone_number)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = isEditMode || contact == null
                    )
                }

                // 微信昵称
                item {
                    OutlinedTextField(
                        value = wechatNickname,
                        onValueChange = { wechatNickname = it },
                        label = { Text(stringResource(R.string.wechat_nickname)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = isEditMode || contact == null
                    )
                }

                // 快捷拨号方式
                if (isEditMode || contact == null) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.contact_methods),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // 使用紧凑的行布局来显示三个选项
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // 微信视频
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(enabled = isEditMode || contact == null) {
                                            hasVideoCall = !hasVideoCall
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = hasVideoCall,
                                        onCheckedChange = { hasVideoCall = it },
                                        enabled = isEditMode || contact == null
                                    )
                                    Text(
                                        text = stringResource(R.string.wechat_video),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                
                                // 微信语音
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(enabled = isEditMode || contact == null) {
                                            hasVoiceCall = !hasVoiceCall
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = hasVoiceCall,
                                        onCheckedChange = { hasVoiceCall = it },
                                        enabled = isEditMode || contact == null
                                    )
                                    Text(
                                        text = stringResource(R.string.wechat_voice),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                
                                // 电话拨打
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable(enabled = isEditMode || contact == null) {
                                            hasPhoneCall = !hasPhoneCall
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = hasPhoneCall,
                                        onCheckedChange = { hasPhoneCall = it },
                                        enabled = isEditMode || contact == null
                                    )
                                    Text(
                                        text = stringResource(R.string.phone_call),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }

                // 保存和取消按钮
                if (isEditMode || contact == null) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = onBack,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(stringResource(R.string.cancel))
                            }

                            Button(
                                onClick = {
                                    if (name.isNotBlank() && (hasVideoCall || hasVoiceCall || hasPhoneCall)) {
                                        val newContact = Contact(
                                            id = contact?.id ?: 0,
                                            name = name,
                                            phone = if (phone.isNotBlank()) phone else null,
                                            wechatNickname = wechatNickname.ifBlank { null },
                                            avatarUri = avatarUri,
                                            hasVideoCall = hasVideoCall,
                                            hasVoiceCall = hasVoiceCall,
                                            hasPhoneCall = hasPhoneCall,
                                            order = contact?.order ?: 0
                                        )
                                        onSave(newContact)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = name.isNotBlank() && (hasVideoCall || hasVoiceCall || hasPhoneCall)
                            ) {
                                Text(stringResource(R.string.save))
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 头像选择底部弹出框
    if (showAvatarPickerBottomSheet) {
        AvatarPickerBottomSheet(
            onDismiss = { showAvatarPickerBottomSheet = false },
            onGalleryClick = {
                showAvatarPickerBottomSheet = false
                imagePickerLauncher.launch("image/*")
            },
            onCameraClick = {
                showAvatarPickerBottomSheet = false
                // 使用XXPermissions请求相机权限并启动相机
                XXPermissions.with(context as androidx.activity.ComponentActivity)
                    .permission(Manifest.permission.CAMERA)
                    .request(object : OnPermissionCallback {
                        override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                            if (allGranted) {
                                // 已有权限，直接启动相机
                                val photoFile = File(context.externalCacheDir, "temp_avatar_${System.currentTimeMillis()}.jpg")
                                tempCameraUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    photoFile
                                )
                                cameraLauncher.launch(tempCameraUri!!)
                            }
                        }
                        
                        override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                            if (doNotAskAgain) {
                                // 权限被永久拒绝，引导用户到设置页面
                                XXPermissions.startPermissionActivity(context, permissions)
                            }
                        }
                    })
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPickerBottomSheet(
    onDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    // 获取系统应用图标
    val galleryIcon = rememberGalleryAppIcon()
    val cameraIcon = rememberCameraAppIcon()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "选择头像",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // 图标选择行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 相册图标
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clickable { onGalleryClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (galleryIcon != null) {
                        // 使用系统相册应用的真实图标
                        Image(
                            bitmap = galleryIcon.toBitmap(128, 128).asImageBitmap(),
                            contentDescription = "从相册选择",
                            modifier = Modifier.size(64.dp)
                        )
                    } else {
                        // 降级使用Material图标
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "从相册选择",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
                
                // 相机图标
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clickable { onCameraClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (cameraIcon != null) {
                        // 使用系统相机应用的真实图标
                        Image(
                            bitmap = cameraIcon.toBitmap(128, 128).asImageBitmap(),
                            contentDescription = "拍照",
                            modifier = Modifier.size(64.dp)
                        )
                    } else {
                        // 降级使用Material图标
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "拍照",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}