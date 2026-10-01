package com.example.meditrack.ui.theme.podsjetnici

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.meditrack.data.model.Podsjetnik
import com.example.meditrack.ui.theme.MedicalBlue
import com.example.meditrack.ui.theme.WarningOrange
import com.example.meditrack.ui.theme.components.MediTrackBottomNavigationBar
import com.example.meditrack.ui.theme.components.MediTrackTopBar
import com.example.meditrack.ui.theme.components.NetworkStatusBanner

@Composable
fun PodsjetniciScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToNalazi: () -> Unit,
    onNavigateToParametri: () -> Unit,
    onNavigateToProfil: () -> Unit,
    onNavigateToKreiranje: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: PodsjetniciViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadPodsjetnici()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MediTrackTopBar()
        NetworkStatusBanner()
        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = viewModel.statusFilter == "sve",
                    onClick = { viewModel.onStatusFilterChange("sve") },
                    label = { Text("Sve") }
                )
                FilterChip(
                    selected = viewModel.statusFilter == "aktivno",
                    onClick = { viewModel.onStatusFilterChange("aktivno") },
                    label = { Text("Aktivno") }
                )
                FilterChip(
                    selected = viewModel.statusFilter == "proslo",
                    onClick = { viewModel.onStatusFilterChange("proslo") },
                    label = { Text("Prošlo") }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = viewModel.tipFilter == "sve",
                    onClick = { viewModel.onTipFilterChange("sve") },
                    label = { Text("Sve") }
                )
                FilterChip(
                    selected = viewModel.tipFilter == "lijek",
                    onClick = { viewModel.onTipFilterChange("lijek") },
                    label = { Text("Lijekovi") }
                )
                FilterChip(
                    selected = viewModel.tipFilter == "pregled",
                    onClick = { viewModel.onTipFilterChange("pregled") },
                    label = { Text("Pregledi") }
                )
            }
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (viewModel.podsjetnici.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Nema podsjetnika za odabrani filter",
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
                    items(viewModel.podsjetnici) { podsjetnik ->
                        PodsjetnikCard(
                            podsjetnik = podsjetnik,
                            onDelete = { viewModel.deletePodsjetnik(it) },
                            onEdit = { onNavigateToEdit(it.id) }
                        )
                    }
                }
            }

            FloatingActionButton(
                onClick = onNavigateToKreiranje,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novi podsjetnik")
            }
        }

        MediTrackBottomNavigationBar(
            onNavigateToHome = onNavigateToHome,
            onNavigateToNalazi = onNavigateToNalazi,
            onNavigateToParametri = onNavigateToParametri,
            onNavigateToPodsjetnici = {},
            onNavigateToProfil = onNavigateToProfil,
            currentRoute = "podsjetnici"
        )
    }
}

@Composable
fun PodsjetnikCard(
    podsjetnik: Podsjetnik,
    onDelete: (String) -> Unit,
    onEdit: (Podsjetnik) -> Unit
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val jePregled = podsjetnik.tip == "pregled"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (jePregled)
                MedicalBlue.copy(alpha = 0.08f)
            else
                WarningOrange.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = podsjetnik.naziv,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (jePregled) "Pregled" else "Lijek",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Datum: ${podsjetnik.datum}")
            Text("Vrijeme: ${podsjetnik.vrijeme}")
            if (podsjetnik.opis.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Napomena: ${podsjetnik.opis}", fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { onEdit(podsjetnik) }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Izmijenite",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Izbrišite",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Brisanje podsjetnika") },
            text = { Text("Da li ste sigurni da želite obrisati ovaj podsjetnik?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(podsjetnik.id)
                    showDeleteDialog = false
                }) {
                    Text("Da", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Ne")
                }
            }
        )
    }
}