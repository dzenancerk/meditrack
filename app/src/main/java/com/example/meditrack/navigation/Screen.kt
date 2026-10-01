package com.example.meditrack.navigation

sealed class Screen(val route: String) {
    object SignIn : Screen("signin")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object Profil : Screen("profil")
    object Nalazi : Screen("nalazi")
    object Parametri : Screen("parametri")
    object Podsjetnici : Screen("podsjetnici")
    object KreiranjePodsjetnika : Screen("kreiranjePodsjetnika")
    object EditPodsjetnik : Screen("editPodsjetnik/{podsjetnikId}") {
        fun createRoute(podsjetnikId: String) = "editPodsjetnik/$podsjetnikId"
    }
    object DodavanjeNalaza : Screen("dodavanjeNalaza")
    object NalazDetalji : Screen("nalazDetalji/{nalazId}") {
        fun createRoute(nalazId: String) = "nalazDetalji/$nalazId"
    }
    object UnosParametra : Screen("unosParametra/{tabIndex}") {
        fun createRoute(tabIndex: Int) = "unosParametra/$tabIndex"
    }
    object EditNalaz : Screen("editNalaz/{nalazId}") {
        fun createRoute(nalazId: String) = "editNalaz/$nalazId"
    }
}