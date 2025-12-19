package com.example.agrohub.presentation.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrohub.data.repository.ContentRepository
import com.example.agrohub.models.NewsResult
import com.example.agrohub.models.VideoResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContentViewModel(private val repository: ContentRepository) : ViewModel() {
    
    private val _videos = MutableStateFlow<List<VideoResult>>(emptyList())
    val videos: StateFlow<List<VideoResult>> = _videos.asStateFlow()
    
    private val _news = MutableStateFlow<List<NewsResult>>(emptyList())
    val news: StateFlow<List<NewsResult>> = _news.asStateFlow()
    
    private val _isLoadingVideos = MutableStateFlow(false)
    val isLoadingVideos: StateFlow<Boolean> = _isLoadingVideos.asStateFlow()
    
    private val _isLoadingNews = MutableStateFlow(false)
    val isLoadingNews: StateFlow<Boolean> = _isLoadingNews.asStateFlow()
    
    init {
        loadContent()
    }
    
    fun loadContent() {
        loadVideos()
        loadNews()
    }
    
    private fun loadVideos() {
        viewModelScope.launch {
            _isLoadingVideos.value = true
            try {
                repository.getAgricultureVideos().onSuccess { videos ->
                    _videos.value = videos.take(5) // Show only first 5 videos
                }
            } catch (e: Exception) {
                println("ContentViewModel: Error loading videos - ${e.message}")
            } finally {
                _isLoadingVideos.value = false
            }
        }
    }
    
    private fun loadNews() {
        viewModelScope.launch {
            _isLoadingNews.value = true
            try {
                repository.getAgricultureNews().onSuccess { news ->
                    _news.value = news.take(5) // Show only first 5 news items
                }
            } catch (e: Exception) {
                println("ContentViewModel: Error loading news - ${e.message}")
            } finally {
                _isLoadingNews.value = false
            }
        }
    }
}
