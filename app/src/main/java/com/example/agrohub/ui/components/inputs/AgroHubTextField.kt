package com.example.agrohub.ui.components.inputs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubShapes
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTheme
import com.example.agrohub.ui.theme.AgroHubTypography

/**
 * AgroHub custom text field component with label and error state support
 * 
 * @param value Current text value
 * @param onValueChange Callback when text changes
 * @param label Label text displayed above the field
 * @param modifier Modifier for customization
 * @param placeholder Placeholder text when field is empty
 * @param isError Whether the field is in error state
 * @param errorMessage Error message to display when isError is true
 * @param enabled Whether the field is enabled for input
 * @param readOnly Whether the field is read-only
 * @param singleLine Whether the field should be single line
 * @param maxLines Maximum number of lines
 * @param leadingIcon Optional leading icon
 * @param trailingIcon Optional trailing icon
 * @param visualTransformation Visual transformation for the text (e.g., password)
 * @param keyboardOptions Keyboard options for input type
 * @param keyboardActions Keyboard actions for IME
 */
@Composable
fun AgroHubTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = modifier) {
        // Label
        Text(
            text = label,
            style = AgroHubTypography.Body.copy(
                color = if (isError) AgroHubColors.CriticalRed else AgroHubColors.TextPrimary
            ),
            modifier = Modifier.padding(bottom = AgroHubSpacing.sm)
        )
        
        // Text Field
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = AgroHubTypography.Body.copy(color = AgroHubColors.TextHint)
                    )
                }
            },
            isError = isError,
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = maxLines,
            leadingIcon = leadingIcon,
            trailingIcon = if (isError) {
                {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = AgroHubColors.CriticalRed
                    )
                }
            } else trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = AgroHubShapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AgroHubColors.DeepGreen,
                unfocusedBorderColor = AgroHubColors.TextHint,
                errorBorderColor = AgroHubColors.CriticalRed,
                focusedLabelColor = AgroHubColors.DeepGreen,
                unfocusedLabelColor = AgroHubColors.TextSecondary,
                cursorColor = AgroHubColors.DeepGreen,
                errorCursorColor = AgroHubColors.CriticalRed
            ),
            textStyle = AgroHubTypography.Body
        )
        
        // Error Message
        if (isError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                style = AgroHubTypography.Caption.copy(color = AgroHubColors.CriticalRed),
                modifier = Modifier.padding(
                    start = AgroHubSpacing.md,
                    top = AgroHubSpacing.xs
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AgroHubTextFieldPreview() {
    AgroHubTheme {
        Column(modifier = Modifier.padding(AgroHubSpacing.md)) {
            AgroHubTextField(
                value = "Sample Text",
                onValueChange = {},
                label = "Farm Name",
                placeholder = "Enter farm name"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AgroHubTextFieldErrorPreview() {
    AgroHubTheme {
        Column(modifier = Modifier.padding(AgroHubSpacing.md)) {
            AgroHubTextField(
                value = "",
                onValueChange = {},
                label = "Farm Name",
                placeholder = "Enter farm name",
                isError = true,
                errorMessage = "Farm name is required"
            )
        }
    }
}
