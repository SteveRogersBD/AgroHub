package com.example.agrohub.data.repository

import android.content.Context
import android.location.Geocoder
import com.example.agrohub.domain.model.Field
import com.example.agrohub.domain.model.FieldPoint
import com.example.agrohub.domain.model.FieldTask
import com.example.agrohub.domain.model.TaskStatus
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Locale

/**
 * Repository for managing field data in Firebase Realtime Database
 */
class FieldRepository(private val context: Context) {
    private val database = FirebaseDatabase.getInstance()
    private val geocoder = Geocoder(context, Locale.getDefault())
    private val prefs = context.getSharedPreferences("agrohub_prefs", Context.MODE_PRIVATE)
    
    /**
     * Get username from SharedPreferences
     */
    private fun getUsername(): String? {
        val username = prefs.getString("username", null)
        println("FieldRepository: Username from prefs: $username")
        return username
    }
    
    /**
     * Get address from coordinates using reverse geocoding
     */
    private suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String {
        return try {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                buildString {
                    address.getAddressLine(0)?.let { append(it) }
                }
            } else {
                "Address not found"
            }
        } catch (e: Exception) {
            "Lat: ${"%.6f".format(latitude)}, Lng: ${"%.6f".format(longitude)}"
        }
    }
    
    /**
     * Save a new field to Firebase Realtime Database
     * Structure: users/{username}/fields/{fieldName}/{data}
     */
    suspend fun saveField(field: Field): Result<String> {
        return try {
            println("FieldRepository: Starting saveField for '${field.name}'")
            
            val username = getUsername()
            if (username.isNullOrBlank()) {
                println("FieldRepository: No username found!")
                return Result.failure(Exception("Please sign in to save fields"))
            }
            
            println("FieldRepository: Saving field for username: $username")
            
            // Calculate center point
            val center = field.calculateCenter() ?: run {
                println("FieldRepository: Failed to calculate center")
                return Result.failure(Exception("Invalid field boundaries"))
            }
            
            // Get address for center point
            val address = getAddressFromCoordinates(center.latitude, center.longitude)
            
            // Calculate area and perimeter
            val area = field.calculateArea()
            val perimeter = field.calculatePerimeter()
            
            println("FieldRepository: Center: ${center.latitude}, ${center.longitude}")
            println("FieldRepository: Address: $address")
            println("FieldRepository: Area: $area m²")
            println("FieldRepository: Perimeter: $perimeter m")
            
            // Firebase structure: users/{username}/fields/{fieldName}
            val fieldRef = database.getReference("users")
                .child(username)
                .child("fields")
                .child(field.name)
            
            // Store all field data
            val fieldData = hashMapOf(
                "name" to field.name,
                "points" to field.points.map { point ->
                    hashMapOf(
                        "latitude" to point.latitude,
                        "longitude" to point.longitude
                    )
                },
                "centerPoint" to hashMapOf(
                    "latitude" to center.latitude,
                    "longitude" to center.longitude
                ),
                "centerAddress" to address,
                "areaInSquareMeters" to area,
                "perimeterInMeters" to perimeter,
                "createdAt" to System.currentTimeMillis()
            )
            
            println("FieldRepository: Saving to: users/$username/fields/${field.name}")
            fieldRef.setValue(fieldData).await()
            println("FieldRepository: Field saved successfully!")
            
            Result.success(field.name)
        } catch (e: Exception) {
            println("FieldRepository: Error: ${e.message}")
            e.printStackTrace()
            Result.failure(Exception("Failed to save field: ${e.message}"))
        }
    }
    
    /**
     * Get all fields for the current user as a Flow
     */
    fun getUserFields(): Flow<List<Field>> = callbackFlow {
        val username = getUsername()
        
        if (username.isNullOrBlank()) {
            println("FieldRepository: No username, returning empty list")
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        println("FieldRepository: Loading fields for: $username")
        
        val fieldsRef = database.getReference("users")
            .child(username)
            .child("fields")
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fields = mutableListOf<Field>()
                
                snapshot.children.forEach { fieldSnapshot ->
                    try {
                        val name = fieldSnapshot.child("name").getValue(String::class.java) ?: ""
                        
                        val points = fieldSnapshot.child("points").children.mapNotNull { pointSnapshot ->
                            val lat = pointSnapshot.child("latitude").getValue(Double::class.java)
                            val lng = pointSnapshot.child("longitude").getValue(Double::class.java)
                            if (lat != null && lng != null) {
                                FieldPoint(lat, lng)
                            } else null
                        }
                        
                        val centerLat = fieldSnapshot.child("centerPoint").child("latitude").getValue(Double::class.java)
                        val centerLng = fieldSnapshot.child("centerPoint").child("longitude").getValue(Double::class.java)
                        val centerPoint = if (centerLat != null && centerLng != null) {
                            FieldPoint(centerLat, centerLng)
                        } else null
                        
                        val centerAddress = fieldSnapshot.child("centerAddress").getValue(String::class.java) ?: ""
                        val area = fieldSnapshot.child("areaInSquareMeters").getValue(Double::class.java) ?: 0.0
                        val createdAt = fieldSnapshot.child("createdAt").getValue(Long::class.java) ?: 0L
                        
                        fields.add(
                            Field(
                                id = name,
                                name = name,
                                username = username,
                                points = points,
                                centerPoint = centerPoint,
                                centerAddress = centerAddress,
                                areaInSquareMeters = area,
                                createdAt = createdAt,
                                tasks = parseTasks(fieldSnapshot.child("tasks"))
                            )
                        )
                    } catch (e: Exception) {
                        println("FieldRepository: Error parsing field: ${e.message}")
                    }
                }
                
                trySend(fields.sortedByDescending { it.createdAt })
            }
            
            override fun onCancelled(error: DatabaseError) {
                println("FieldRepository: Firebase error: ${error.message}")
                close(error.toException())
            }
        }
        
        fieldsRef.addValueEventListener(listener)
        awaitClose { fieldsRef.removeEventListener(listener) }
    }
    
    /**
     * Delete a field by name
     */
    suspend fun deleteField(fieldName: String): Result<Unit> {
        return try {
            val username = getUsername()
            if (username.isNullOrBlank()) {
                return Result.failure(Exception("Please sign in to delete fields"))
            }
            
            database.getReference("users")
                .child(username)
                .child("fields")
                .child(fieldName)
                .removeValue()
                .await()
                
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete field: ${e.message}"))
        }
    }
    
    /**
     * Add a task to a specific field
     */
    suspend fun addTask(fieldName: String, task: FieldTask): Result<String> {
        return try {
            val username = getUsername() ?: return Result.failure(Exception("Please sign in"))
            
            val taskRef = database.getReference("users")
                .child(username)
                .child("fields")
                .child(fieldName)
                .child("tasks")
                .push() // Generate unique ID
                
            val taskId = taskRef.key ?: return Result.failure(Exception("Failed to generate task ID"))
            
            val taskWithId = task.copy(id = taskId)
            
            taskRef.setValue(taskWithId).await()
            
            Result.success(taskId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get tasks for a specific field
     */
    fun getFieldTasks(fieldName: String): Flow<List<FieldTask>> = callbackFlow {
        val username = getUsername()
        
        if (username.isNullOrBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val tasksRef = database.getReference("users")
            .child(username)
            .child("fields")
            .child(fieldName)
            .child("tasks")
            
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasks = mutableListOf<FieldTask>()
                
                snapshot.children.forEach { taskSnapshot ->
                    try {
                        val id = taskSnapshot.child("id").getValue(String::class.java) ?: ""
                        val title = taskSnapshot.child("title").getValue(String::class.java) ?: ""
                        val content = taskSnapshot.child("content").getValue(String::class.java) ?: ""
                        val statusStr = taskSnapshot.child("status").getValue(String::class.java) ?: "PENDING"
                        val status = try { TaskStatus.valueOf(statusStr) } catch (e: Exception) { TaskStatus.PENDING }
                        val createdAt = taskSnapshot.child("createdAt").getValue(Long::class.java) ?: 0L
                        
                        tasks.add(FieldTask(id, title, content, status, createdAt))
                    } catch (e: Exception) {
                        println("FieldRepository: Error parsing task: ${e.message}")
                    }
                }
                
                trySend(tasks.sortedByDescending { it.createdAt })
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        tasksRef.addValueEventListener(listener)
        awaitClose { tasksRef.removeEventListener(listener) }
    }

    private fun parseTasks(tasksSnapshot: DataSnapshot): List<FieldTask> {
        val tasks = mutableListOf<FieldTask>()
        try {
            tasksSnapshot.children.forEach { taskSnapshot ->
                val id = taskSnapshot.child("id").getValue(String::class.java) ?: ""
                val title = taskSnapshot.child("title").getValue(String::class.java) ?: ""
                val content = taskSnapshot.child("content").getValue(String::class.java) ?: ""
                val statusStr = taskSnapshot.child("status").getValue(String::class.java) ?: "PENDING"
                val status = try { TaskStatus.valueOf(statusStr) } catch (e: Exception) { TaskStatus.PENDING }
                val createdAt = taskSnapshot.child("createdAt").getValue(Long::class.java) ?: 0L
                
                tasks.add(FieldTask(id, title, content, status, createdAt))
            }
        } catch (e: Exception) {
            println("FieldRepository: Error parsing tasks: ${e.message}")
        }
        return tasks.sortedByDescending { it.createdAt }
    }
    
    suspend fun updateTaskStatus(fieldName: String, taskId: String, isCompleted: Boolean): Result<Unit> {
        return try {
            val username = getUsername() ?: return Result.failure(Exception("Please sign in"))
            
            val status = if (isCompleted) TaskStatus.COMPLETED else TaskStatus.PENDING
            
            // We need to find the specific task key (which might be different from taskId if taskId is just a property)
            // But usually the key IS the taskId if we saved it that way. 
            // In addTask we did:   val taskRef = ...push();  val taskId = taskRef.key;  val taskWithId = task.copy(id = taskId)
            // So the key in Firebase is indeed the taskId.
            
            database.getReference("users")
                .child(username)
                .child("fields")
                .child(fieldName)
                .child("tasks")
                .child(taskId)
                .child("status")
                .setValue(status.name)
                .await()
                
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
