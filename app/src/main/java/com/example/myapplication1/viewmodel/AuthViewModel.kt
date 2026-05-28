package com.example.myapplication1.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication1.api.RetrofitClient
import com.example.myapplication1.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class AuthViewModel : ViewModel() {
    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _actionSuccess = MutableStateFlow<String?>(null)
    val actionSuccess: StateFlow<String?> = _actionSuccess

    fun login(email: String, password: String) {
        viewModelScope.launch {
            Log.d(TAG, "开始登录: email=$email")
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.api.login(
                    mapOf("email" to email, "password" to password)
                )
                Log.d(TAG, "登录响应: code=${response.code}, data=${response.data}")
                if (response.code == 200 && response.data != null) {
                    _user.value = response.data
                    Log.d(TAG, "登录成功: ${_user.value}")
                } else {
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                Log.e(TAG, "登录异常", e)
                _errorMessage.value = when (e) {
                    is UnknownHostException, is ConnectException -> "无法连接服务器，请检查网络"
                    is SocketTimeoutException -> "连接超时，请重试"
                    else -> "网络错误: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            Log.d(TAG, "开始注册: email=$email")
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.api.register(
                    mapOf("email" to email, "password" to password)
                )
                Log.d(TAG, "注册响应: code=${response.code}, data=${response.data}")
                if (response.code == 200 && response.data != null) {
                    _user.value = response.data
                    Log.d(TAG, "注册成功: ${_user.value}")
                } else {
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                Log.e(TAG, "注册异常", e)
                _errorMessage.value = when (e) {
                    is UnknownHostException, is ConnectException -> "无法连接服务器，请检查网络"
                    is SocketTimeoutException -> "连接超时，请重试"
                    else -> "网络错误: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateNickname(userId: Long, nickname: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.api.updateNickname(
                    mapOf("userId" to userId.toString(), "nickname" to nickname)
                )
                if (response.code == 200 && response.data != null) {
                    _user.value = response.data
                    _actionSuccess.value = "昵称修改成功"
                } else {
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                Log.e(TAG, "修改昵称异常", e)
                _errorMessage.value = when (e) {
                    is UnknownHostException, is ConnectException -> "无法连接服务器"
                    else -> "操作失败: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun changePassword(userId: Long, oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = RetrofitClient.api.changePassword(
                    mapOf("userId" to userId.toString(), "oldPassword" to oldPassword, "newPassword" to newPassword)
                )
                if (response.code == 200) {
                    _actionSuccess.value = "密码修改成功"
                } else {
                    _errorMessage.value = response.message
                }
            } catch (e: Exception) {
                Log.e(TAG, "修改密码异常", e)
                _errorMessage.value = when (e) {
                    is UnknownHostException, is ConnectException -> "无法连接服务器"
                    else -> "操作失败: ${e.message}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        _user.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearActionSuccess() {
        _actionSuccess.value = null
    }
}
