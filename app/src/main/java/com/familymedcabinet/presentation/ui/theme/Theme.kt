package com.familymedcabinet.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.familymedcabinet.domain.model.AppTheme

@Composable
fun FamilyMedCabinetTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    highContrast: Boolean = false,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (appTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> systemDark
    }

    val colorScheme = if (dark) {
        darkColorScheme(
            primary = if (highContrast) androidx.compose.ui.graphics.Color(0xFFFFD600) else androidx.compose.ui.graphics.Color(0xFF90CAF9),
            onPrimary = androidx.compose.ui.graphics.Color.Black,
            background = androidx.compose.ui.graphics.Color(0xFF121212),
            surface = androidx.compose.ui.graphics.Color(0xFF1E1E1E)
        )
    } else {
        lightColorScheme(
            primary = if (highContrast) androidx.compose.ui.graphics.Color(0xFF000000) else androidx.compose.ui.graphics.Color(0xFF1565C0),
            onPrimary = androidx.compose.ui.graphics.Color.White
        )
    }

    val base = androidx.compose.material3.Typography()
    val scaledTypography = androidx.compose.material3.Typography(
        displayLarge = base.displayLarge.copy(fontSize = (57 * fontScale).sp),
        headlineMedium = base.headlineMedium.copy(fontSize = (28 * fontScale).sp),
        titleMedium = base.titleMedium.copy(fontSize = (16 * fontScale).sp),
        bodyLarge = base.bodyLarge.copy(fontSize = (16 * fontScale).sp),
        bodyMedium = base.bodyMedium.copy(fontSize = (14 * fontScale).sp),
        labelLarge = base.labelLarge.copy(fontSize = (14 * fontScale).sp)
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = scaledTypography, content = content)
}
