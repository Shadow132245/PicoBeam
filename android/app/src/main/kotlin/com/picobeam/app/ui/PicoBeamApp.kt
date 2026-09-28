package com.picobeam.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.picobeam.app.HubViewModel
import com.picobeam.app.R

private const val ShareRoute = "share"
private const val ReceiveRoute = "receive"

@Composable
fun PicoBeamApp(vm: HubViewModel = viewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == ShareRoute } == true,
                    onClick = {
                        navController.navigate(ShareRoute) {
                            popUpTo(navController.graph.id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.Share, contentDescription = null) },
                    label = { Text(stringResource(R.string.share_tab)) },
                )
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == ReceiveRoute } == true,
                    onClick = {
                        navController.navigate(ReceiveRoute) {
                            popUpTo(navController.graph.id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Filled.Send, contentDescription = null) },
                    label = { Text(stringResource(R.string.receive_tab)) },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ShareRoute,
            modifier = Modifier.padding(padding),
        ) {
            composable(ShareRoute) { ShareScreen(vm) }
            composable(ReceiveRoute) { ReceiveScreen(vm) }
        }
    }
}