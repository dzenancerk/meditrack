package com.example.meditrack.ui.theme.nalazi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.meditrack.data.model.Nalaz
import com.example.meditrack.ui.theme.components.MediTrackBottomNavigationBar
import com.example.meditrack.ui.theme.components.MediTrackTopBar
import com.example.meditrack.ui.theme.WarningOrange
import com.example.meditrack.ui.theme.components.NetworkStatusBanner
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

@Composable
fun NalaziScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToParametri: () -> Unit,
    onNavigateToPodsjetnici: () -> Unit,
    onNavigateToProfil: () -> Unit,
    onNavigateToDodavanje: () -> Unit,
    onNavigateToDetalji: (String) -> Unit,
    viewModel: NalaziViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.loadNalazi() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MediTrackTopBar()
        NetworkStatusBanner()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tipovi = listOf(
                "sve" to "Sve",
                "laboratorijski" to "Laboratorijski",
                "rendgen" to "Rendgen",
                "ultrazvuk" to "Ultrazvuk",
                "ct" to "CT",
                "mri" to "MRI",
                "kardiološki" to "Kardiološki",
                "ostalo" to "Ostalo"
            )
            tipovi.forEach { (vrijednost, label) ->
                FilterChip(
                    selected = viewModel.tipFilter == vrijednost,
                    onClick = { viewModel.onTipFilterChange(vrijednost) },
                    label = { Text(label) }
                )
            }
        }
        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (viewModel.nalazi.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (viewModel.tipFilter == "sve") "Nemate sačuvanih nalaza"
                        else "Nema nalaza za odabrani filter",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.nalazi) { nalaz ->
                        NalazCard(
                            nalaz = nalaz,
                            onDelete = { viewModel.deleteNalaz(nalaz) },
                            onClick = { onNavigateToDetalji(nalaz.id) }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = onNavigateToDodavanje,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj nalaz")
            }
        }

        MediTrackBottomNavigationBar(
            onNavigateToHome = onNavigateToHome,
            onNavigateToNalazi = {},
            onNavigateToParametri = onNavigateToParametri,
            onNavigateToPodsjetnici = onNavigateToPodsjetnici,
            onNavigateToProfil = onNavigateToProfil,
            currentRoute = "nalazi"
        )
    }
}

@Composable
fun NalazCard(
    nalaz: Nalaz,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = WarningOrange.copy(alpha = 0.08f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    nalaz.naziv,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    nalaz.tip.replaceFirstChar { it.uppercase() },
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    nalaz.datum,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (nalaz.fileUrls.isNotEmpty()) {
                    Text(
                        "📎 ${nalaz.fileUrls.size} ${if (nalaz.fileUrls.size == 1) "slika" else "slike"} priloženo",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Obriši",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Brisanje nalaza") },
            text = { Text("Da li ste sigurni da želite obrisati ovaj nalaz?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) { Text("Da", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Ne") }
            }
        )
    }
}