package com.example.meditrack.ui.theme.nalazi

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.meditrack.ui.theme.components.MediTrackTopBarWithBack
import com.example.meditrack.ui.theme.kreiranjepodsjetnika.DatumPicker
import com.example.meditrack.ui.theme.components.NetworkStatusBanner
import android.app.Activity
import androidx.activity.result.IntentSenderRequest
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import androidx.compose.ui.platform.LocalContext
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.lazy.LazyRow
import com.example.meditrack.data.model.Nalaz
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import com.example.meditrack.data.model.NalazSlika
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipNalazaDropdown(
    selectedTip: String,
    onTipChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val tipovi = listOf(
        "laboratorijski" to "Laboratorijski nalaz",
        "rendgen" to "Rendgen",
        "ultrazvuk" to "Ultrazvuk",
        "ct" to "CT",
        "mri" to "MRI",
        "kardiološki" to "Kardiološki",
        "ostalo" to "Ostalo"
    )
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = tipovi.firstOrNull { it.first == selectedTip }?.second ?: selectedTip,
            onValueChange = {},
            readOnly = true,
            label = { Text("Tip nalaza") },
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
fun DodavanjeNalazaScreen(
    nalazId: String? = null,
    viewModel: DodavanjeNalazaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(nalazId) {
        if (!nalazId.isNullOrBlank()) viewModel.ucitajNalazZaEdit(nalazId)
    }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris: List<Uri> ->
        viewModel.onImagesSelected(uris)
    }
    val context = LocalContext.current
    val activity = context as? Activity
    var showImageSourceDialog by remember { mutableStateOf(false) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scanResult = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            val pageUris = scanResult?.pages?.map { it.imageUri } ?: emptyList()
            if (pageUris.isNotEmpty()) {
                viewModel.onImagesSelected(pageUris)
            } else {
                viewModel.onScannerError("Nije moguće dobiti skeniranu sliku")
            }
        }
    }

    fun launchScanner() {
        val trenutnaActivity = activity ?: return
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(10)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
        GmsDocumentScanning.getClient(options)
            .getStartScanIntent(trenutnaActivity)
            .addOnSuccessListener { intentSender ->
                scannerLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { e ->
                viewModel.onScannerError(e.message ?: "Nepoznata greška")
            }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MediTrackTopBarWithBack(onNavigateBack = onNavigateBack)
        NetworkStatusBanner()
        if (viewModel.isLoadingNalaz) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.naziv,
                onValueChange = viewModel::onNazivChange,
                label = { Text("Naziv nalaza") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = viewModel.formErrorMessage != null
            )
            TipNalazaDropdown(
                selectedTip = viewModel.selectedTip,
                onTipChange = viewModel::onTipChange
            )
            DatumPicker(
                selectedDatum = viewModel.selectedDatum,
                onDatumChange = viewModel::onDatumChange,
                dozvoliProslost = true
            )
            OutlinedTextField(
                value = viewModel.opis,
                onValueChange = viewModel::onOpisChange,
                label = { Text("Napomena (opciono)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            OutlinedTextField(
                value = viewModel.prepoznatiTekst,
                onValueChange = viewModel::onPrepoznatiTekstChange,
                label = { Text("Prepoznati tekst sa slike (OCR)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Text(
                "Slike nalaza (${viewModel.slike.size}/${Nalaz.MAX_SLIKA})",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.slike.size) { index ->
                    val slika = viewModel.slike[index]
                    val model = when (slika) {
                        is NalazSlika.Nova -> slika.uri
                        is NalazSlika.Postojeca -> slika.url
                    }
                    Box(modifier = Modifier.size(100.dp)) {
                        AsyncImage(
                            model = model,
                            contentDescription = "Slika nalaza ${index + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.onSlikaClick(index) }
                                .then(
                                    if (viewModel.selectedSlikaIndex == index)
                                        Modifier.border(
                                            width = 3.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    else Modifier
                                ),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { viewModel.removeSlika(index) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Ukloni sliku",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                item {
                    OutlinedCard(
                        modifier = Modifier.size(100.dp),
                        onClick = { showImageSourceDialog = true }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = "Dodaj sliku")
                        }
                    }
                }
            }
            if (viewModel.selectedSlikaIndex != null) {
                OutlinedButton(
                    onClick = { viewModel.prepoznajTekstSaSlike() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isRecognizingText
                ) {
                    if (viewModel.isRecognizingText) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Prepoznajem tekst...")
                    } else {
                        Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Prepoznaj tekst sa odabrane slike (OCR)")
                    }
                }
                if (viewModel.ocrErrorMessage != null) {
                    Text(
                        text = viewModel.ocrErrorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Text(
                    "Napomena: prepoznavanje najbolje radi na jasno odštampanom tekstu (npr. laboratorijski nalazi). Rukom pisani nalazi mogu biti manje precizno prepoznati.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (viewModel.formErrorMessage != null) {
                Text(
                    text = viewModel.formErrorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Button(
                onClick = { viewModel.saveNalaz(onNavigateBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 30.dp),
                enabled = !viewModel.isUploading
            ) {
                if (viewModel.isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (viewModel.isEditMode) "Sačuvajte izmjene" else "Sačuvajte nalaz")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (showImageSourceDialog) {
                AlertDialog(
                    onDismissRequest = { showImageSourceDialog = false },
                    title = { Text("Dodaj sliku nalaza") },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Odaberite način dodavanja slike nalaza.")
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = {
                                    showImageSourceDialog = false
                                    launchScanner()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Skeniraj dokument")
                            }
                            OutlinedButton(
                                onClick = {
                                    showImageSourceDialog = false
                                    imagePickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Iz galerije")
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showImageSourceDialog = false }) {
                            Text("Otkaži")
                        }
                    }
                )
            }
        }
    }
}