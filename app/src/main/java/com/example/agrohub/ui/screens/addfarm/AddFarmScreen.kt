package com.example.agrohub.ui.screens.addfarm

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.agrohub.ui.components.buttons.PrimaryButton
import com.example.agrohub.ui.components.inputs.AgroHubTextField
import com.example.agrohub.ui.components.inputs.DatePickerField
import com.example.agrohub.ui.components.inputs.DropdownField
import com.example.agrohub.ui.components.inputs.DropdownOption
import com.example.agrohub.ui.icons.AgroHubIcons
import com.example.agrohub.ui.theme.*
import com.google.android.gms.maps.model.LatLng

/**
 * Add Farm screen - Form for adding new farm information
 * 
 * Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6
 */
@Composable
fun AddFarmScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var farmName by remember { mutableStateOf("") }
    var cropType by remember { mutableStateOf("") }
    var farmSize by remember { mutableStateOf("") }
    var sizeUnit by remember { mutableStateOf("Acres") }
    var sowingDate by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AgroHubColors.BackgroundLight)
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AgroHubColors.DeepGreen,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AgroHubSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        imageVector = AgroHubIcons.Back,
                        contentDescription = "Back",
                        tint = AgroHubColors.White
                    )
                }
                
                Spacer(modifier = Modifier.width(AgroHubSpacing.sm))
                
                Text(
                    text = "Add New Farm",
                    style = AgroHubTypography.Heading2,
                    color = AgroHubColors.White
                )
            }
        }
        
        // Form Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(AgroHubSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.lg)
        ) {
            // Farm Form Section
            FarmFormSection(
                farmName = farmName,
                onFarmNameChange = { farmName = it },
                cropType = cropType,
                onCropTypeChange = { cropType = it },
                farmSize = farmSize,
                onFarmSizeChange = { farmSize = it },
                sizeUnit = sizeUnit,
                onSizeUnitChange = { sizeUnit = it },
                sowingDate = sowingDate,
                onSowingDateChange = { sowingDate = it }
            )
            
            // Map Picker Section
            MapPickerSection(
                selectedLocation = selectedLocation,
                onLocationSelect = { selectedLocation = it }
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.md))
            
            // Save Button
            PrimaryButton(
                text = if (isLoading) "Saving..." else "Save Farm",
                onClick = {
                    isLoading = true
                    // Simulate save operation
                    // In real app, this would save to database
                },
                modifier = Modifier.fillMaxWidth(),
                icon = AgroHubIcons.Save,
                isLoading = isLoading
            )
            
            Spacer(modifier = Modifier.height(AgroHubSpacing.xl))
        }
    }
}

/**
 * Farm Form Section - Contains all input fields for farm information
 * 
 * Requirements: 8.1, 8.3, 8.4, 8.5
 */
@Composable
fun FarmFormSection(
    farmName: String,
    onFarmNameChange: (String) -> Unit,
    cropType: String,
    onCropTypeChange: (String) -> Unit,
    farmSize: String,
    onFarmSizeChange: (String) -> Unit,
    sizeUnit: String,
    onSizeUnitChange: (String) -> Unit,
    sowingDate: String,
    onSowingDateChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
    ) {
        Text(
            text = "Farm Information",
            style = AgroHubTypography.Heading3,
            color = AgroHubColors.TextPrimary
        )
        
        // Farm Name Field
        AgroHubTextField(
            value = farmName,
            onValueChange = onFarmNameChange,
            label = "Farm Name",
            placeholder = "Enter farm name",
            modifier = Modifier.fillMaxWidth()
        )
        
        // Crop Type Dropdown
        CropTypeDropdown(
            selectedCrop = cropType,
            onSelect = onCropTypeChange,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Farm Size with Unit Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
        ) {
            AgroHubTextField(
                value = farmSize,
                onValueChange = onFarmSizeChange,
                label = "Farm Size",
                placeholder = "0.0",
                modifier = Modifier.weight(1f)
            )
            
            // Unit Selector
            DropdownField(
                value = sizeUnit,
                onValueChange = onSizeUnitChange,
                label = "Unit",
                options = listOf(
                    DropdownOption("Acres", "Acres"),
                    DropdownOption("Hectares", "Hectares")
                ),
                modifier = Modifier.width(120.dp)
            )
        }
        
        // Sowing Date Picker
        DatePickerField(
            value = sowingDate,
            onValueChange = onSowingDateChange,
            label = "Sowing Date",
            placeholder = "Select sowing date",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Crop Type Dropdown - Dropdown selector with crop options and icons
 * 
 * Requirements: 8.3
 */
@Composable
fun CropTypeDropdown(
    selectedCrop: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val cropOptions = listOf(
        CropOption("Wheat", AgroHubIcons.Seed),
        CropOption("Rice", AgroHubIcons.Plant),
        CropOption("Corn", AgroHubIcons.Harvest),
        CropOption("Cotton", AgroHubIcons.Leaf),
        CropOption("Sugarcane", AgroHubIcons.Plant),
        CropOption("Vegetables", AgroHubIcons.Leaf),
        CropOption("Fruits", AgroHubIcons.Plant),
        CropOption("Pulses", AgroHubIcons.Seed)
    )
    
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        Text(
            text = "Crop Type",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AgroHubColors.TextPrimary,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = AgroHubShapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = AgroHubColors.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                // Selected Crop Display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(AgroHubSpacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val selectedOption = cropOptions.find { it.name == selectedCrop }
                        if (selectedOption != null) {
                            Icon(
                                imageVector = selectedOption.icon,
                                contentDescription = null,
                                tint = AgroHubColors.DeepGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Text(
                            text = selectedCrop.ifEmpty { "Select crop type" },
                            color = if (selectedCrop.isEmpty()) 
                                AgroHubColors.TextSecondary 
                            else 
                                AgroHubColors.TextPrimary
                        )
                    }
                    
                    Icon(
                        imageVector = if (expanded) AgroHubIcons.Collapse else AgroHubIcons.Expand,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = AgroHubColors.TextSecondary
                    )
                }
                
                // Dropdown Menu
                if (expanded) {
                    HorizontalDivider(color = AgroHubColors.LightGreen)
                    
                    cropOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(option.name)
                                    expanded = false
                                }
                                .padding(AgroHubSpacing.md),
                            horizontalArrangement = Arrangement.spacedBy(AgroHubSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = null,
                                tint = AgroHubColors.DeepGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Text(
                                text = option.name,
                                color = AgroHubColors.TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Data class for crop options with icons
 */
private data class CropOption(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

/**
 * Map Picker Section - Interactive map for selecting farm location
 * 
 * Requirements: 8.2
 */
@Composable
fun MapPickerSection(
    selectedLocation: LatLng?,
    onLocationSelect: (LatLng) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
    ) {
        Text(
            text = "Farm Location",
            style = AgroHubTypography.Heading3,
            color = AgroHubColors.TextPrimary
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            shape = AgroHubShapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = AgroHubColors.LightGreen
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        // Simulate location selection
                        // In real app, this would open an interactive map
                        onLocationSelect(LatLng(37.7749, -122.4194))
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AgroHubSpacing.md)
                ) {
                    Icon(
                        imageVector = AgroHubIcons.Location,
                        contentDescription = "Select Location",
                        tint = AgroHubColors.DeepGreen,
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Text(
                        text = if (selectedLocation != null) {
                            "Location Selected"
                        } else {
                            "Tap to select location"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = AgroHubColors.DeepGreen
                    )
                    
                    if (selectedLocation != null) {
                        Text(
                            text = "Lat: ${String.format("%.4f", selectedLocation.latitude)}, " +
                                    "Lng: ${String.format("%.4f", selectedLocation.longitude)}",
                            fontSize = 14.sp,
                            color = AgroHubColors.TextSecondary
                        )
                    } else {
                        Text(
                            text = "Drag marker to adjust position",
                            fontSize = 14.sp,
                            color = AgroHubColors.TextSecondary
                        )
                    }
                }
            }
        }
    }
}
