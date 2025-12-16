package com.example.agrohub.data.repository

import android.content.Context
import android.location.Geocoder
import com.example.agrohub.data.remote.NetworkModule
import com.example.agrohub.domain.model.Field
import com.example.agrohub.domain.model.FieldPoint
import com.example.agrohub.domain.model.FieldTask
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
    private val fieldsRef = database.getReference("fields")
    private val geocoder = Geocoder(context, Locale.getDefault())
    
    /**
     * Get username from token manager
     */
    private suspend fun getUsername(): String? {
        return try {
            val tokenManager = NetworkModule.provideTokenManager(context) as com.example.agrohub.security.TokenManagerImpl
            val username = tokenManager.getUsername()
            
            if (username.isNullOrBlank()) {
                println("FieldRepository: No username found in storage")
                return null
            }
            
            println("FieldRepository: Found username: $username")
            username
        } catch (e: Exception) {
            println("FieldRepository: Error getting username: ${e.message}")
            e.printStackTrace()
            null
        }
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
     */
    suspend fun saveField(field: Field): Result<String> {
        return try {
            val username = getUsername()
            if (username.isNullOrBlank()) {
                return Result.failure(Exception("Please sign in to save fields"))
            }
            
            // Calculate center point
            val center = field.calculateCenter() ?: return Result.failure(Exception("Invalid field boundaries"))
            
            // Get address for center point
            val address = getAddressFromCoordinates(center.latitude, center.longitude)
            
            // Calculate area
            val area = field.calculateArea()
            
            // Create field ID
            val fieldId = fieldsRef.child(username).push().key 
                ?: return Result.failure(Exception("Failed to generate field ID"))
            
            val fieldData = hashMapOf(
                "id" to fieldId,
                "name" to field.name,
                "username" to username,
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
                "tasks" to field.tasks.map { task ->
                    hashMapOf(
                        "id" to task.id,
                        "title" to task.title,
                        "description" to task.description,
                        "status" to task.status.name,
                        "dueDate" to task.dueDate,
                        "createdAt" to task.createdAt
                    )
                },
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            
            fieldsRef.child(username).child(fieldId).setValue(fieldData).await()
            Result.success(fieldId)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save field: ${e.message}"))
        }
    }
    
    /**
     * Get all fields for the current user as a Flow
     */
    fun getUserFields(): Flow<List<Field>> = callbackFlow {
        val username = try {
            getUsername()
        } catch (e: Exception) {
            null
        }
        
        if (username.isNullOrBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fields = mutableListOf<Field>()
                
                snapshot.children.forEach { fieldSnapshot ->
                    try {
                        val id = fieldSnapshot.child("id").getValue(String::class.java) ?: ""
                        val name = fieldSnapshot.child("name").getValue(String::class.java) ?: ""
                        val user = fieldSnapshot.child("username").getValue(String::class.java) ?: ""
                        
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
                        
                        val tasks = fieldSnapshot.child("tasks").children.mapNotNull { taskSnapshot ->
                            try {
                                FieldTask(
                                    id = taskSnapshot.child("id").getValue(String::class.java) ?: "",
                                    title = taskSnapshot.child("title").getValue(String::class.java) ?: "",
                                    description = taskSnapshot.child("description").getValue(String::class.java) ?: "",
                                    status = taskSnapshot.child("status").getValue(String::class.java)?.let {
                                        try { com.example.agrohub.domain.model.TaskStatus.valueOf(it) }
                                        catch (e: Exception) { com.example.agrohub.domain.model.TaskStatus.PENDING }
                                    } ?: com.example.agrohub.domain.model.TaskStatus.PENDING,
                                    dueDate = taskSnapshot.child("dueDate").getValue(Long::class.java),
                                    createdAt = taskSnapshot.child("createdAt").getValue(Long::class.java) ?: 0L
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        
                        val createdAt = fieldSnapshot.child("createdAt").getValue(Long::class.java) ?: 0L
                        val updatedAt = fieldSnapshot.child("updatedAt").getValue(Long::class.java) ?: 0L
                        
                        fields.add(
                            Field(
                                id = id,
                                name = name,
                                username = user,
                                points = points,
                                centerPoint = centerPoint,
                                centerAddress = centerAddress,
                                areaInSquareMeters = area,
                                tasks = tasks,
                                createdAt = createdAt,
                                updatedAt = updatedAt
                            )
                        )
                    } catch (e: Exception) {
                        // Skip invalid field
                    }
                }
                
                // Sort by createdAt descending (most recent first)
                trySend(fields.sortedByDescending { it.createdAt })
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        fieldsRef.child(username).addValueEventListener(listener)
        
        awaitClose { fieldsRef.child(username).removeEventListener(listener) }
    }
    
    /**
     * Delete a field by ID
     */
    suspend fun deleteField(fieldId: String): Result<Unit> {
        return try {
            val username = getUsername()
            if (username.isNullOrBlank()) {
                return Result.failure(Exception("Please sign in to delete fields"))
            }
            
            fieldsRef.child(username).child(fieldId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete field: ${e.message}"))
        }
    }
}
