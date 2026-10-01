package com.example.meditrack.ui.theme.nalazi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.meditrack.ui.theme.components.MediTrackTopBarWithBack
import com.example.meditrack.ui.theme.components.NetworkStatusBanner
import com.example.meditrack.ui.theme.shared.FullScreenImageViewer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
@Composable
fun NalazDetaljiScreen(
    nalazId: String,
    viewModel: NalazDetaljiViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    LaunchedEffect(nalazId) { viewModel.loadNalaz(nalazId) }

    val nalaz = viewModel.nalaz
    var otvorenaSlikaIndex by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MediTrackTopBarWithBack(onNavigateBack = onNavigateBack)
        NetworkStatusBanner()
        when {
            viewModel.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            viewModel.errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        viewModel.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            nalaz != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        nalaz.naziv,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        nalaz.tip.replaceFirstChar { it.uppercase() },
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Datum: ${nalaz.datum}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (nalaz.opis.isNotBlank()) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Napomena",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(nalaz.opis, fontSize = 15.sp)
                            }
                        }
                    }
                    if (nalaz.prepoznatiTekst.isNotBlank()) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Prepoznati tekst (OCR)",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(nalaz.prepoznatiTekst, fontSize = 15.sp)
                            }
                        }
                    }
                    if (nalaz.fileUrls.isNotEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Slike nalaza (${nalaz.fileUrls.size})",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                SlikeGrid(
                                    fileUrls = nalaz.fileUrls,
                                    onSlikaClick = { index -> otvorenaSlikaIndex = index }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Dodirnite sliku za uvećan prikaz",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onNavigateToEdit(nalaz.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Uredite nalaz")
                    }
                }
            }
        }
        if (otvorenaSlikaIndex != null && nalaz != null) {
            FullScreenImageViewer(
                slike = nalaz.fileUrls,
                initialIndex = otvorenaSlikaIndex!!,
                onDismiss = { otvorenaSlikaIndex = null }
            )
        }
    }
}

@Composable
private fun SlikeGrid(
    fileUrls: List<String>,
    onSlikaClick: (Int) -> Unit
) {
    val prikazano = minOf(4, fileUrls.size)
    val preostalo = fileUrls.size - 4
    val tileSize = 140.dp
    val redovi = (0 until prikazano).chunked(2)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        redovi.forEach { red ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                red.forEach { index ->
                    Box(
                        modifier = Modifier
                            .size(tileSize)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSlikaClick(index) }
                    ) {
                        AsyncImage(
                            model = fileUrls[index],
                            contentDescription = "Slika nalaza ${index + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        if (index == 3 && preostalo > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "+$preostalo",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}