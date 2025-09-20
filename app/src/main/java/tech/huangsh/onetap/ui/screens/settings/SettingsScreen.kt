package tech.huangsh.onetap.ui.screens.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import tech.huangsh.onetap.R
import tech.huangsh.onetap.data.model.Settings
import tech.huangsh.onetap.ui.activity.AppManagementActivity
import tech.huangsh.onetap.ui.activity.ContactManagementActivity
import tech.huangsh.onetap.ui.activity.DisplaySettingsActivity
import tech.huangsh.onetap.ui.screens.components.CommonTopBar
import tech.huangsh.onetap.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val settings by settingsViewModel.settings.collectAsState(initial = Settings())
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶部栏 - 使用统一的样式
        CommonTopBar(
            title = stringResource(R.string.settings),
            onBack = onBack
        )

        // 设置列表 - 与首页保持一致的白色卡片风格
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsItem(
                icon = Icons.Default.People,
                title = stringResource(R.string.contact_management),
                onClick = {
                    val intent = Intent(context, ContactManagementActivity::class.java)
                    context.startActivity(intent)
                }
            )

            SettingsItem(
                icon = Icons.Default.Apps,
                title = stringResource(R.string.app_settings),
                onClick = {
                    val intent = Intent(context, AppManagementActivity::class.java)
                    context.startActivity(intent)
                }
            )
            
            // 显示设置
            SettingsItem(
                icon = Icons.Default.DisplaySettings,
                title = stringResource(R.string.display_settings),
                onClick = {
                    val intent = Intent(context, DisplaySettingsActivity::class.java)
                    context.startActivity(intent)
                }
            )
            
            // 桌面启动器设置
            if (settings.showExitLauncher) {
                LauncherSettingsSection(settingsViewModel)
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    trailing: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标背景 - 使用与首页一致的蓝色主题
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // 标题
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            // 尾部内容
            trailing()
        }
    }
}

@Composable
fun LauncherSettingsSection(settingsViewModel: SettingsViewModel) {
    val context = LocalContext.current
    val settings by settingsViewModel.settings.collectAsState(initial = Settings())
    val isDefaultLauncher by settingsViewModel.isDefaultLauncher.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 默认桌面状态
        SettingsItemWithDescription(
            icon = Icons.Default.Home,
            title = stringResource(R.string.default_launcher_status),
            description = if (isDefaultLauncher) {
                stringResource(R.string.is_default_launcher)
            } else {
                stringResource(R.string.not_default_launcher) + "\n" + stringResource(R.string.set_as_default_launcher)
            },
            descriptionColor = if (isDefaultLauncher) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            onClick = {
                if (!isDefaultLauncher) {
                    // 先尝试简单方法
                    settingsViewModel.triggerDefaultLauncherChooser()
                } else {
                    // 如果已经是默认桌面，刷新状态
                    settingsViewModel.refreshDefaultLauncherStatus()
                }
            }
        )
        
        // 如果不是默认桌面，提供备用设置方法
        if (!isDefaultLauncher) {
            SettingsItemWithDescription(
                icon = Icons.Default.DisplaySettings,
                title = stringResource(R.string.open_launcher_settings),
                description = "如果上方方法无效，请使用此选项",
                onClick = {
                    settingsViewModel.openDefaultAppSettings()
                }
            )
        }
        
        // 退出桌面启动器
        if (isDefaultLauncher) {
            SettingsItemWithDescription(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = stringResource(R.string.exit_launcher_mode),
                description = stringResource(R.string.exit_launcher_mode_desc),
                onClick = {
                    if (settings.launcherExitConfirmation) {
                        showExitDialog = true
                    } else {
                        settingsViewModel.exitLauncherMode()
                    }
                }
            )
        }
    }
    
    // 退出确认对话框
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.exit_launcher_dialog_title),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.exit_launcher_dialog_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        settingsViewModel.exitLauncherMode()
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SettingsItemWithDescription(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    descriptionColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit = {},
    trailing: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
    }
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标背景 - 使用与首页一致的蓝色主题
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // 标题和描述
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = descriptionColor,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 尾部内容
            trailing()
        }
    }
}