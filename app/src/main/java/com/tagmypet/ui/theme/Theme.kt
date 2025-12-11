package com.tagmypet.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Tema Claro (Fiel ao design Web React)
private val LightColorScheme = lightColorScheme(
    primary = Primary600,
    onPrimary = Color.White,
    primaryContainer = Primary100,
    onPrimaryContainer = Primary700,

    // Secondary usamos para elementos de apoio (texto secundário/ícones)
    secondary = TextSecondary,
    onSecondary = Color.White,
    secondaryContainer = InputBg,
    onSecondaryContainer = TextPrimary,

    tertiary = PawLove, // Usado para o botão de like/amei

    background = Background, // Cream (Creme suave)
    onBackground = TextPrimary, // Dark Brown

    surface = Surface, // White
    onSurface = TextPrimary,

    error = Destructive,
    errorContainer = DestructiveBg,
    onError = Color.White,

    outline = BorderColor,
    surfaceVariant = InputBg, // Usado para backgrounds de inputs e cards secundários
    onSurfaceVariant = TextSecondary
)

// Tema Escuro (Apenas um fallback básico por enquanto, usando cores invertidas manuais se necessário)
private val DarkColorScheme = darkColorScheme(
    primary = Primary600,
    onPrimary = Color.White,
    background = Color(0xFF1E1B1A), // Um marrom bem escuro quase preto
    surface = Color(0xFF2C2826),
    onBackground = Color(0xFFEDE0D9),
    onSurface = Color(0xFFEDE0D9)
)

@Composable
fun TagMyPetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color desativado para manter a identidade visual Coral/Warm do app
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Barra de status com a cor do background para parecer "infinita"
            window.statusBarColor = colorScheme.background.toArgb()
            // Ícones escuros na barra de status (pois o fundo é claro)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}