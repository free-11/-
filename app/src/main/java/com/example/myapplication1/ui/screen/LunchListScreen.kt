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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication1.model.Lunch
import com.example.myapplication1.model.User
import com.example.myapplication1.viewmodel.LunchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LunchListScreen(
    user: User,
    lunchViewModel: LunchViewModel = viewModel()
) {
    val lunchList by lunchViewModel.lunchList.collectAsState()
    val isLoading by lunchViewModel.isLoading.collectAsState()
    val errorMessage by lunchViewModel.errorMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingLunch by remember { mutableStateOf<Lunch?>(null) }
    var deleteConfirmLunch by remember { mutableStateOf<Lunch?>(null) }

    LaunchedEffect(user.id) {
        user.id?.let { lunchViewModel.loadLunchList(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "菜品管理",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "添加", modifier = Modifier.size(32.dp))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && lunchList.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "加载中...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else if (lunchList.isEmpty()) {
                EmptyLunchState(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lunchList, key = { it.id ?: it.hashCode() }) { lunch ->
                        LunchItem(
                            lunch = lunch,
                            onEdit = { editingLunch = lunch },
                            onDelete = { deleteConfirmLunch = lunch }
                        )
                    }
                }
            }

            errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(20.dp),
                    action = {
                        TextButton(onClick = { lunchViewModel.clearError() }) {
                            Text("关闭", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Text(error, fontWeight = FontWeight.Medium)
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditLunchDialog(
            lunch = null,
            onDismiss = { showAddDialog = false },
            onSave = { newLunch ->
                user.id?.let { userId ->
                    lunchViewModel.addLunch(newLunch.copy(userId = userId))
                }
                showAddDialog = false
            }
        )
    }

    editingLunch?.let { lunch ->
        AddEditLunchDialog(
            lunch = lunch,
            onDismiss = { editingLunch = null },
            onSave = { updatedLunch ->
                lunch.id?.let { id ->
                    lunchViewModel.updateLunch(id, updatedLunch)
                }
                editingLunch = null
            }
        )
    }

    deleteConfirmLunch?.let { lunch ->
        AlertDialog(
            onDismissRequest = { deleteConfirmLunch = null },
            icon = {
                Icon(
                    Icons.Filled.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text("确认删除", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                Text(
                    "确定要删除「${lunch.name}」吗？此操作不可撤销。",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lunchId = lunch.id
                        val userId = user.id
                        if (lunchId != null && userId != null) {
                            lunchViewModel.deleteLunch(lunchId, userId)
                        }
                        deleteConfirmLunch = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("删除", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmLunch = null }) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun EmptyLunchState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("\uD83C\uDF7D\uFE0F", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "还没有餐品选项",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "点击右下角 + 按钮添加你喜欢的美食吧",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun LunchItem(
    lunch: Lunch,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val foodEmojiMap = mapOf(
        "麻辣烫" to "\uD83C\uDF72", "炸鸡" to "\uD83C\uDF57", "寿司" to "\uD83C\uDF63",
        "汉堡" to "\uD83C\uDF54", "酸菜鱼" to "\uD83D\uDC1F", "沙拉" to "\uD83E\uDD57"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    foodEmojiMap[lunch.name] ?: "\uD83C\uDF74",
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    lunch.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                lunch.description?.let { desc ->
                    Text(
                        desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                lunch.tags?.let { tags ->
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tags.split(",").take(3).forEach { tag ->
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    tag.trim(),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "编辑",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.DeleteOutline,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLunchDialog(
    lunch: Lunch?,
    onDismiss: () -> Unit,
    onSave: (Lunch) -> Unit
) {
    var name by remember { mutableStateOf(lunch?.name ?: "") }
    var description by remember { mutableStateOf(lunch?.description ?: "") }
    var tags by remember { mutableStateOf(lunch?.tags ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                if (lunch == null) "添加午餐" else "编辑午餐",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("餐品名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("标签（可选，逗号分隔）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("如：辣,川菜,便宜") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(Lunch(
                        id = lunch?.id,
                        name = name,
                        description = description.ifBlank { null },
                        tags = tags.ifBlank { null },
                        userId = lunch?.userId ?: 0
                    ))
                },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("保存", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            }
        }
    )
}
