package com.example.myapplication1.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication1.api.RetrofitClient
import com.example.myapplication1.model.ForumComment
import com.example.myapplication1.model.ForumPost
import com.example.myapplication1.model.Lunch
import com.example.myapplication1.model.LunchHistory
import com.example.myapplication1.model.StatsData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class LunchViewModel : ViewModel() {
    companion object {
        private const val TAG = "LunchViewModel"
    }

    private val _lunchList = MutableStateFlow<List<Lunch>>(emptyList())
    val lunchList: StateFlow<List<Lunch>> = _lunchList

    private val _selectedLunch = MutableStateFlow<Lunch?>(null)
    val selectedLunch: StateFlow<Lunch?> = _selectedLunch

    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _historyList = MutableStateFlow<List<LunchHistory>>(emptyList())
    val historyList: StateFlow<List<LunchHistory>> = _historyList

    private val _stats = MutableStateFlow<StatsData?>(null)
    val stats: StateFlow<StatsData?> = _stats

    private val _forumPosts = MutableStateFlow<List<ForumPost>>(emptyList())
    val forumPosts: StateFlow<List<ForumPost>> = _forumPosts

    private val _comments = MutableStateFlow<Map<Long, List<ForumComment>>>(emptyMap())
    val comments: StateFlow<Map<Long, List<ForumComment>>> = _comments

    data class ChatMessage(val role: String, val content: String)

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking

    fun loadLunchList(userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.api.getLunchList(userId)
                if (response.code == 200) {
                    _lunchList.value = response.data ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载列表失败", e)
                _errorMessage.value = errorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun spin(userId: Long, filterTags: String? = null) {
        viewModelScope.launch {
            _isSpinning.value = true
            _selectedLunch.value = null
            _errorMessage.value = null
            try {
                delay(2000)
                val response = RetrofitClient.api.spinLunch(userId, filterTags)
                if (response.code == 200 && response.data != null) {
                    _selectedLunch.value = response.data
                } else {
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                Log.e(TAG, "抽奖失败", e)
                _errorMessage.value = errorMessage(e)
            } finally {
                _isSpinning.value = false
            }
        }
    }

    fun loadHistory(userId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getHistoryList(userId)
                if (response.code == 200) {
                    _historyList.value = response.data ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载历史失败", e)
            }
        }
    }

    fun loadStats(userId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getStats(userId)
                if (response.code == 200) {
                    _stats.value = response.data
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载统计失败", e)
            }
        }
    }

    fun addLunch(lunch: Lunch) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.api.addLunch(lunch)
                if (response.code == 200) {
                    loadLunchList(lunch.userId)
                } else {
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                Log.e(TAG, "添加失败", e)
                _errorMessage.value = errorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateLunch(id: Long, lunch: Lunch) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.api.updateLunch(id, lunch)
                if (response.code == 200) {
                    loadLunchList(lunch.userId)
                } else {
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                Log.e(TAG, "更新失败", e)
                _errorMessage.value = errorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteLunch(id: Long, userId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.api.deleteLunch(id)
                if (response.code == 200) {
                    loadLunchList(userId)
                } else {
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                Log.e(TAG, "删除失败", e)
                _errorMessage.value = errorMessage(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun loadForumPosts() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getForumPosts()
                if (response.code == 200) {
                    _forumPosts.value = response.data ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载帖子失败", e)
            }
        }
    }

    fun createForumPost(post: ForumPost) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.createForumPost(post)
                if (response.code == 200) {
                    loadForumPosts()
                } else {
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                Log.e(TAG, "发帖失败", e)
                _errorMessage.value = errorMessage(e)
            }
        }
    }

    fun likePost(postId: Long, userId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.likePost(postId, mapOf("userId" to userId))
                if (response.code == 200 && response.data != null) {
                    val updated = response.data
                    _forumPosts.value = _forumPosts.value.map {
                        if (it.id == postId) updated else it
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "点赞失败", e)
                _errorMessage.value = errorMessage(e)
            }
        }
    }

    fun deleteForumPost(postId: Long, userId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.deleteForumPost(postId, userId)
                if (response.code == 200) {
                    _forumPosts.value = _forumPosts.value.filter { it.id != postId }
                }
            } catch (e: Exception) {
                Log.e(TAG, "删除帖子失败", e)
                _errorMessage.value = errorMessage(e)
            }
        }
    }

    fun loadComments(postId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.api.getComments(postId)
                if (response.code == 200) {
                    _comments.value = _comments.value + (postId to (response.data ?: emptyList()))
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载评论失败", e)
            }
        }
    }

    fun addComment(postId: Long, userId: Long, nickname: String, content: String) {
        viewModelScope.launch {
            try {
                val comment = ForumComment(
                    postId = postId,
                    userId = userId,
                    nickname = nickname,
                    content = content
                )
                val response = RetrofitClient.api.addComment(comment)
                if (response.code == 200) {
                    loadComments(postId)
                }
            } catch (e: Exception) {
                Log.e(TAG, "评论失败", e)
                _errorMessage.value = errorMessage(e)
            }
        }
    }

    fun aiRecommend(userId: Long, message: String) {
        viewModelScope.launch {
            _isAiThinking.value = true
            _chatMessages.value = _chatMessages.value + ChatMessage("user", message)
            _errorMessage.value = null

            try {
                val response = RetrofitClient.api.aiRecommend(
                    mapOf("userId" to userId.toString(), "message" to message)
                )
                if (response.code == 200 && response.data != null) {
                    _chatMessages.value = _chatMessages.value + ChatMessage("assistant", response.data!!)
                } else {
                    _errorMessage.value = response.message ?: "AI 推荐失败"
                    _chatMessages.value = _chatMessages.value + ChatMessage("assistant", "抱歉，推荐服务暂时不可用，请稍后再试 🙏")
                }
            } catch (e: Exception) {
                Log.e(TAG, "AI推荐失败", e)
                _errorMessage.value = errorMessage(e)
                _chatMessages.value = _chatMessages.value + ChatMessage("assistant", "网络异常，请检查连接后重试")
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
    }

    private fun errorMessage(e: Exception): String = when (e) {
        is UnknownHostException, is ConnectException -> "无法连接服务器，请检查网络"
        is SocketTimeoutException -> "连接超时，请重试"
        else -> "操作失败: ${e.message}"
    }
}
