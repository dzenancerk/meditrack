package com.example.meditrack.ui.theme.parametri

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.meditrack.ui.theme.components.MediTrackTopBarWithBack
import com.example.meditrack.ui.theme.components.NetworkStatusBanner
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnosParametraScreen(
    initialTab: Int = 0,
    viewModel: UnosParametraViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(initialTab) {
        viewModel.setInitialTab(initialTab)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MediTrackTopBarWithBack(onNavigateBack = onNavigateBack)
        NetworkStatusBanner()
        ScrollableTabRow(selectedTabIndex = viewModel.selectedTip) {
            viewModel.tabovi.forEachIndexed { index, naziv ->
                Tab(
                    selected = viewModel.selectedTip == index,
                    onClick = { viewModel.onTipChange(index) },
                    text = { Text(naziv, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(44.dp))

            if (viewModel.selectedTip == 0) {
                OutlinedTextField(
                    value = viewModel.sistolicki,
                    onValueChange = viewModel::onSistolickiChange,
                    label = { Text("Sistolički pritisak (mmHg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = viewModel.errorMessage != null
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Normalne vrijednosti: 90–120 mmHg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = viewModel.dijastolicki,
                    onValueChange = viewModel::onDijastolickiChange,
                    label = { Text("Dijastolički pritisak (mmHg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = viewModel.errorMessage != null
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Normalne vrijednosti: 60–80 mmHg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val tip = com.example.meditrack.data.model.ParametarTip.entries[viewModel.selectedTip - 1]
                OutlinedTextField(
                    value = viewModel.vrijednost,
                    onValueChange = viewModel::onVrijednostChange,
                    label = { Text("Vrijednost (${tip.jedinica})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = viewModel.errorMessage != null
                )
                if (tip != com.example.meditrack.data.model.ParametarTip.TJELESNA_TEZINA) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Normalne vrijednosti: ${tip.minNormalno} – ${tip.maxNormalno} ${tip.jedinica}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (viewModel.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = viewModel.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.onSave(onNavigateBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                enabled = !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Sačuvajte mjerenje")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}