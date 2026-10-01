package com.example.meditrack.ui.theme.kreiranjepodsjetnika

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.meditrack.ui.theme.components.MediTrackTopBarWithBack
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.ui.window.Dialog
import com.example.meditrack.ui.theme.components.NetworkStatusBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KreiranjePodsjetnikaScreen(
    podsjetnikId: String? = null,
    viewModel: KreiranjePodsjetnikaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(podsjetnikId) {
        if (!podsjetnikId.isNullOrBlank()) viewModel.ucitajPodsjetnikZaEdit(podsjetnikId)
    }
    if (viewModel.isLoadingPodsjetnik) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MediTrackTopBarWithBack(onNavigateBack = onNavigateBack)
        NetworkStatusBanner()

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = viewModel.naziv,
                onValueChange = viewModel::onNazivChange,
                label = { Text("Naziv (npr. Aspirin, Kardiolog)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = viewModel.errorMessage != null
            )
            Spacer(modifier = Modifier.height(16.dp))

            TipDropdown(
                selectedTip = viewModel.selectedTip,
                onTipChange = viewModel::onTipChange
            )
            Spacer(modifier = Modifier.height(16.dp))

            DatumPicker(
                selectedDatum = viewModel.selectedDatum,
                onDatumChange = viewModel::onDatumChange
            )
            Spacer(modifier = Modifier.height(16.dp))

            VrijemePicker(
                selectedVrijeme = viewModel.selectedVrijeme,
                onVrijemeChange = viewModel::onVrijemeChange
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = viewModel.opis,
                onValueChange = viewModel::onOpisChange,
                label = { Text("Napomena (opciono)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

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
                    Text(if (viewModel.isEditMode) "Sačuvajte izmjene" else "Kreirajte podsjetnik")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipDropdown(
    selectedTip: String,
    onTipChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val tipovi = listOf("lijek" to "Lijek", "pregled" to "Pregled")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = tipovi.first { it.first == selectedTip }.second,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tip podsjetnika") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            tipovi.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onTipChange(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatumPicker(
    selectedDatum: String,
    onDatumChange: (String) -> Unit,
    dozvoliProslost: Boolean = false
) {
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val selected = dateFormat.format(Date(utcTimeMillis))
                val today = dateFormat.format(Date())
                return if (dozvoliProslost) {
                    selected <= today
                } else {
                    selected >= today
                }
            }
        }
    )
    OutlinedTextField(
        value = selectedDatum,
        onValueChange = {},
        readOnly = true,
        label = { Text("Datum") },
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Izaberi datum")
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                            .format(Date(millis))
                        onDatumChange(date)
                    }
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Otkaži") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VrijemePicker(
    selectedVrijeme: String,
    onVrijemeChange: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val calendar = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = true
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
    ) {
        OutlinedTextField(
            value = selectedVrijeme,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text("Vrijeme") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(state = timePickerState)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Otkaži")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            val vrijeme = String.format(
                                "%02d:%02d",
                                timePickerState.hour,
                                timePickerState.minute
                            )
                            onVrijemeChange(vrijeme)
                            showDialog = false
                        }) {
                            Text("OK")
                        }
                    }
                }
            }
        }
    }
}