package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
  val route: String,
  val title: String,
  val activeIcon: ImageVector,
  val inactiveIcon: ImageVector
) {
  object Home : Screen("home", "首页", Icons.Filled.Home, Icons.Outlined.Home)
  object Catalog : Screen("catalog", "片库", Icons.Filled.Movie, Icons.Outlined.Movie)
  object Storage : Screen("storage", "资源库", Icons.Filled.Storage, Icons.Outlined.Storage)
  object Profile : Screen("profile", "我的", Icons.Filled.Person, Icons.Outlined.Person)
}

object NavRoutes {
  const val AddSource = "add_source"
}

val BottomNavScreens = listOf(Screen.Home, Screen.Catalog, Screen.Storage, Screen.Profile)
