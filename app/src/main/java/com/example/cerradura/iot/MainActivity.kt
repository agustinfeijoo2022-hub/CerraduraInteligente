package com.example.cerradura.iot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    val navController = rememberNavController()
                    NavHost(navController, startDestination = "modo") {
                        composable("modo") {
                            ModoSeleccionScreen(
                                onModoCliente = { ip ->
                                    navController.navigate("login") {
                                        popUpTo("modo") { inclusive = true }
                                    }
                                },
                                onModoServidor = {
                                    navController.navigate("servidor") {
                                        popUpTo("modo") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("servidor") {
                            ServidorScreen(
                                onBack = {
                                    navController.navigate("modo") {
                                        popUpTo("servidor") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("monitoreo") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onGoToRegister = {
                                    navController.navigate("register")
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onRegisterSuccess = {
                                    navController.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                },
                                onGoToLogin = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("monitoreo") {
                            MonitoreoScreen(
                                onGoToControl = {
                                    navController.navigate("control")
                                }
                            )
                        }
                        composable("control") {
                            ControlScreen(
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}