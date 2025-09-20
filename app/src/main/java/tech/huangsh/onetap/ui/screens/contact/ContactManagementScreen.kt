package tech.huangsh.onetap.ui.screens.contact

import android.Manifest
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.OnPermissionCallback
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.*
import tech.huangsh.onetap.R
import tech.huangsh.onetap.data.model.Contact
import tech.huangsh.onetap.ui.activity.ContactDetailActivity
import tech.huangsh.onetap.ui.screens.components.CommonTopBar
import tech.huangsh.onetap.ui.screens.components.ContactItem
import tech.huangsh.onetap.viewmodel.ContactViewModel

@Composable
fun ContactManagementScreen(
    onBack: () -> Unit,
    onAddContact: () -> Unit,
    viewModel: ContactViewModel = hiltViewModel()
) {
    val contacts by viewModel.contacts.collectAsState(emptyList())
    val systemContacts by viewModel.systemContacts.collectAsState(emptyList())
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // ComposeReorderable状态
    val state = rememberReorderableLazyListState(onMove = { from, to ->
        val fromIndex = from.index
        val toIndex = to.index
        coroutineScope.launch {
            viewModel.moveContactToPosition(fromIndex, toIndex)
        }
        true
    })

    // 导入联系人底部弹出框状态
    var showImportBottomSheet by remember { mutableStateOf(false) }
    var selectedContacts by remember { mutableStateOf<Set<Contact>>(emptySet()) }
    var searchQuery by remember { mutableStateOf("") }

    // Log contact data for debugging
    LaunchedEffect(contacts) {
        android.util.Log.d("ContactList", "Contacts loaded: ${contacts.size}")
        contacts.forEach { contact ->
            android.util.Log.d("ContactList", "Contact: ${contact.name}, avatarUri: ${contact.avatarUri}")
        }
    }

    // 使用XXPermissions请求联系人权限
    LaunchedEffect(Unit) {
        XXPermissions.with(context as androidx.activity.ComponentActivity)
            .permission(Manifest.permission.READ_CONTACTS)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                    if (allGranted) {
                        // 权限已授予，可以访问通讯录
                        // 这里可以添加权限获得后的处理逻辑
                    }
                }
                
                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    // 权限被拒绝的处理
                    if (doNotAskAgain) {
                        // 权限被永久拒绝，引导用户到设置页面
                        XXPermissions.startPermissionActivity(context, permissions)
                    }
                }
            })
    }

        // 显示导入联系人底部弹出框时加载系统联系人
    LaunchedEffect(showImportBottomSheet) {
        if (showImportBottomSheet) {
            viewModel.loadSystemContacts()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(240, 245, 255))
    ) {
        // 顶部栏 - 使用统一的样式
        CommonTopBar(
            title = stringResource(R.string.contact_management),
            onBack = onBack,
            actions = {
                IconButton(onAddContact) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_contact),
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = { showImportBottomSheet = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.ImportContacts,
                        contentDescription = stringResource(R.string.import_contacts),
                        tint = Color.White
                    )
                }
            }
        )

        // 联系人列表
        if (contacts.isEmpty()) {
            // 空状态
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(R.string.no_contacts),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.add_first_contact),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                state = state.listState,
                modifier = Modifier
                    .fillMaxSize() // 改为fillMaxSize占满整个屏幕
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .reorderable(state),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                contentPadding = PaddingValues(bottom = 80.dp) // 给底部添加额外间距
            ) {
                items(contacts, key = { it.id }) { contact ->
                    ReorderableItem(state, key = contact.id) { isDragging ->
                        DraggableContactItem(
                            contact = contact,
                            isDragging = isDragging,
                            onClick = {
                                ContactDetailActivity.start(context, contact, true)
                            },
                            reorderableState = state
                        )
                    }
                }
            }
        }
    }

    // 导入联系人底部弹出框
    if (showImportBottomSheet) {
        ImportContactsBottomSheet(
            systemContacts = systemContacts,
            selectedContacts = selectedContacts,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onContactSelect = { contact, isSelected ->
                selectedContacts = if (isSelected) {
                    selectedContacts + contact
                } else {
                    selectedContacts - contact
                }
            },
            onImport = { orderedContacts ->
                coroutineScope.launch {
                    // 按照排序后的顺序导入联系人
                    orderedContacts.forEach { contact ->
                        viewModel.addContact(contact)
                    }
                    selectedContacts = emptySet()
                    searchQuery = ""
                    showImportBottomSheet = false
                }
            },
            onDismiss = {
                selectedContacts = emptySet()
                searchQuery = ""
                showImportBottomSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportContactsBottomSheet(
    systemContacts: List<Contact>,
    selectedContacts: Set<Contact>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onContactSelect: (Contact, Boolean) -> Unit,
    onImport: (List<Contact>) -> Unit,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val bottomSheetHeight = screenHeight * 0.8f
    
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val dismissThreshold = 150f // 向下拖动超过这个阈值就关闭
    
    // 动画偏移量
    val animatedOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dragOffset"
    )
    
    // 过滤联系人
    val filteredContacts = remember(systemContacts, searchQuery) {
        if (searchQuery.isBlank()) {
            systemContacts
        } else {
            systemContacts.filter { contact ->
                contact.name.contains(searchQuery, ignoreCase = true) ||
                contact.phone?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }
    
    // 简化：直接使用过滤后的联系人，不需要拖动排序
    
    Dialog(
        onDismissRequest = { }, // 不允许点击外部关闭
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false // 禁用点击外部关闭
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            // 底部弹出内容 - 向上偏移一些
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bottomSheetHeight)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-20).dp)
                    .graphicsLayer {
                        translationY = maxOf(0f, animatedOffset)
                    }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = {
                                // 开始拖动时停止动画
                            },
                            onDragEnd = {
                                if (dragOffset > dismissThreshold) {
                                    onDismiss()
                                } else {
                                    // 回弹到原位置
                                    dragOffset = 0f
                                }
                            }
                        ) { _, dragAmount ->
                            // 只允许向下拖动
                            if (dragAmount > 0 || dragOffset > 0) {
                                dragOffset = maxOf(0f, dragOffset + dragAmount)
                            }
                        }
                    },
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // 顶部拖动指示器
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(2.dp)
                            )
                            .align(Alignment.CenterHorizontally)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 标题
                    Text(
                        text = stringResource(R.string.import_contacts),
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 搜索框 - 圆角样式
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        label = { Text("搜索联系人") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "清除")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        singleLine = true,
                        shape = RoundedCornerShape(25.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 联系人列表
                    Box(modifier = Modifier.weight(1f)) {
                        if (systemContacts.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(stringResource(R.string.loading_contacts))
                            }
                        } else if (filteredContacts.isEmpty()) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "未找到匹配的联系人",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyColumn {
                                items(filteredContacts) { contact ->
                                    val isSelected = selectedContacts.contains(contact)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onContactSelect(contact, !isSelected)
                                            }
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                onContactSelect(contact, checked)
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = contact.name,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                            contact.phone?.let { phone ->
                                                Text(
                                                    text = phone,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // 底部按钮 - 减少间距，向上移动
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.cancel),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        
                        Button(
                            onClick = { 
                                // 传递选中的联系人
                                val selectedContactsList = selectedContacts.toList()
                                onImport(selectedContactsList)
                            },
                            enabled = selectedContacts.isNotEmpty(),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(
                                text = if (selectedContacts.isEmpty()) {
                                    "导入"
                                } else {
                                    "导入 (${selectedContacts.size})"
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DraggableContactItem(
    contact: Contact,
    isDragging: Boolean,
    onClick: () -> Unit,
    reorderableState: ReorderableLazyListState
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isDragging -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = when {
            isDragging -> CardDefaults.cardElevation(defaultElevation = 8.dp)
            else -> CardDefaults.cardElevation(defaultElevation = 2.dp)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(enabled = !isDragging) { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            ContactItem(
                contact = contact,
                modifier = Modifier.size(50.dp),
                onClick = onClick,
                isHomeScreen = false
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 姓名
            Text(
                text = contact.name,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            // 三杠拖动手柄
            Icon(
                imageVector = Icons.Default.DragIndicator,
                contentDescription = stringResource(R.string.drag_to_sort),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(40.dp)
                    .detectReorderAfterLongPress(reorderableState)
                    .padding(8.dp)
            )
        }
    }
}
