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
                                    navController.navigate("login_servidor") {
                                        popUpTo("modo") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("pin") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onGoToRegister = {
                                    navController.navigate("register")
                                }
                            )
                        }
                        composable("login_servidor") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("servidor") {
                                        popUpTo("login_servidor") { inclusive = true }
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
                                    navController.navigate("modo") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                },
                                onGoToLogin = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("pin") {
                            PinScreen(
                                onPinCorrecto = {
                                    navController.navigate("monitoreo") {
                                        popUpTo("pin") { inclusive = true }
                                    }
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
                        composable("servidor") {
                            ServidorScreen(
                                onBack = {
                                    navController.navigate("modo") {
                                        popUpTo("servidor") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}