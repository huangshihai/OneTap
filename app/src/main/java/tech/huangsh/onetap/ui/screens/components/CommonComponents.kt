package tech.huangsh.onetap.ui.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import tech.huangsh.onetap.R
import tech.huangsh.onetap.data.model.Contact

@Composable
fun ContactItem(
    contact: Contact,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    isHomeScreen: Boolean = false // 新增参数标识是否在首页使用
) {
    Card(
        modifier = modifier
            .let { 
                if (isHomeScreen) {
                    it.fillMaxWidth().aspectRatio(1f) // 首页保持正方形
                } else if (modifier == Modifier.size(50.dp)) {
                    it // 在联系人管理页面中，保持指定大小
                } else {
                    it.fillMaxWidth().height(120.dp) // 其他页面使用固定高度
                }
            }
            .clickable { onClick() },
        shape = if (isHomeScreen) RoundedCornerShape(16.dp) else RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHomeScreen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHomeScreen) 1.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 头像 - 在首页中占据大部分空间
            Box(
                modifier = Modifier
                    .let { 
                        if (isHomeScreen) {
                            Modifier.fillMaxWidth().weight(0.85f) // 首页中占据85%的空间
                        } else {
                            Modifier.size(if (modifier == Modifier.size(50.dp)) 50.dp else 100.dp)
                        }
                    }
                    .clip(if (isHomeScreen) RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp) else CircleShape)
                    .background(
                        if (isHomeScreen) 
                            Color.White.copy(alpha = 0.8f) 
                        else 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
            ) {
                if (contact.avatarUri != null) {
                    android.util.Log.d("ContactAvatar", "Loading avatar for ${contact.name}: ${contact.avatarUri}")
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(contact.avatarUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.contact_avatar),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onError = { error ->
                            android.util.Log.e("ContactAvatar", "Error loading avatar for ${contact.name}: ${error.result.throwable}")
                        },
                        onSuccess = {
                            android.util.Log.d("ContactAvatar", "Successfully loaded avatar for ${contact.name}")
                        }
                    )
                } else {
                    android.util.Log.d("ContactAvatar", "No avatar URI for ${contact.name}")
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize(if (isHomeScreen) 0.5f else if (modifier == Modifier.size(50.dp)) 0.7f else 0.7f)
                            .align(Alignment.Center),
                        tint = if (isHomeScreen) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 姓名 - 只在首页显示，其他页面显示方式不变
            if (isHomeScreen) {
                Spacer(modifier = Modifier.height(12.dp)) // 增加间距
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 8.dp) // 增加水平内边距
                )
            } else if (modifier != Modifier.size(50.dp)) { // 在非首页且非联系人管理页面中显示姓名
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactActionBottomSheet(
    contact: Contact,
    onDismiss: () -> Unit,
    onVideoCall: () -> Unit,
    onVoiceCall: () -> Unit,
    onPhoneCall: (String) -> Unit,
    onCancelCall: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            if (contact.hasVideoCall) {
                ActionButton(
                    icon = Icons.Default.VideoCall,
                    text = stringResource(R.string.wechat_video),
                    backgroundColor = colorResource(R.color.action_video_background),
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = onVideoCall
                )
                Spacer(modifier = Modifier.height(15.dp))
            }

            if (contact.hasVoiceCall) {
                ActionButton(
                    icon = Icons.Default.Phone,
                    text = stringResource(R.string.wechat_voice),
                    backgroundColor = colorResource(R.color.action_voice_background),
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = onVoiceCall
                )
                Spacer(modifier = Modifier.height(15.dp))
            }

            if (contact.hasPhoneCall) {
                ActionButton(
                    icon = Icons.Default.Call,
                    text = stringResource(R.string.phone_call),
                    backgroundColor = colorResource(R.color.action_phone_background),
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    onClick = { contact.phone?.let { onPhoneCall(it) } }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            ActionButton(
                text = stringResource(R.string.cancel),
                backgroundColor = colorResource(R.color.action_cancel_background),
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = { onCancelCall() }
            )
        }
    }
}

@Composable
fun CommonTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp, 25.dp, 16.dp, 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.weight(1f)
        )
        actions()
    }
}

@Composable
fun ActionButton(
    icon: ImageVector? = null,
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = contentColor
                )
                Spacer(modifier = Modifier.width(15.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                color = contentColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}