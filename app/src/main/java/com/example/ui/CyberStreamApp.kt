package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.catalog.CatalogScreen
import com.example.ui.home.HomeScreen
import com.example.ui.navigation.BottomNavScreens
import com.example.ui.navigation.Screen
import com.example.ui.profile.ProfileScreen
import com.example.ui.storage.StorageScreen
import com.example.ui.storage.AddSourceScreen
import com.example.ui.navigation.NavRoutes
import com.example.ui.scan.ScanProgressBanner
import com.example.ui.scan.ScanViewModel

@Composable
fun CyberStreamApp(scanViewModel: ScanViewModel = viewModel()) {
  val navController = rememberNavController()
  
  val scanStatus by scanViewModel.scanStatus.collectAsState()

  Scaffold(
    bottomBar = {
      val navBackStackEntry by navController.currentBackStackEntryAsState()
      val currentRoute = navBackStackEntry?.destination?.route
      if (BottomNavScreens.any { it.route == currentRoute }) {
        Column {
          ScanProgressBanner(scanStatus = scanStatus)
          BottomNavigationBar(navController = navController)
        }
      }
    }
  ) { innerPadding ->
    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
      NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.fillMaxSize()
      ) {
        composable(Screen.Home.route) { HomeScreen() }
        composable(Screen.Catalog.route) { CatalogScreen() }
        composable(Screen.Storage.route) { 
          StorageScreen(
            onAddClick = { navController.navigate(NavRoutes.AddSource) }
          )
        }
        composable(Screen.Profile.route) { ProfileScreen() }
        composable(NavRoutes.AddSource) {
          AddSourceScreen(onNavigateBack = { navController.popBackStack() })
        }
      }
    }
  }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination

  NavigationBar(
      modifier = Modifier.navigationBarsPadding() // Ensures it sits above system nav bar
  ) {
    BottomNavScreens.forEach { screen ->
      val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
      NavigationBarItem(
        icon = {
          Icon(
            imageVector = if (selected) screen.activeIcon else screen.inactiveIcon,
            contentDescription = screen.title
          )
        },
        label = { Text(screen.title) },
        selected = selected,
        onClick = {
          navController.navigate(screen.route) {
            popUpTo(navController.graph.findStartDestination().id) {
              saveState = true
            }
            launchSingleTop = true
            restoreState = true
          }
        }
      )
    }
  }
}
