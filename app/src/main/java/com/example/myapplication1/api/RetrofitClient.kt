package com.example.myapplication1.api

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://10.119.218.208:8080/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun streamAiRecommend(
        userId: Long,
        message: String,
        onChunk: (String) -> Unit,
        onComplete: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            try {
                val json = """{"userId":"$userId","message":"${message.replace("\"", "\\\"")}"}"""
                val request = Request.Builder()
                    .url("${BASE_URL}api/ai/recommend/stream")
                    .header("Accept", "text/event-stream")
                    .post(json.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    onError("HTTP ${response.code}: ${response.body?.string()}")
                    return@Thread
                }

                val body = response.body ?: run { onError("空响应"); return@Thread }
                val source = body.source()
                var currentEvent = ""
                var fullText = ""

                source.use {
                    while (!source.exhausted()) {
                        val line = it.readUtf8Line() ?: continue
                        val trimmed = line.trim()
                        if (trimmed.isEmpty()) continue

                        if (trimmed.startsWith("event:")) {
                            currentEvent = trimmed.substring(6).trim()
                        } else if (trimmed.startsWith("data:")) {
                            val data = trimmed.substring(5).trim()
                            when (currentEvent) {
                                "chunk" -> {
                                    fullText += data
                                    onChunk(data)
                                }
                                "done" -> onComplete(data)
                                "error" -> onError(data)
                            }
                            currentEvent = ""
                        }
                    }
                }

            } catch (e: Exception) {
                onError(e.message ?: "未知错误")
            }
        }.start()
    }
}
