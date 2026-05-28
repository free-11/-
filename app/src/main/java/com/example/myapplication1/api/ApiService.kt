package com.example.myapplication1.api

import com.example.myapplication1.model.ApiResponse
import com.example.myapplication1.model.ForumComment
import com.example.myapplication1.model.ForumPost
import com.example.myapplication1.model.Lunch
import com.example.myapplication1.model.LunchHistory
import com.example.myapplication1.model.StatsData
import com.example.myapplication1.model.User
import retrofit2.http.*

interface ApiService {
    @POST("/api/user/register")
    suspend fun register(@Body user: Map<String, String>): ApiResponse<User>

    @POST("/api/user/login")
    suspend fun login(@Body user: Map<String, String>): ApiResponse<User>

    @PUT("/api/user/update-nickname")
    suspend fun updateNickname(@Body body: Map<String, String>): ApiResponse<User>

    @PUT("/api/user/change-password")
    suspend fun changePassword(@Body body: Map<String, String>): ApiResponse<Unit>

    @GET("/api/lunch/list/{userId}")
    suspend fun getLunchList(@Path("userId") userId: Long): ApiResponse<List<Lunch>>

    @GET("/api/lunch/spin/{userId}")
    suspend fun spinLunch(@Path("userId") userId: Long, @Query("tags") tags: String? = null): ApiResponse<Lunch>

    @POST("/api/lunch/add")
    suspend fun addLunch(@Body lunch: Lunch): ApiResponse<Lunch>

    @PUT("/api/lunch/update/{id}")
    suspend fun updateLunch(@Path("id") id: Long, @Body lunch: Lunch): ApiResponse<Lunch>

    @DELETE("/api/lunch/delete/{id}")
    suspend fun deleteLunch(@Path("id") id: Long): ApiResponse<Unit>

    @GET("/api/history/list/{userId}")
    suspend fun getHistoryList(@Path("userId") userId: Long): ApiResponse<List<LunchHistory>>

    @GET("/api/stats/{userId}")
    suspend fun getStats(@Path("userId") userId: Long): ApiResponse<StatsData>

    @GET("/api/forum/list")
    suspend fun getForumPosts(): ApiResponse<List<ForumPost>>

    @POST("/api/forum/create")
    suspend fun createForumPost(@Body post: ForumPost): ApiResponse<ForumPost>

    @POST("/api/forum/like/{postId}")
    suspend fun likePost(@Path("postId") postId: Long, @Body body: Map<String, Long>): ApiResponse<ForumPost>

    @DELETE("/api/forum/delete/{postId}")
    suspend fun deleteForumPost(@Path("postId") postId: Long, @Query("userId") userId: Long): ApiResponse<Unit>

    @GET("/api/forum/comments/{postId}")
    suspend fun getComments(@Path("postId") postId: Long): ApiResponse<List<ForumComment>>

    @POST("/api/forum/comment/add")
    suspend fun addComment(@Body comment: ForumComment): ApiResponse<ForumComment>

    @POST("/api/ai/recommend")
    suspend fun aiRecommend(@Body body: Map<String, String>): ApiResponse<String>
}
