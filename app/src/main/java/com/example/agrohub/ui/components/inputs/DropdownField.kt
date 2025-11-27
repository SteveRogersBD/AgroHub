package com.example.agrohub.ui.components.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubShapes
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTheme
import com.example.agrohub.ui.theme.AgroHubTypography

/**
 * Data class representing a dropdown option with optional icon
 */
data class DropdownOption(
    val value: String,
    val label: String,
    val icon: ImageVector? = null
)

/**
 * AgroHub dropdown field component with icon support
 * 
 * @param value Currently selected value
 * @param onValueChange Callback when selection changes
 * @param options List of available options
 * @param label Label text displayed above the field
 * @param modifier Modifier for customization
 * @param placeholder Placeholder text when no selection
 * @param isError Whether the field is in error state
 * @param errorMessage Error message to display when isError is true
 * @param enabled Whether the field is enabled for interaction
 */
@Composable
fun DropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    options: List<DropdownOption>,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "Select an option",
    isError: Boolean = false,
    errorMessage: String = "",
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOption = options.find { it.value == value }
    
    Column(modifier = modifier) {
        // Label
        Text(
            text = label,
            style = AgroHubTypography.Body.copy(
                color = if (isError) AgroHubColors.CriticalRed else AgroHubColors.TextPrimary
            ),
            modifier = Modifier.padding(bottom = AgroHubSpacing.sm)
        )
        
        // Dropdown Field
        OutlinedTextField(
            value = selectedOption?.label ?: "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { expanded = true },
            placeholder = {
                Text(
                    text = placeholder,
                    style = AgroHubTypography.Body.copy(color = AgroHubColors.TextHint)
                )
            },
            readOnly = true,
            enabled = enabled,
            isError = isError,
            leadingIcon = selectedOption?.icon?.let { icon ->
                {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AgroHubColors.DeepGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            trailingIcon = {
                if (isError) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = AgroHubColors.CriticalRed
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = if (enabled) AgroHubColors.TextSecondary else AgroHubColors.TextHint
                    )
                }
            },
            shape = AgroHubShapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AgroHubColors.DeepGreen,
                unfocusedBorderColor = AgroHubColors.TextHint,
                errorBorderColor = AgroHubColors.CriticalRed,
                disabledBorderColor = AgroHubColors.TextHint,
                disabledTextColor = AgroHubColors.TextSecondary
            ),
            textStyle = AgroHubTypography.Body
        )
        
        // Dropdown Menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            option.icon?.let { icon ->
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = AgroHubColors.DeepGreen,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(end = AgroHubSpacing.sm)
                                )
                            }
                            Text(
                                text = option.label,
                                style = AgroHubTypography.Body
                            )
                        }
                    },
                    onClick = {
                        onValueChange(option.value)
                        expanded = false
                    }
                )
            }
        }
        
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
private fun DropdownFieldPreview() {
    AgroHubTheme {
        Column(modifier = Modifier.padding(AgroHubSpacing.md)) {
            DropdownField(
                value = "wheat",
                onValueChange = {},
                options = listOf(
                    DropdownOption("wheat", "Wheat"),
                    DropdownOption("rice", "Rice"),
                    DropdownOption("corn", "Corn")
                ),
                label = "Crop Type",
                placeholder = "Select crop type"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DropdownFieldErrorPreview() {
    AgroHubTheme {
        Column(modifier = Modifier.padding(AgroHubSpacing.md)) {
            DropdownField(
                value = "",
                onValueChange = {},
                options = listOf(
                    DropdownOption("wheat", "Wheat"),
                    DropdownOption("rice", "Rice"),
                    DropdownOption("corn", "Corn")
                ),
                label = "Crop Type",
                placeholder = "Select crop type",
                isError = true,
                errorMessage = "Please select a crop type"
            )
        }
    }
}
