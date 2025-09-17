package tech.huangsh.onetap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.sp
import tech.huangsh.onetap.R
import tech.huangsh.onetap.data.model.ThemeMode
import tech.huangsh.onetap.data.model.FontSize

/**
 * 应用主题配置
 */

@Composable
fun OneTapTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    largeText: Boolean = false,
    fontSize: FontSize = FontSize.MEDIUM,
    themeMode: ThemeMode = ThemeMode.BLUE,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        highContrast -> lightColorScheme(
            primary = Color.Black,
            onPrimary = Color.White,
            primaryContainer = Color.Black,
            onPrimaryContainer = Color.White,

            secondary = Color.Black,
            onSecondary = Color.White,
            secondaryContainer = Color.Black,
            onSecondaryContainer = Color.White,

            tertiary = Color.Black,
            onTertiary = Color.White,

            error = Color.Black,
            onError = Color.White,

            background = Color.Black,
            onBackground = Color.White,
            surface = Color.Black,
            onSurface = Color.White,
            surfaceVariant = Color.Black,
            onSurfaceVariant = Color.White,
            outline = Color.White
        )

        darkTheme -> {
            when (themeMode) {
                ThemeMode.BLUE -> darkColorScheme(
                    primary = colorResource(R.color.primary_color),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFF1A3A5C),
                    onPrimaryContainer = Color(0xFFE8F4FD),

                    secondary = colorResource(R.color.secondary_color),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFF1B5E20),
                    onSecondaryContainer = Color(0xFFE8F5E9),

                    tertiary = colorResource(R.color.tertiary_color),
                    onTertiary = Color.White,

                    error = colorResource(R.color.error_color),
                    onError = Color.White,

                    background = Color(0xFF1A1A1A),
                    onBackground = Color(0xFFF0F0F0),
                    surface = Color(0xFF2C2C2C),
                    onSurface = Color(0xFFF0F0F0),
                    surfaceVariant = Color(0xFF3C3C3C),
                    onSurfaceVariant = Color(0xFFD0D0D0),
                    outline = Color(0xFFA0A0A0)
                )
                ThemeMode.ORANGE -> darkColorScheme(
                    primary = colorResource(R.color.orange_primary),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFBF360C),
                    onPrimaryContainer = Color(0xFFFFCCBC),

                    secondary = colorResource(R.color.orange_secondary),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFF3E2723),
                    onSecondaryContainer = Color(0xFFFFAB91),

                    tertiary = colorResource(R.color.orange_tertiary),
                    onTertiary = Color.White,

                    error = colorResource(R.color.error_color),
                    onError = Color.White,

                    background = Color(0xFF1A1A1A),
                    onBackground = Color(0xFFF0F0F0),
                    surface = Color(0xFF2C2C2C),
                    onSurface = Color(0xFFF0F0F0),
                    surfaceVariant = Color(0xFF3C3C3C),
                    onSurfaceVariant = Color(0xFFD0D0D0),
                    outline = Color(0xFFA0A0A0)
                )
            }
        }

        else -> {
            when (themeMode) {
                ThemeMode.BLUE -> lightColorScheme(
                    primary = colorResource(R.color.primary_color),
                    onPrimary = Color.White,
                    primaryContainer = colorResource(R.color.primary_container),
                    onPrimaryContainer = colorResource(R.color.on_primary_container),

                    secondary = colorResource(R.color.secondary_color),
                    onSecondary = Color.White,
                    secondaryContainer = colorResource(R.color.secondary_container),
                    onSecondaryContainer = colorResource(R.color.on_secondary_container),

                    tertiary = colorResource(R.color.tertiary_color),
                    onTertiary = Color.White,

                    error = colorResource(R.color.error_color),
                    onError = Color.White,

                    background = colorResource(R.color.background),
                    onBackground = colorResource(R.color.on_background),
                    surface = colorResource(R.color.surface),
                    onSurface = colorResource(R.color.on_surface),
                    surfaceVariant = colorResource(R.color.surface_variant),
                    onSurfaceVariant = colorResource(R.color.on_surface_variant),
                    outline = colorResource(R.color.outline),
                )
                ThemeMode.ORANGE -> lightColorScheme(
                    primary = colorResource(R.color.orange_primary),
                    onPrimary = Color.White,
                    primaryContainer = colorResource(R.color.orange_primary_container),
                    onPrimaryContainer = colorResource(R.color.orange_on_primary_container),

                    secondary = colorResource(R.color.orange_secondary),
                    onSecondary = Color.White,
                    secondaryContainer = colorResource(R.color.orange_secondary_container),
                    onSecondaryContainer = colorResource(R.color.orange_on_secondary_container),

                    tertiary = colorResource(R.color.orange_tertiary),
                    onTertiary = Color.White,

                    error = colorResource(R.color.error_color),
                    onError = Color.White,

                    background = colorResource(R.color.background),
                    onBackground = colorResource(R.color.on_background),
                    surface = colorResource(R.color.surface),
                    onSurface = colorResource(R.color.on_surface),
                    surfaceVariant = Color(0xFFF4DDD5),
                    onSurfaceVariant = colorResource(R.color.on_surface_variant),
                    outline = colorResource(R.color.outline)
                )
            }
        }
    }

    // 根据字体大小设置调整字体 - 支持三种字体大小
    val adjustedTypography = when (fontSize) {
        FontSize.SMALL -> _root_ide_package_.androidx.compose.material3.Typography(
            displayLarge = Typography.displayLarge.copy(fontSize = 64.sp),
            displayMedium = Typography.displayMedium.copy(fontSize = 42.sp),
            displaySmall = Typography.displaySmall.copy(fontSize = 32.sp),
            headlineLarge = Typography.headlineLarge.copy(fontSize = 32.sp),
            headlineMedium = Typography.headlineMedium.copy(fontSize = 24.sp),
            headlineSmall = Typography.headlineSmall.copy(fontSize = 20.sp),
            titleLarge = Typography.titleLarge.copy(fontSize = 18.sp),
            titleMedium = Typography.titleMedium.copy(fontSize = 14.sp),
            titleSmall = Typography.titleSmall.copy(fontSize = 12.sp),
            bodyLarge = Typography.bodyLarge.copy(fontSize = 16.sp),
            bodyMedium = Typography.bodyMedium.copy(fontSize = 14.sp),
            bodySmall = Typography.bodySmall.copy(fontSize = 12.sp),
            labelLarge = Typography.labelLarge.copy(fontSize = 14.sp),
            labelMedium = Typography.labelMedium.copy(fontSize = 12.sp),
            labelSmall = Typography.labelSmall.copy(fontSize = 10.sp)
        )
        FontSize.MEDIUM -> Typography // 使用默认字体大小
        FontSize.LARGE -> _root_ide_package_.androidx.compose.material3.Typography(
            displayLarge = Typography.displayLarge.copy(fontSize = 80.sp),
            displayMedium = Typography.displayMedium.copy(fontSize = 56.sp),
            displaySmall = Typography.displaySmall.copy(fontSize = 42.sp),
            headlineLarge = Typography.headlineLarge.copy(fontSize = 42.sp),
            headlineMedium = Typography.headlineMedium.copy(fontSize = 34.sp),
            headlineSmall = Typography.headlineSmall.copy(fontSize = 30.sp),
            titleLarge = Typography.titleLarge.copy(fontSize = 28.sp),
            titleMedium = Typography.titleMedium.copy(fontSize = 22.sp),
            titleSmall = Typography.titleSmall.copy(fontSize = 18.sp),
            bodyLarge = Typography.bodyLarge.copy(fontSize = 24.sp),
            bodyMedium = Typography.bodyMedium.copy(fontSize = 22.sp),
            bodySmall = Typography.bodySmall.copy(fontSize = 20.sp),
            labelLarge = Typography.labelLarge.copy(fontSize = 20.sp),
            labelMedium = Typography.labelMedium.copy(fontSize = 18.sp),
            labelSmall = Typography.labelSmall.copy(fontSize = 16.sp)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = adjustedTypography,
        shapes = Shapes,
        content = content
    )
}