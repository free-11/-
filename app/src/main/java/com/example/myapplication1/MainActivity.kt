package com.example.myapplication1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication1.ui.screen.*
import com.example.myapplication1.ui.theme.MyApplication1Theme
import com.example.myapplication1.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplication1Theme {
                AppMain()
            }
        }
    }
}

@Composable
fun AppMain() {
    val authViewModel: AuthViewModel = viewModel()
    val user by authViewModel.user.collectAsState()

    if (user != null) {
        val mainNavController = rememberNavController()
        MainScreenWithBottomNav(
            user = user!!,
            authViewModel = authViewModel,
            navController = mainNavController,
            onLogout = { authViewModel.logout() }
        )
    } else {
        val authNavController = rememberNavController()
        NavHost(navController = authNavController, startDestination = "login") {
            composable("login") {
                LoginScreen(authViewModel = authViewModel, onLoginSuccess = {}, onNavigateToRegister = { authNavController.navigate("register") })
            }
            composable("register") {
                RegisterScreen(authViewModel = authViewModel, onRegisterSuccess = {}, onNavigateBack = { authNavController.popBackStack() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithBottomNav(
    user: com.example.myapplication1.model.User,
    authViewModel: AuthViewModel,
    navController: NavHostController,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val navItems = listOf(
        BottomNavItem("抽奖", Icons.Default.Home, "spin"),
        BottomNavItem("广场", Icons.Default.Public, "foodSquare"),
        BottomNavItem("菜品", Icons.AutoMirrored.Filled.List, "lunchList"),
        BottomNavItem("AI助手", Icons.Default.SmartToy, "aiRecommend"),
        BottomNavItem("我的", Icons.Default.Person, "profile")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "spin",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("spin") { SpinScreen(user = user, onLogout = onLogout) }
            composable("foodSquare") { FoodSquareScreen(user = user) }
            composable("lunchList") { LunchListScreen(user = user) }
            composable("aiRecommend") { AiRecommendScreen(user = user, onBack = { navController.popBackStack() }) }
            composable("profile") {
                ProfileScreen(
                    user = user,
                    authViewModel = authViewModel,
                    onLogout = onLogout,
                    onNavigateToStats = { navController.navigate("stats") },
                    onNavigateToHistory = { navController.navigate("history") },
                    onNavigateToAi = { navController.navigate("aiRecommend") }
                )
            }
            composable("stats") { StatsScreen(user = user, onBack = { navController.popBackStack() }) }
            composable("history") { HistoryScreen(user = user, onBack = { navController.popBackStack() }) }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)
