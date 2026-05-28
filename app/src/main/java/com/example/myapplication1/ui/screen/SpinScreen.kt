package com.example.myapplication1.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication1.model.User
import com.example.myapplication1.viewmodel.LunchViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpinScreen(
    user: User,
    onLogout: () -> Unit,
    lunchViewModel: LunchViewModel = viewModel()
) {
    val lunchList by lunchViewModel.lunchList.collectAsState()
    val selectedLunch by lunchViewModel.selectedLunch.collectAsState()
    val isSpinning by lunchViewModel.isSpinning.collectAsState()
    val errorMessage by lunchViewModel.errorMessage.collectAsState()

    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val bounce = remember { Animatable(0f) }
    var emojiIndex by remember { mutableIntStateOf(0) }
    var selectedTags by remember { mutableStateOf<Set<String>>(emptySet()) }

    val foodEmojis = listOf("\uD83C\uDF5C", "\uD83C\uDF57", "\uD83C\uDF63", "\uD83C\uDF54", "\uD83D\uDC1F", "\uD83E\uDD57")

    val allTags = remember(lunchList) {
        lunchList.flatMap { lunch ->
            (lunch.tags ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }.distinct().sorted()
    }

    LaunchedEffect(user.id) {
        user.id?.let { lunchViewModel.loadLunchList(it) }
    }

    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            rotation.snapTo(0f)
            scale.snapTo(1f)
            bounce.snapTo(0f)
            rotation.animateTo(1800f, animationSpec = tween(2000, easing = FastOutSlowInEasing))
            bounce.animateTo(-30f, animationSpec = spring(stiffness = Spring.StiffnessHigh, dampingRatio = Spring.DampingRatioMediumBouncy))
            bounce.animateTo(0f, animationSpec = tween(300))
        }
    }

    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            while (isSpinning) {
                delay(120)
                emojiIndex = (emojiIndex + 1) % foodEmojis.size
            }
        } else {
            selectedLunch?.let { lunch ->
                emojiIndex = when (lunch.name) {
                    "麻辣烫" -> 0; "炸鸡" -> 1; "寿司" -> 2; "汉堡" -> 3; "酸菜鱼" -> 4; "沙拉" -> 5; else -> 0
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 36.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("吃了吗", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("你好，${user.nickname ?: user.email.substringBefore("@")}", style = MaterialTheme.typography.titleSmall, color = Color.White.copy(alpha = 0.85f))
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "退出", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(top = 80.dp, bottom = 16.dp),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f).wrapContentHeight(Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (allTags.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                FilterChip(
                                    selected = selectedTags.isEmpty(),
                                    onClick = { selectedTags = emptySet() },
                                    label = { Text("全部") },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                allTags.forEach { tag ->
                                    FilterChip(
                                        selected = tag in selectedTags,
                                        onClick = { selectedTags = if (tag in selectedTags) selectedTags - tag else selectedTags + tag },
                                        label = { Text(tag) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier.size(140.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer).rotate(rotation.value).scale(scale.value).offset(y = bounce.value.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(foodEmojis[emojiIndex], fontSize = 64.sp)
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        AnimatedVisibility(
                            visible = selectedLunch != null && !isSpinning,
                            enter = fadeIn() + scaleIn(initialScale = 0.85f),
                            exit = fadeOut() + scaleOut()
                        ) {
                            selectedLunch?.let { lunch ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("今天吃", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(lunch.name, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, letterSpacing = (-0.5).sp)
                                    lunch.description?.let { desc ->
                                        Text(desc, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
                                    }
                                    lunch.tags?.let { tags ->
                                        Row(modifier = Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            tags.split(",").forEach { tag ->
                                                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(20.dp)) {
                                                    Text(tag.trim(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (!isSpinning && selectedLunch == null) {
                            Text("\uD83C\uDF72", fontSize = 52.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("点击下方按钮\n随机抽取今天的午餐", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 28.sp)
                        }

                        if (isSpinning) {
                            Text("正在为你抽取...", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Button(
                        onClick = {
                            val tags = if (selectedTags.isEmpty()) null else selectedTags.joinToString(",")
                            user.id?.let { lunchViewModel.spin(it, tags) }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(20.dp),
                        enabled = !isSpinning && user.id != null,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isSpinning) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.5.dp)
                        } else {
                            Text(if (selectedLunch != null) "再抽一次" else "开始抽奖", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }

            errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp),
                    action = { TextButton(onClick = { lunchViewModel.clearError() }) { Text("关闭", color = Color.White, fontWeight = FontWeight.Bold) } },
                    containerColor = MaterialTheme.colorScheme.error
                ) { Text(error, color = Color.White) }
            }
        }
    }
}
