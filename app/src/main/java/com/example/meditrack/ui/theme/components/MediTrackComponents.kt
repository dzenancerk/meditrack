package com.example.meditrack.ui.theme.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
@Composable
fun MediTrackTopBar(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(vertical = 16.dp, horizontal = 16.dp)
    ) {
        Text(
            text = "MediTrack",
            modifier = Modifier.fillMaxWidth(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun MediTrackTopBarWithBack(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Nazad",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = "MediTrack",
            modifier = Modifier.weight(1f),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
fun MediTrackBottomNavigationBar(
    onNavigateToHome: () -> Unit,
    onNavigateToNalazi: () -> Unit,
    onNavigateToParametri: () -> Unit,
    onNavigateToPodsjetnici: () -> Unit,
    onNavigateToProfil: () -> Unit,
    currentRoute: String = "",
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple("home", "Home", Icons.Default.Home),
        Triple("nalazi", "Nalazi", Icons.Default.Description),
        Triple("parametri", "Parametri", Icons.Default.BarChart),
        Triple("podsjetnici", "Podsjetnici", Icons.Default.Notifications),
        Triple("profil", "Profil", Icons.Default.Person)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val actions = listOf(
            onNavigateToHome,
            onNavigateToNalazi,
            onNavigateToParametri,
            onNavigateToPodsjetnici,
            onNavigateToProfil
        )

        items.forEachIndexed { index, (route, label, icon) ->
            val isActive = currentRoute == route
            Column(
                modifier = Modifier
                    .clickable { actions[index]() }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isActive)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onBackground
                )
                Text(
                    label,
                    color = if (isActive)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onBackground,
                    fontSize = 11.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun NetworkStatusBanner(
    modifier: Modifier = Modifier,
    viewModel: ConnectivityViewModel = hiltViewModel()
) {
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    if (!isOnline) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = "Nema internet konekcije. Podaci će se sinhronizovati automatski.",
                modifier = Modifier.padding(12.dp),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}