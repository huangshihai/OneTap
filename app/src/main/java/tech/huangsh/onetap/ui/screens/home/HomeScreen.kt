package tech.huangsh.onetap.ui.screens.home

import android.Manifest
import android.content.Intent
import android.widget.Toast
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.OnPermissionCallback
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tech.huangsh.onetap.R
import tech.huangsh.onetap.data.model.AppInfo
import tech.huangsh.onetap.ui.activity.SettingsActivity
import tech.huangsh.onetap.ui.screens.components.ContactActionBottomSheet
import tech.huangsh.onetap.ui.screens.components.ContactItem
import tech.huangsh.onetap.ui.theme.OneTapTheme
import tech.huangsh.onetap.utils.ImageUtils
import tech.huangsh.onetap.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val contacts by viewModel.contacts.collectAsState()
    val apps by viewModel.apps.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState() // 时间
    val currentDate by viewModel.currentDate.collectAsState() // 日期
    val currentWeek by viewModel.currentWeek.collectAsState() // 星期
    val currentLunarDate by viewModel.currentLunarDate.collectAsState() // 农历日期
    val weatherInfo by viewModel.weatherInfo.collectAsState() // 天气
    val showBottomSheet by viewModel.showBottomSheet.collectAsState()
    val selectedContact by viewModel.selectedContact.collectAsState()
    
    // 使用XXPermissions申请电话权限并拨打电话
    fun requestPhonePermissionAndCall(phoneNumber: String) {
        XXPermissions.with(context as androidx.activity.ComponentActivity)
            .permission(Manifest.permission.CALL_PHONE)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (allGranted) {
                        // 权限已授予，执行打电话
                        val intent = viewModel.makePhoneCall(phoneNumber)
                        intent?.let { context.startActivity(it) }
                    }
                }
                
                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    // 权限被拒绝，显示提示
                    if (doNotAskAgain) {
                        Toast.makeText(context, "请在设置中开启电话权限才能拨打电话", Toast.LENGTH_LONG).show()
                        // 可以引导用户到设置页面
                        XXPermissions.startPermissionActivity(context, permissions)
                    } else {
                        Toast.makeText(context, "需要电话权限才能拨打电话", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 顶部时间 + 天气 + 设置按钮
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(16.dp).fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(currentTime, fontSize = 50.sp, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("$currentDate $currentWeek", color = MaterialTheme.colorScheme.onPrimary)
                            Text(
                                "$currentLunarDate  ${weatherInfo?.weatherIcon ?: ""} ${weatherInfo?.weather ?: ""} ${weatherInfo?.temperature ?: ""}°C",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        IconButton(
                            onClick = {
                                val intent = Intent(context, SettingsActivity::class.java)
                                context.startActivity(intent)
                            },
                            modifier = Modifier.size(50.dp).align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings), modifier = Modifier.size(50.dp), tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 常用APP（最多5个）
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        apps.take(5).forEach {
                            AppCard(it, onClick = { packageName ->
                                val intent = viewModel.launchApp(packageName)
                                intent?.let { app -> context.startActivity(app) }
                            })
                        }
                    }

                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 联系人网格 (每行2个)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(contacts) { contact ->
                    ContactItem(
                        contact = contact,
                        onClick = { viewModel.showContactActions(contact) },
                        isHomeScreen = true
                    )
                }
            }
        }

        // 底部菜单
        if (showBottomSheet && selectedContact != null) {
            ContactActionBottomSheet(
                contact = selectedContact!!,
                onDismiss = { viewModel.hideBottomSheet() },
                onVideoCall = {
                    viewModel.startWeChatVideoCall(selectedContact!!.wechatNickname)
                    viewModel.hideBottomSheet()
                },
                onVoiceCall = {
                    viewModel.startWeChatVoiceCall(selectedContact!!.wechatNickname)
                    viewModel.hideBottomSheet()
                },
                onPhoneCall = { phone ->
                    // 使用XXPermissions申请电话权限并拨打电话
                    requestPhonePermissionAndCall(phone)
                    viewModel.hideBottomSheet()
                },
                onCancelCall = {
                    viewModel.hideBottomSheet()
                }
            )
        }
    }
}

@Composable
fun AppCard(app: AppInfo, onClick: (String) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick(app.packageName) }
    ) {
        Card(
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier.size(60.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when {
                    app.iconBytes != null -> {
                        val bitmap = ImageUtils.byteArrayToBitmap(app.iconBytes)
                        bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = app.appName,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    else -> {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = app.appName,
                            modifier = Modifier.fillMaxSize(),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(app.appName, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Preview
@Composable
fun contactCardPreview() {
    OneTapTheme {
        val apps = listOf(
            AppInfo(
                packageName = "tech.huangsh.assistant",
                appName = "微信",
                iconBytes = null,
                isEnabled = true,
                order = 1,
                installTime = 1,
                lastUpdateTime = 1,
            )
        )
        apps.forEach { app ->
            AppCard(app) {
            }
        }
    }
}