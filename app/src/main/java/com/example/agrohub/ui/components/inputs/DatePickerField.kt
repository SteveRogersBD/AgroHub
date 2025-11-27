package com.example.agrohub.ui.components.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.agrohub.ui.theme.AgroHubColors
import com.example.agrohub.ui.theme.AgroHubShapes
import com.example.agrohub.ui.theme.AgroHubSpacing
import com.example.agrohub.ui.theme.AgroHubTheme
import com.example.agrohub.ui.theme.AgroHubTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AgroHub date picker field component with calendar interface
 * 
 * @param value Currently selected date as formatted string
 * @param onValueChange Callback when date selection changes
 * @param label Label text displayed above the field
 * @param modifier Modifier for customization
 * @param placeholder Placeholder text when no date selected
 * @param isError Whether the field is in error state
 * @param errorMessage Error message to display when isError is true
 * @param enabled Whether the field is enabled for interaction
 * @param dateFormat Format pattern for displaying the date (default: "MMM dd, yyyy")
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "Select date",
    isError: Boolean = false,
    errorMessage: String = "",
    enabled: Boolean = true,
    dateFormat: String = "MMM dd, yyyy"
) {
    var showDialog by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat(dateFormat, Locale.getDefault()) }
    
    Column(modifier = modifier) {
        // Label
        Text(
            text = label,
            style = AgroHubTypography.Body.copy(
                color = if (isError) AgroHubColors.CriticalRed else AgroHubColors.TextPrimary
            ),
            modifier = Modifier.padding(bottom = AgroHubSpacing.sm)
        )
        
        // Date Field
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { showDialog = true },
            placeholder = {
                Text(
                    text = placeholder,
                    style = AgroHubTypography.Body.copy(color = AgroHubColors.TextHint)
                )
            },
            readOnly = true,
            enabled = enabled,
            isError = isError,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Calendar",
                    tint = if (enabled) AgroHubColors.DeepGreen else AgroHubColors.TextHint
                )
            },
            trailingIcon = if (isError) {
                {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error",
                        tint = AgroHubColors.CriticalRed
                    )
                }
            } else null,
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
    
    // Date Picker Dialog
    if (showDialog) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled by remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }
        
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formattedDate = dateFormatter.format(Date(millis))
                            onValueChange(formattedDate)
                        }
                        showDialog = false
                    },
                    enabled = confirmEnabled
                ) {
                    Text(
                        text = "OK",
                        style = AgroHubTypography.Body.copy(
                            color = if (confirmEnabled) AgroHubColors.DeepGreen 
                                   else AgroHubColors.TextHint
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(
                        text = "Cancel",
                        style = AgroHubTypography.Body.copy(color = AgroHubColors.TextSecondary)
                    )
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = androidx.compose.material3.DatePickerDefaults.colors(
                    selectedDayContainerColor = AgroHubColors.DeepGreen,
                    todayContentColor = AgroHubColors.DeepGreen,
                    todayDateBorderColor = AgroHubColors.DeepGreen
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DatePickerFieldPreview() {
    AgroHubTheme {
        Column(modifier = Modifier.padding(AgroHubSpacing.md)) {
            DatePickerField(
                value = "Nov 26, 2025",
                onValueChange = {},
                label = "Sowing Date",
                placeholder = "Select sowing date"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DatePickerFieldEmptyPreview() {
    AgroHubTheme {
        Column(modifier = Modifier.padding(AgroHubSpacing.md)) {
            DatePickerField(
                value = "",
                onValueChange = {},
                label = "Sowing Date",
                placeholder = "Select sowing date"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DatePickerFieldErrorPreview() {
    AgroHubTheme {
        Column(modifier = Modifier.padding(AgroHubSpacing.md)) {
            DatePickerField(
                value = "",
                onValueChange = {},
                label = "Sowing Date",
                placeholder = "Select sowing date",
                isError = true,
                errorMessage = "Sowing date is required"
            )
        }
    }
}
