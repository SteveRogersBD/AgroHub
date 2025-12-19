package com.example.agrohub.ui.screens.farm

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.agrohub.presentation.field.FieldViewModel
import com.example.agrohub.presentation.field.SaveState
import com.example.agrohub.ui.theme.AgroHubColors
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.tasks.await
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.example.agrohub.domain.model.Field
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.isGranted

/**
 * FieldMapScreen - Full screen map view for field tracking
 *
 * Displays:
 * - Interactive Google Map centered on user's location
 * - Saved field boundaries with polygons
 * - Drawing mode to create new field boundaries
 * - Floating button to add new fields
 *
 * @param navController Navigation controller for screen navigation
 */
@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun FieldMapScreen(
    navController: NavController,
    viewModel: FieldViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Debug: Check username on screen load
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("agrohub_prefs", android.content.Context.MODE_PRIVATE)
        val username = prefs.getString("username", null)
        println("FieldMapScreen: Username in prefs: $username")
        if (username == null) {
            println("FieldMapScreen: WARNING - No username found!")
        }
    }

    // Location permissions
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // User location state
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    // Saved fields from ViewModel
    val savedFields by viewModel.fields.collectAsState()

    // Determine initial camera position
    val initialPosition = remember(savedFields, userLocation) {
        // If there are saved fields, use the most recent one's center
        val mostRecentField = savedFields.firstOrNull()
        when {
            mostRecentField?.centerPoint != null -> {
                LatLng(mostRecentField.centerPoint.latitude, mostRecentField.centerPoint.longitude)
            }
            userLocation != null -> userLocation
            else -> LatLng(28.6139, 77.2090) // Default location
        }
    }

    // Camera position state
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPosition!!, 18f)
    }

    // Update camera when fields are loaded or user location is obtained
    LaunchedEffect(savedFields, userLocation) {
        val mostRecentField = savedFields.firstOrNull()
        val targetPosition = when {
            mostRecentField?.centerPoint != null -> {
                LatLng(mostRecentField.centerPoint.latitude, mostRecentField.centerPoint.longitude)
            }
            userLocation != null -> userLocation
            else -> null
        }

        targetPosition?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it, 18f),
                durationMs = 1000
            )
        }
    }

    // Get user location
    LaunchedEffect(locationPermissions.permissions.all { it.status.isGranted }) {
        if (locationPermissions.permissions.all { it.status.isGranted }) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val location = fusedLocationClient.lastLocation.await()
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude)
                }
            } catch (_: Exception) {
                // Handle error silently, use default location
            }
        }
    }

    // Dialog states
    var showAddFieldDialog by remember { mutableStateOf(false) }
    var showDrawingMode by remember { mutableStateOf(false) }
    var fieldName by remember { mutableStateOf("") }

    // Drawing state
    var drawingPoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    // Field Details & Task States
    var selectedField by remember { mutableStateOf<Field?>(null) }
    var showFieldDetails by remember { mutableStateOf(false) }
    var showTasksDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskContent by remember { mutableStateOf("") }

    // Task list state
    val fieldTasks by viewModel.fieldTasks.collectAsState()
    val addTaskState by viewModel.addTaskState.collectAsState()

    // Load tasks when field is selected for tasks view
    LaunchedEffect(selectedField, showTasksDialog) {
        if (showTasksDialog && selectedField != null) {
            viewModel.loadFieldTasks(selectedField!!.name)
        }
    }

    // Handle add task state
    LaunchedEffect(addTaskState) {
        if (addTaskState is SaveState.Success) {
            showAddTaskDialog = false
            newTaskTitle = ""
            newTaskContent = ""
            viewModel.resetSaveState()
            // Reload tasks
            selectedField?.let { viewModel.loadFieldTasks(it.name) }
        }
    }

    val saveState by viewModel.saveState.collectAsState()

    // Handle save state
    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.Success -> {
                showDrawingMode = false
                drawingPoints = emptyList()
                fieldName = ""
                snackbarHostState.showSnackbar("Field saved successfully!")
                viewModel.resetSaveState()
            }
            is SaveState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (saveState as SaveState.Error).message,
                    duration = SnackbarDuration.Long
                )
                viewModel.resetSaveState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Google Map
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = locationPermissions.permissions.all { it.status.isGranted },
                    mapType = MapType.HYBRID,
                    isTrafficEnabled = false
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = true,
                    compassEnabled = true,
                    scrollGesturesEnabled = true,
                    zoomGesturesEnabled = true,
                    tiltGesturesEnabled = true,
                    rotationGesturesEnabled = true,
                    mapToolbarEnabled = false
                ),
                onMapClick = { latLng ->
                    if (showDrawingMode) {
                        drawingPoints = drawingPoints + latLng
                    }
                }
            ) {
                // Draw saved fields
                savedFields.forEach { field ->
                    if (field.points.isNotEmpty()) {
                        Polygon(
                            points = field.points.map { it.toLatLng() },
                            fillColor = AgroHubColors.LightGreen.copy(alpha = 0.4f),
                            strokeColor = AgroHubColors.White,
                            strokeWidth = 5f
                        )

                        field.points.forEach { point ->
                            Circle(
                                center = point.toLatLng(),
                                radius = 1.0,
                                fillColor = AgroHubColors.DeepGreen,
                                strokeColor = AgroHubColors.DeepGreen,
                                strokeWidth = 2f
                            )
                        }

                        // Add marker at center with field info
                        field.centerPoint?.let { center ->
                            Marker(
                                state = MarkerState(position = center.toLatLng()),
                                title = field.name,
                                snippet = "Click for details",
                                onClick = {
                                    selectedField = field
                                    showFieldDetails = true
                                    true
                                }
                            )
                        }
                    }
                }

                // Draw current drawing points
                if (showDrawingMode && drawingPoints.isNotEmpty()) {
                    // Draw polygon if more than 2 points
                    if (drawingPoints.size > 2) {
                        Polygon(
                            points = drawingPoints,
                            fillColor = Color.Blue.copy(alpha = 0.4f),
                            strokeColor = AgroHubColors.White,
                            strokeWidth = 8f,
                            zIndex = 10f
                        )
                    }

                    drawingPoints.forEach { point ->
                        Circle(
                            center = point,
                            radius = 1.0,
                            fillColor = Color.Blue,
                            strokeColor = Color.Blue,
                            strokeWidth = 2f
                        )
                    }
                }
            }

            // Drawing mode controls
            if (showDrawingMode) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AgroHubColors.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Drawing: $fieldName",
                            style = MaterialTheme.typography.titleMedium,
                            color = AgroHubColors.DeepGreen
                        )
                        Text(
                            text = "Tap on map to draw field boundary",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AgroHubColors.TextSecondary
                        )
                        Text(
                            text = "Points: ${drawingPoints.size} ${if (drawingPoints.size < 3) "(need at least 3)" else "✓"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (drawingPoints.size >= 3) AgroHubColors.DeepGreen else Color.Red
                        )

                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Cancel button
                            Button(
                                onClick = {
                                    showDrawingMode = false
                                    drawingPoints = emptyList()
                                    fieldName = ""
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red
                                )
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cancel")
                            }

                            // Save button
                            Button(
                                onClick = {
                                    if (drawingPoints.size >= 3 && fieldName.isNotBlank()) {
                                        viewModel.saveField(fieldName, drawingPoints)
                                    }
                                },
                                enabled = drawingPoints.size >= 3 && fieldName.isNotBlank() && saveState !is SaveState.Loading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AgroHubColors.DeepGreen
                                )
                            ) {
                                if (saveState is SaveState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = AgroHubColors.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.Check, contentDescription = "Save")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save")
                                }
                            }
                        }
                    }
                }
            }

            // Add Field Button
            if (!showDrawingMode) {
                FloatingActionButton(
                    onClick = { showAddFieldDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = AgroHubColors.DeepGreen,
                    contentColor = AgroHubColors.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Field")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Field")
                    }
                }
            }
        }

        // Add Field Dialog
        if (showAddFieldDialog) {
            Dialog(onDismissRequest = { showAddFieldDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AgroHubColors.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Add New Field",
                            style = MaterialTheme.typography.headlineSmall,
                            color = AgroHubColors.DeepGreen
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = fieldName,
                            onValueChange = { fieldName = it },
                            label = { Text("Field Name") },
                            placeholder = { Text("e.g., North Field, Rice Paddy") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AgroHubColors.DeepGreen,
                                focusedLabelColor = AgroHubColors.DeepGreen,
                                cursorColor = AgroHubColors.DeepGreen
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showAddFieldDialog = false
                                    fieldName = ""
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    if (fieldName.isNotBlank()) {
                                        showAddFieldDialog = false
                                        showDrawingMode = true
                                        drawingPoints = emptyList()
                                    }
                                },
                                enabled = fieldName.isNotBlank(),
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AgroHubColors.DeepGreen
                                )
                            ) {
                                Text("Next")
                            }
                        }
                    }
                }
            }

            // Request location permissions if not granted
            LaunchedEffect(Unit) {
                if (!locationPermissions.permissions.all { it.status.isGranted }) {
                    locationPermissions.launchMultiplePermissionRequest()
                }
            }
        }

        // Field Details Dialog
        if (showFieldDetails && selectedField != null) {
            Dialog(onDismissRequest = { showFieldDetails = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AgroHubColors.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = selectedField!!.name,
                            style = MaterialTheme.typography.headlineMedium,
                            color = AgroHubColors.DeepGreen
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Area: ${"%.2f".format(selectedField!!.areaInSquareMeters)} m²")
                        Text("Address: ${selectedField!!.centerAddress}")

                        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                        Text("Created: ${dateFormat.format(Date(selectedField!!.createdAt))}")

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                showFieldDetails = false
                                showTasksDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AgroHubColors.DeepGreen)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tasks")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(onClick = { showFieldDetails = false }) {
                            Text("Close", color = AgroHubColors.TextSecondary)
                        }
                    }
                }
            }
        }

        // Tasks Dialog
        if (showTasksDialog && selectedField != null) {
            Dialog(onDismissRequest = { showTasksDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.8f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AgroHubColors.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${selectedField!!.name} Tasks",
                            style = MaterialTheme.typography.headlineSmall,
                            color = AgroHubColors.DeepGreen,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (fieldTasks.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No tasks yet", color = AgroHubColors.TextSecondary)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(fieldTasks) { task ->
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = AgroHubColors.BackgroundLight
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(12.dp)
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = task.status == com.example.agrohub.domain.model.TaskStatus.COMPLETED,
                                                onCheckedChange = { isChecked ->
                                                    if (selectedField != null) {
                                                        viewModel.updateTaskStatus(selectedField!!.name, task.id, isChecked)
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = AgroHubColors.DeepGreen)
                                            )
                                            
                                            Spacer(modifier = Modifier.width(8.dp))
                                            
                                            Column {
                                                Text(
                                                    task.title,
                                                    style = MaterialTheme.typography.titleSmall
                                                )
                                                Text(
                                                    task.content,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                                Text(
                                                    task.status.name, 
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (task.status == com.example.agrohub.domain.model.TaskStatus.COMPLETED) 
                                                        AgroHubColors.DeepGreen 
                                                    else 
                                                        AgroHubColors.TextSecondary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showAddTaskDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AgroHubColors.DeepGreen)
                        ) {
                            Text("Add Task")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { showTasksDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Close", color = AgroHubColors.TextSecondary)
                        }
                    }
                }
            }
        }

        // Add Task Dialog
        if (showAddTaskDialog) {
            Dialog(onDismissRequest = { showAddTaskDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AgroHubColors.White)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "New Task",
                            style = MaterialTheme.typography.titleLarge,
                            color = AgroHubColors.DeepGreen
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            label = { Text("Title") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = newTaskContent,
                            onValueChange = { newTaskContent = it },
                            label = { Text("Content") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showAddTaskDialog = false }) {
                                Text("Cancel", color = AgroHubColors.TextSecondary)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (newTaskTitle.isNotBlank() && selectedField != null) {
                                        viewModel.addTask(
                                            selectedField!!.name,
                                            newTaskTitle,
                                            newTaskContent
                                        )
                                    }
                                },
                                enabled = newTaskTitle.isNotBlank() && addTaskState !is SaveState.Loading,
                                colors = ButtonDefaults.buttonColors(containerColor = AgroHubColors.DeepGreen)
                            ) {
                                if (addTaskState is SaveState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = AgroHubColors.White
                                    )
                                } else {
                                    Text("Save")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
