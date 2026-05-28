package com.example.myapplication1.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication1.model.ForumComment
import com.example.myapplication1.model.ForumPost
import com.example.myapplication1.model.Lunch
import com.example.myapplication1.model.User
import com.example.myapplication1.viewmodel.LunchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSquareScreen(
    user: User,
    lunchViewModel: LunchViewModel = viewModel()
) {
    val forumPosts by lunchViewModel.forumPosts.collectAsState()
    val lunchList by lunchViewModel.lunchList.collectAsState()
    val isLoading by lunchViewModel.isLoading.collectAsState()
    val comments by lunchViewModel.comments.collectAsState()

    var showPostDialog by remember { mutableStateOf(false) }

    LaunchedEffect(user.id) {
        user.id?.let { lunchViewModel.loadLunchList(it) }
        lunchViewModel.loadForumPosts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("\uD83C\uDFD9\uFE0F", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("美食广场", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showPostDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "发帖", modifier = Modifier.size(32.dp))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && forumPosts.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("加载中...", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
            } else if (forumPosts.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("\uD83C\uDFD9\uFE0F", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("美食广场", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("还没有人发帖，快来抢沙发吧！", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(forumPosts, key = { it.id ?: it.hashCode() }) { post ->
                        ForumPostItem(
                            post = post,
                            currentUserId = user.id,
                            comments = comments[post.id] ?: emptyList(),
                            onLike = { post.id?.let { lunchViewModel.likePost(it, user.id ?: return@let) } },
                            onDelete = { post.id?.let { lunchViewModel.deleteForumPost(it, user.id ?: return@let) } },
                            onToggleComments = { post.id?.let { lunchViewModel.loadComments(it) } },
                            onSendComment = { text ->
                                post.id?.let { postId ->
                                    lunchViewModel.addComment(
                                        postId = postId,
                                        userId = user.id ?: return@let,
                                        nickname = user.nickname ?: user.email.substringBefore("@"),
                                        content = text
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showPostDialog) {
        PostDialog(
            user = user,
            lunchList = lunchList,
            onDismiss = { showPostDialog = false },
            onPost = { content, dishName ->
                lunchViewModel.createForumPost(
                    ForumPost(
                        userId = user.id,
                        nickname = user.nickname ?: user.email.substringBefore("@"),
                        content = content,
                        dishName = dishName
                    )
                )
                showPostDialog = false
            }
        )
    }
}

@Composable
fun ForumPostItem(
    post: ForumPost,
    currentUserId: Long?,
    comments: List<ForumComment>,
    onLike: () -> Unit,
    onDelete: () -> Unit,
    onToggleComments: () -> Unit,
    onSendComment: (String) -> Unit
) {
    val foodEmojiMap = mapOf(
        "麻辣烫" to "\uD83C\uDF72", "炸鸡" to "\uD83C\uDF57", "寿司" to "\uD83C\uDF63",
        "汉堡" to "\uD83C\uDF54", "酸菜鱼" to "\uD83D\uDC1F", "沙拉" to "\uD83E\uDD57"
    )

    val time = post.createdAt?.let {
        if (it.length >= 16) it.substring(5, 16).replace("T", " ") else it
    } ?: ""

    var showComments by remember { mutableStateOf(false) }
    var commentInput by remember { mutableStateOf("") }

    val isLiked = currentUserId != null && post.likedUserIds?.split(",")?.contains(currentUserId.toString()) == true

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        (post.nickname ?: "?").first().uppercase(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        post.nickname ?: "匿名用户",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (time.isNotEmpty()) {
                        Text(
                            time,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!post.dishName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            foodEmojiMap[post.dishName] ?: "\uD83C\uDF74",
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "推荐：${post.dishName}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                post.content ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onLike) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isLiked) "取消点赞" else "点赞",
                        tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "${post.likes ?: 0}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = {
                    showComments = !showComments
                    if (showComments) onToggleComments()
                }) {
                    Icon(
                        imageVector = if (showComments) Icons.Filled.Chat else Icons.Filled.ChatBubbleOutline,
                        contentDescription = "评论",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "${comments.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                if (currentUserId != null && post.userId == currentUserId) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (showComments) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))

                if (comments.isEmpty()) {
                    Text(
                        "暂无评论",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    comments.forEach { comment ->
                        val commentTime = comment.createdAt?.let {
                            if (it.length >= 16) it.substring(5, 16).replace("T", " ") else it
                        } ?: ""
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    (comment.nickname ?: "?").first().uppercase(),
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        comment.nickname ?: "匿名用户",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        commentTime,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    comment.content ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        placeholder = { Text("说点什么...", style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (commentInput.isNotBlank()) {
                                onSendComment(commentInput.trim())
                                commentInput = ""
                            }
                        },
                        enabled = commentInput.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "发送",
                            tint = if (commentInput.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDialog(
    user: User,
    lunchList: List<Lunch>,
    onDismiss: () -> Unit,
    onPost: (String, String?) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var selectedDish by remember { mutableStateOf<String?>(null) }
    var showDishPicker by remember { mutableStateOf(false) }

    val foodEmojiMap = mapOf(
        "麻辣烫" to "\uD83C\uDF72", "炸鸡" to "\uD83C\uDF57", "寿司" to "\uD83C\uDF63",
        "汉堡" to "\uD83C\uDF54", "酸菜鱼" to "\uD83D\uDC1F", "沙拉" to "\uD83E\uDD57"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("发帖", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (!showDishPicker) {
                    if (selectedDish != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(foodEmojiMap[selectedDish] ?: "\uD83C\uDF74", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("推荐：$selectedDish", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { selectedDish = null }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Filled.Close, contentDescription = "取消", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    } else if (lunchList.isNotEmpty()) {
                        TextButton(onClick = { showDishPicker = true }) {
                            Icon(Icons.Filled.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("推荐菜品", color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("想和大家分享什么？") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        lunchList.take(8).forEach { lunch ->
                            Surface(
                                onClick = {
                                    selectedDish = lunch.name
                                    showDishPicker = false
                                },
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(foodEmojiMap[lunch.name] ?: "\uD83C\uDF74", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(lunch.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                        TextButton(onClick = { showDishPicker = false }) {
                            Text("返回编辑", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onPost(content.trim(), selectedDish) },
                enabled = content.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("发布", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant) } }
    )
}
