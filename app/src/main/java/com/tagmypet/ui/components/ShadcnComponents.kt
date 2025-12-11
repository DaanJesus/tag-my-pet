package com.tagmypet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tagmypet.ui.theme.BorderColor
import com.tagmypet.ui.theme.Destructive
import com.tagmypet.ui.theme.InputBg
import com.tagmypet.ui.theme.Primary600
import com.tagmypet.ui.theme.Surface
import com.tagmypet.ui.theme.TextPrimary
import com.tagmypet.ui.theme.TextSecondary

// --- 1. BOTÃO ESTILO SHADCN (ATUALIZADO) ---
enum class ButtonVariant {
    PRIMARY, SECONDARY, DESTRUCTIVE, OUTLINE, GHOST
}

@Composable
fun ShadcnButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null
) {
    val containerColor = when (variant) {
        ButtonVariant.PRIMARY -> Primary600
        ButtonVariant.SECONDARY -> Surface // Ghost/Secondary
        ButtonVariant.DESTRUCTIVE -> Destructive
        ButtonVariant.OUTLINE -> Color.Transparent
        ButtonVariant.GHOST -> Color.Transparent
    }

    val contentColor = when (variant) {
        ButtonVariant.PRIMARY, ButtonVariant.DESTRUCTIVE -> Color.White
        else -> TextPrimary
    }

    val border = when (variant) {
        ButtonVariant.SECONDARY, ButtonVariant.OUTLINE -> BorderStroke(1.dp, BorderColor)
        else -> null
    }

    val elevation = if (variant == ButtonVariant.PRIMARY) {
        ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    } else {
        ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp).fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp), // Radius um pouco maior no novo design (lg)
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = BorderColor,
            disabledContentColor = TextSecondary
        ),
        border = border,
        elevation = elevation
    ) {
        if (icon != null) {
            icon()
            // Espaçamento seria tratado no layout pai ou Row interna se necessário
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

// --- 2. INPUT DE TEXTO SHADCN (ATUALIZADO) ---
@Composable
fun ShadcnInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        isError = isError,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary600,
            unfocusedBorderColor = BorderColor,
            errorBorderColor = Destructive,

            // Cores de fundo e texto atualizadas
            focusedContainerColor = Surface,
            unfocusedContainerColor = InputBg, // Fundo levemente cinza/creme quando inativo

            cursorColor = Primary600,

            focusedLabelColor = Primary600,
            unfocusedLabelColor = TextSecondary,

            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,

            focusedPlaceholderColor = TextSecondary.copy(alpha = 0.7f),
            unfocusedPlaceholderColor = TextSecondary.copy(alpha = 0.7f)
        )
    )
}