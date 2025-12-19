package com.example.agrohub.presentation.field

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrohub.data.repository.FieldRepository
import com.example.agrohub.domain.model.Field
import com.example.agrohub.domain.model.FieldPoint
import com.google.android.gms.maps.model.LatLng
import com.example.agrohub.domain.model.FieldTask
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for managing field operations
 */
class FieldViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val repository = FieldRepository(application.applicationContext)
    
    private val _fields = MutableStateFlow<List<Field>>(emptyList())
    val fields: StateFlow<List<Field>> = _fields.asStateFlow()
    
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()
    
    private val _fieldTasks = MutableStateFlow<List<FieldTask>>(emptyList())
    val fieldTasks: StateFlow<List<FieldTask>> = _fieldTasks.asStateFlow()
    
    private val _addTaskState = MutableStateFlow<SaveState>(SaveState.Idle)
    val addTaskState: StateFlow<SaveState> = _addTaskState.asStateFlow()
    
    init {
        loadFields()
    }
    
    private fun loadFields() {
        viewModelScope.launch {
            try {
                repository.getUserFields().collect { fieldList ->
                    _fields.value = fieldList
                }
            } catch (e: Exception) {
                println("FieldViewModel: Error loading fields: ${e.message}")
                // Optionally set an error state or keeping empty list
            }
        }
    }
    
    fun saveField(name: String, boundaries: List<LatLng>) {
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            
            // Debug: Check SharedPreferences
            val prefs = getApplication<Application>().getSharedPreferences("agrohub_prefs", android.content.Context.MODE_PRIVATE)
            val username = prefs.getString("username", null)
            println("FieldViewModel: Username in prefs: $username")
            
            val field = Field(
                name = name,
                points = boundaries.map { FieldPoint.fromLatLng(it) }
            )
            
            println("FieldViewModel: Saving field '$name' with ${boundaries.size} points")
            
            val result = repository.saveField(field)
            _saveState.value = if (result.isSuccess) {
                println("FieldViewModel: Field saved successfully")
                SaveState.Success
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Failed to save field"
                println("FieldViewModel: Error saving field - $errorMsg")
                SaveState.Error(errorMsg)
            }
        }
    }
    
    fun deleteField(fieldId: String) {
        viewModelScope.launch {
            repository.deleteField(fieldId)
        }
    }
    
    fun resetSaveState() {
        _saveState.value = SaveState.Idle
        _addTaskState.value = SaveState.Idle
    }

    fun loadFieldTasks(fieldName: String) {
        viewModelScope.launch {
            try {
                repository.getFieldTasks(fieldName).collect { tasks ->
                    _fieldTasks.value = tasks
                }
            } catch (e: Exception) {
                println("FieldViewModel: Error loading tasks: ${e.message}")
            }
        }
    }
    
    fun addTask(fieldName: String, title: String, content: String) {
        viewModelScope.launch {
            _addTaskState.value = SaveState.Loading
            
            val task = FieldTask(
                title = title,
                content = content
            )
            
            val result = repository.addTask(fieldName, task)
            
            _addTaskState.value = if (result.isSuccess) {
                SaveState.Success
            } else {
                SaveState.Error(result.exceptionOrNull()?.message ?: "Failed to add task")
            }
        }
    }
    
    fun updateTaskStatus(fieldName: String, taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateTaskStatus(fieldName, taskId, isCompleted)
            // Ideally we should reload tasks or update local state optimistically
            // For now, reloading is safer to ensure sync
            loadFieldTasks(fieldName)
        }
    }
}

sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}
