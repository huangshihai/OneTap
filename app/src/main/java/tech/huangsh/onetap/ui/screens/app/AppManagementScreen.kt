package tech.huangsh.onetap.ui.screens.app

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import tech.huangsh.onetap.data.model.AppInfo
import tech.huangsh.onetap.ui.screens.components.CommonTopBar
import tech.huangsh.onetap.utils.ImageUtils
import tech.huangsh.onetap.viewmodel.AppViewModel
import tech.huangsh.onetap.R
// 判断是否为用户常用的系统应用
private fun isUserFriendlySystemApp(packageName: String): Boolean {
    val userFriendlyApps = listOf(
        // 基础系统应用
        "com.android.settings",           // 设置
        "com.android.camera",             // 相机
        "com.android.camera2",            // 相机2
        "com.android.gallery3d",          // 图库
        "com.android.contacts",           // 联系人
        "com.android.dialer",             // 拨号
        "com.android.messaging",          // 短信
        "com.android.mms",                // 彩信
        "com.android.calculator2",        // 计算器
        "com.android.calendar",           // 日历
        "com.android.deskclock",          // 时钟
        "com.android.documentsui",        // 文件管理
        "com.android.soundrecorder",      // 录音
        "com.android.music",              // 音乐
        "com.android.browser",            // 浏览器
        "com.android.email",              // 邮件
        
        // Google 应用
        "com.google.android.apps.maps",   // 地图
        "com.google.android.chrome",      // Chrome
        "com.google.android.gm",          // Gmail
        "com.google.android.keep",        // Keep
        "com.google.android.apps.weather", // 天气
        "com.google.android.youtube",     // YouTube
        "com.google.android.apps.photos", // Google相册
        "com.google.android.calculator",  // Google计算器
        "com.google.android.calendar",    // Google日历
        
        // 应用商店
        "com.android.vending",            // Play商店
        "com.huawei.appmarket",           // 华为应用市场
        "com.xiaomi.market",              // 小米应用商店
        "com.bbk.appstore",               // vivo应用商店
        "com.oppo.market",                // OPPO应用商店
        "com.sec.android.app.samsungapps", // 三星应用商店
        
        // 厂商系统应用
        "com.miui.calculator",            // MIUI计算器
        "com.miui.camera",                // MIUI相机
        "com.miui.gallery",               // MIUI相册
        "com.huawei.camera",              // 华为相机
        "com.huawei.photos",              // 华为相册
        "com.samsung.android.gallery3d",  // 三星相册
        "com.sec.android.app.camera"      // 三星相机
    )
    return userFriendlyApps.contains(packageName)
}

@Composable
fun AppManagementScreen(
    onBack: () -> Unit,
    viewModel: AppViewModel = hiltViewModel()
) {
    val enabledApps by viewModel.enabledApps.collectAsState(emptyList())
    val availableApps by viewModel.availableApps.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasPermission by viewModel.hasPermission.collectAsState()
    var showScanDialog by remember { mutableStateOf(false) }
    var showMiuiPermissionDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 标准权限请求启动器
    val standardPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.checkPermission()
        if (isGranted && availableApps.isEmpty()) {
            viewModel.loadAvailableApps()
        }
    }

    // 小米权限请求启动器
    val miuiPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.checkPermission()
        if (isGranted && availableApps.isEmpty()) {
            viewModel.loadAvailableApps()
        }
    }
    
    // 检查权限状态
    LaunchedEffect(Unit) {
        viewModel.checkPermission()
    }
    
    // 监听应用生命周期，当用户从设置页面返回时重新检查权限
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // 监听权限状态变化
    LaunchedEffect(hasPermission) {
        if (hasPermission && availableApps.isEmpty()) {
            viewModel.loadAvailableApps()
        } else if (!hasPermission) {
            when {
                // 小米系统支持动态权限申请
                viewModel.needsDynamicPermissionRequest() -> {
                    miuiPermissionLauncher.launch("com.android.permission.GET_INSTALLED_APPS")
                }
                // 小米系统需要手动设置权限
                viewModel.needsManualPermissionSetting() -> {
                    showMiuiPermissionDialog = true
                }
                // 其他系统Android 11+使用标准权限请求
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                    standardPermissionLauncher.launch(Manifest.permission.QUERY_ALL_PACKAGES)
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(240, 245, 255))
    ) {
        // 顶部栏 - 使用统一的样式
        CommonTopBar(
            title = stringResource(R.string.app_settings),
            onBack = onBack,
            actions = {
                IconButton(
                    onClick = { 
                        if (!isLoading) {
                            if (hasPermission) {
                                viewModel.refreshApps()
                            } else {
                                when {
                                    // 小米系统支持动态权限申请
                                    viewModel.needsDynamicPermissionRequest() -> {
                                        miuiPermissionLauncher.launch("com.android.permission.GET_INSTALLED_APPS")
                                    }
                                    // 小米系统需要手动设置权限
                                    viewModel.needsManualPermissionSetting() -> {
                                        showMiuiPermissionDialog = true
                                    }
                                    // 其他系统Android 11+使用标准权限请求
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                                        standardPermissionLauncher.launch(Manifest.permission.QUERY_ALL_PACKAGES)
                                    }
                                }
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "刷新应用列表",
                            tint = Color.White
                        )
                    }
                }
            }
        )
        
        // 搜索输入框
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            shape = RoundedCornerShape(25.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("搜索应用") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "清除")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                singleLine = true,
                shape = RoundedCornerShape(25.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
        
        // 加载状态提示
        if (isLoading) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "正在刷新应用列表...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // 应用列表
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 过滤应用：排除系统应用但保留用户常用的，并支持搜索
            val filteredApps = availableApps.filter { app ->
                // 过滤逻辑：
                // 1. 保留所有第三方应用（非系统应用）
                // 2. 保留用户常用的系统应用
                val isUserFriendlyApp = when {
                    // 第三方应用：不以系统包名开头
                    !app.packageName.startsWith("com.android.") && 
                    !app.packageName.startsWith("android.") &&
                    !app.packageName.startsWith("com.google.android.") &&
                    !app.packageName.startsWith("com.sec.android.") &&
                    !app.packageName.startsWith("com.samsung.") &&
                    !app.packageName.startsWith("com.miui.") &&
                    !app.packageName.startsWith("com.huawei.") -> true
                    
                    // 系统应用：只保留用户常用的
                    else -> isUserFriendlySystemApp(app.packageName)
                }
                
                // 搜索过滤
                val matchesSearch = if (searchQuery.isBlank()) {
                    true
                } else {
                    app.appName.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)
                }
                
                isUserFriendlyApp && matchesSearch
            }
            
            // 排序：已启用的应用排在前面，然后按应用名称排序
            val appsToShow = filteredApps.sortedWith(compareBy<AppInfo> { app ->
                // 第一优先级：是否已启用（已启用的排在前面）
                !enabledApps.any { it.packageName == app.packageName }
            }.thenBy { app ->
                // 第二优先级：按应用名称排序
                app.appName.lowercase()
            })
            
            items(appsToShow) { app ->
                AppManagementItem(
                    app = app,
                    isEnabled = enabledApps.any { it.packageName == app.packageName },
                    onToggle = { enabled ->
                        if (enabled) {
                            viewModel.enableApp(app)
                        } else {
                            viewModel.disableApp(app.packageName)
                        }
                    }
                )
            }
        }
    }
    
    // 小米系统权限对话框
    if (showMiuiPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showMiuiPermissionDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { 
                Text(
                    text = "需要应用列表权限",
                    style = MaterialTheme.typography.headlineSmall
                ) 
            },
            text = { 
                Column {
                    Text(
                        text = "检测到您使用的是小米手机，需要手动开启\"获取应用列表\"权限。",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "根据《移动智能终端安全能力技术要求》，小米系统需要用户主动授权才能获取应用列表。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "操作步骤：",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. 点击\"去设置\"打开权限管理\n2. 找到\"其他权限\"或\"特殊权限\"\n3. 开启\"获取应用列表\"权限",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMiuiPermissionDialog = false
                        val intent = viewModel.getMiuiPermissionIntent()
                        if (intent != null) {
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // 如果小米权限页面打开失败，使用通用设置页面
                                context.startActivity(viewModel.getAppSettingsIntent())
                            }
                        }
                    }
                ) {
                    Text("去设置")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showMiuiPermissionDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }

    // 扫描应用对话框
    if (showScanDialog) {
        AlertDialog(
            onDismissRequest = { showScanDialog = false },
            title = { Text(stringResource(R.string.scan_apps)) },
            text = { Text(stringResource(R.string.scan_apps_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.scanApps()
                        showScanDialog = false
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showScanDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun AppManagementItem(
    app: AppInfo,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppItem(
                appName = app.appName,
                iconBytes = app.iconBytes,
                modifier = Modifier.size(60.dp),
                onClick = {}
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
fun AppItem(
    appName: String,
    iconBytes: ByteArray? = null,
    iconRes: Int? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .size(88.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 图标 - 增大尺寸
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            ) {
                when {
                    iconBytes != null -> {
                        val bitmap = ImageUtils.byteArrayToBitmap(iconBytes)
                        bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = appName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                    iconRes != null -> {
                        Icon(
                            imageVector = ImageVector.vectorResource(iconRes),
                            contentDescription = appName,
                            modifier = Modifier.fillMaxSize(0.8f),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = appName,
                            modifier = Modifier.fillMaxSize(0.8f),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 应用名称 - 增大字体
            Text(
                text = appName,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                fontWeight = FontWeight.Medium
            )
        }
    }
}