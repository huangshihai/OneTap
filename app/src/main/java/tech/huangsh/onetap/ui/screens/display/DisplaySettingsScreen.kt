package tech.huangsh.onetap.ui.screens.display

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import tech.huangsh.onetap.R
import tech.huangsh.onetap.data.model.FontSize
import tech.huangsh.onetap.data.model.Settings
import tech.huangsh.onetap.data.model.ThemeMode
import tech.huangsh.onetap.ui.screens.components.CommonTopBar
import tech.huangsh.onetap.viewmodel.SettingsViewModel

@Composable
fun DisplaySettingsScreen(
    onBack: () -> Unit,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings by settingsViewModel.settings.collectAsState(initial = Settings())
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶部栏 - 使用统一的样式
        CommonTopBar(
            title = stringResource(R.string.display_settings),
            onBack = onBack
        )

        // 设置列表
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 主题颜色设置 - 紧凑版
            DisplaySettingsItem(
                icon = Icons.Default.Palette,
                title = stringResource(R.string.theme_mode),
                subtitle = when (settings.themeMode) {
                    ThemeMode.BLUE -> stringResource(R.string.theme_blue)
                    ThemeMode.ORANGE -> stringResource(R.string.theme_orange)
                }
            ) {
                // 紧凑的主题选择器
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactThemeOption(
                        themeMode = ThemeMode.BLUE,
                        currentThemeMode = settings.themeMode,
                        title = stringResource(R.string.theme_blue),
                        onClick = { settingsViewModel.updateThemeMode(ThemeMode.BLUE) }
                    )
                    
                    CompactThemeOption(
                        themeMode = ThemeMode.ORANGE,
                        currentThemeMode = settings.themeMode,
                        title = stringResource(R.string.theme_orange),
                        onClick = { settingsViewModel.updateThemeMode(ThemeMode.ORANGE) }
                    )
                }
            }

            // 字体大小设置 - 紧凑版
            DisplaySettingsItem(
                icon = Icons.Default.TextFields,
                title = stringResource(R.string.font_size),
                subtitle = when (settings.fontSize) {
                    FontSize.SMALL -> stringResource(R.string.font_small)
                    FontSize.MEDIUM -> stringResource(R.string.font_medium)
                    FontSize.LARGE -> stringResource(R.string.font_large)
                }
            ) {
                // 紧凑的字体大小选择器
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CompactFontSizeOption(
                        fontSize = FontSize.SMALL,
                        currentFontSize = settings.fontSize,
                        title = stringResource(R.string.font_small),
                        onClick = { settingsViewModel.updateFontSize(FontSize.SMALL) }
                    )
                    
                    CompactFontSizeOption(
                        fontSize = FontSize.MEDIUM,
                        currentFontSize = settings.fontSize,
                        title = stringResource(R.string.font_medium),
                        onClick = { settingsViewModel.updateFontSize(FontSize.MEDIUM) }
                    )
                    
                    CompactFontSizeOption(
                        fontSize = FontSize.LARGE,
                        currentFontSize = settings.fontSize,
                        title = stringResource(R.string.font_large),
                        onClick = { settingsViewModel.updateFontSize(FontSize.LARGE) }
                    )
                }
            }

            // 对比度设置 - 紧凑版
            DisplaySettingsItem(
                icon = Icons.Default.Contrast,
                title = stringResource(R.string.contrast),
                subtitle = if (settings.highContrast) 
                    stringResource(R.string.contrast_high) 
                    else stringResource(R.string.contrast_normal)
            ) {
                // 紧凑的对比度选择器
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CompactContrastOption(
                        isHighContrast = false,
                        currentHighContrast = settings.highContrast,
                        title = stringResource(R.string.contrast_normal),
                        onClick = { settingsViewModel.updateContrastMode(false) }
                    )
                    
                    CompactContrastOption(
                        isHighContrast = true,
                        currentHighContrast = settings.highContrast,
                        title = stringResource(R.string.contrast_high),
                        onClick = { settingsViewModel.updateContrastMode(true) }
                    )
                }
            }
        }
    }
}

@Composable
fun DisplaySettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .let { if (content == {}) it.clickable { onClick() } else it },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 图标背景
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

                // 标题和副标题
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
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (content == {}) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // 内容区域
            if (content != {}) {
                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}

@Composable
fun RowScope.CompactThemeOption(
    themeMode: ThemeMode,
    currentThemeMode: ThemeMode,
    title: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (themeMode == currentThemeMode) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surface,
            contentColor = if (themeMode == currentThemeMode)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun RowScope.CompactFontSizeOption(
    fontSize: FontSize,
    currentFontSize: FontSize,
    title: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (fontSize == currentFontSize) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surface,
            contentColor = if (fontSize == currentFontSize)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = title,
            style = when (fontSize) {
                FontSize.SMALL -> MaterialTheme.typography.labelMedium  // 14sp
                FontSize.MEDIUM -> MaterialTheme.typography.bodyMedium  // 18sp
                FontSize.LARGE -> MaterialTheme.typography.titleMedium  // 20sp
            }
        )
    }
}

@Composable
fun RowScope.CompactContrastOption(
    isHighContrast: Boolean,
    currentHighContrast: Boolean,
    title: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isHighContrast == currentHighContrast) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surface,
            contentColor = if (isHighContrast == currentHighContrast)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurface
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ThemeModeOption(
    themeMode: ThemeMode,
    currentThemeMode: ThemeMode,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (themeMode == currentThemeMode) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (themeMode != currentThemeMode)
            CardDefaults.outlinedCardBorder()
        else
            null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (themeMode == currentThemeMode) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = if (themeMode == currentThemeMode)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun FontSizeOption(
    fontSize: FontSize,
    currentFontSize: FontSize,
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (fontSize == currentFontSize) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (fontSize != currentFontSize)
            CardDefaults.outlinedCardBorder()
        else
            null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (fontSize == currentFontSize) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = if (fontSize == FontSize.LARGE) 
                    MaterialTheme.typography.titleLarge 
                else if (fontSize == FontSize.MEDIUM)
                    MaterialTheme.typography.titleMedium
                else
                    MaterialTheme.typography.titleSmall,
                color = if (fontSize == currentFontSize)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}