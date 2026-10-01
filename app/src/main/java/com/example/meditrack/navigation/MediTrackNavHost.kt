package com.example.meditrack.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.meditrack.ui.theme.login.SignInScreen
import com.example.meditrack.ui.theme.signup.SignUpScreen
import com.example.meditrack.ui.theme.home.HomeScreen
import com.example.meditrack.ui.theme.profil.ProfilScreen
import com.example.meditrack.ui.theme.nalazi.NalaziScreen
import com.example.meditrack.ui.theme.parametri.ParametriScreen
import com.example.meditrack.ui.theme.podsjetnici.PodsjetniciScreen
import com.example.meditrack.ui.theme.kreiranjepodsjetnika.KreiranjePodsjetnikaScreen
import com.example.meditrack.ui.theme.nalazi.DodavanjeNalazaScreen
import com.example.meditrack.ui.theme.nalazi.NalazDetaljiScreen
import com.example.meditrack.ui.theme.parametri.UnosParametraScreen

@Composable
fun MediTrackNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.SignIn.route) {
            SignInScreen(
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateToSignIn = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.SignIn.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToNalazi = {
                    navController.navigate(Screen.Nalazi.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToParametri = {
                    navController.navigate(Screen.Parametri.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToPodsjetnici = {
                    navController.navigate(Screen.Podsjetnici.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToProfil = {
                    navController.navigate(Screen.Profil.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.Nalazi.route) {
            NalaziScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToParametri = {
                    navController.navigate(Screen.Parametri.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToPodsjetnici = {
                    navController.navigate(Screen.Podsjetnici.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToProfil = {
                    navController.navigate(Screen.Profil.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToDodavanje = {
                    navController.navigate(Screen.DodavanjeNalaza.route)
                },
                onNavigateToDetalji = { nalazId ->
                    navController.navigate(Screen.NalazDetalji.createRoute(nalazId))
                }
            )
        }
        composable(Screen.Parametri.route) {
            ParametriScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToNalazi = {
                    navController.navigate(Screen.Nalazi.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToPodsjetnici = {
                    navController.navigate(Screen.Podsjetnici.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToProfil = {
                    navController.navigate(Screen.Profil.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToUnos = { tabIndex ->
                    navController.navigate(Screen.UnosParametra.createRoute(tabIndex))
                }
            )
        }
        composable(Screen.Podsjetnici.route) {
            PodsjetniciScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToNalazi = {
                    navController.navigate(Screen.Nalazi.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToParametri = {
                    navController.navigate(Screen.Parametri.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToProfil = {
                    navController.navigate(Screen.Profil.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToKreiranje = {
                    navController.navigate(Screen.KreiranjePodsjetnika.route)
                },
                onNavigateToEdit = { podsjetnikId ->
                    navController.navigate(Screen.EditPodsjetnik.createRoute(podsjetnikId))
                }
            )
        }
        composable(Screen.KreiranjePodsjetnika.route) {
            KreiranjePodsjetnikaScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.EditPodsjetnik.route,
            arguments = listOf(navArgument("podsjetnikId") { defaultValue = "" })
        ) { backStackEntry ->
            val podsjetnikId = backStackEntry.arguments?.getString("podsjetnikId") ?: ""
            KreiranjePodsjetnikaScreen(
                podsjetnikId = podsjetnikId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Profil.route) {
            ProfilScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToNalazi = {
                    navController.navigate(Screen.Nalazi.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToParametri = {
                    navController.navigate(Screen.Parametri.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToPodsjetnici = {
                    navController.navigate(Screen.Podsjetnici.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.SignIn.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.UnosParametra.route,
            arguments = listOf(
                navArgument("tabIndex") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { backStackEntry ->
            val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 0
            UnosParametraScreen(
                initialTab = tabIndex,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.DodavanjeNalaza.route) {
            DodavanjeNalazaScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.NalazDetalji.route,
            arguments = listOf(navArgument("nalazId") { defaultValue = "" })
        ) { backStackEntry ->
            val nalazId = backStackEntry.arguments?.getString("nalazId") ?: ""
            NalazDetaljiScreen(
                nalazId = nalazId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(Screen.EditNalaz.createRoute(id))
                }
            )
        }
        composable(
            route = Screen.EditNalaz.route,
            arguments = listOf(navArgument("nalazId") { defaultValue = "" })
        ) { backStackEntry ->
            val nalazId = backStackEntry.arguments?.getString("nalazId") ?: ""
            DodavanjeNalazaScreen(
                nalazId = nalazId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}