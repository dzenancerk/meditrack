package com.example.meditrack.ui.theme.parametri

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.meditrack.data.model.KrvniPritisak
import com.example.meditrack.data.model.Parametar
import com.example.meditrack.data.model.ParametarTip
import com.example.meditrack.ui.theme.components.MediTrackBottomNavigationBar
import com.example.meditrack.ui.theme.components.MediTrackTopBar
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalBox
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.continuous
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import android.text.Layout
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.example.meditrack.ui.theme.components.NetworkStatusBanner
import com.example.meditrack.utils.DateTimeUtils
@Composable
fun ParametriScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToNalazi: () -> Unit,
    onNavigateToPodsjetnici: () -> Unit,
    onNavigateToProfil: () -> Unit,
    onNavigateToUnos: (Int) -> Unit,
    viewModel: ParametriViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.loadData() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MediTrackTopBar()
        NetworkStatusBanner()

        ScrollableTabRow(selectedTabIndex = viewModel.selectedTab) {
            viewModel.tabovi.forEachIndexed { index, naziv ->
                Tab(
                    selected = viewModel.selectedTab == index,
                    onClick = { viewModel.onTabChange(index) },
                    text = { Text(naziv, fontSize = 12.sp) }
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
            } else {
                if (viewModel.selectedTab == 0) {
                    KrvniPritisakContent(
                        lista = viewModel.krvniPritisak,
                        isAbnormalan = viewModel::isKrvniPritisakAbnormalan,
                        onDelete = viewModel::deleteKrvniPritisak
                    )
                } else {
                    val tip = ParametarTip.entries[viewModel.selectedTab - 1]
                    ParametarContent(
                        lista = viewModel.parametri,
                        tip = tip,
                        isAbnormalan = viewModel::isAbnormalan,
                        onDelete = viewModel::deleteParametar
                    )
                }
            }
            FloatingActionButton(
                onClick = { onNavigateToUnos(viewModel.selectedTab) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novi unos")
            }
        }
        MediTrackBottomNavigationBar(
            onNavigateToHome = onNavigateToHome,
            onNavigateToNalazi = onNavigateToNalazi,
            onNavigateToParametri = {},
            onNavigateToPodsjetnici = onNavigateToPodsjetnici,
            onNavigateToProfil = onNavigateToProfil,
            currentRoute = "parametri"
        )
    }
}

@Composable
fun KrvniPritisakContent(
    lista: List<KrvniPritisak>,
    isAbnormalan: (KrvniPritisak) -> Boolean,
    onDelete: (String) -> Unit
) {
    if (lista.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nema unesenih mjerenja", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            KrvniPritisakGraf(lista = lista)
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(lista.reversed()) { kp ->
            KrvniPritisakCard(
                krvniPritisak = kp,
                abnormalan = isAbnormalan(kp),
                onDelete = { onDelete(kp.id) }
            )
        }
    }
}

@Composable
fun ParametarContent(
    lista: List<Parametar>,
    tip: ParametarTip,
    isAbnormalan: (Parametar) -> Boolean,
    onDelete: (String) -> Unit
) {
    if (lista.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nema unesenih mjerenja", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            ParametarGraf(lista = lista, tip = tip)
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(lista.reversed()) { parametar ->
            ParametarCard(
                parametar = parametar,
                tip = tip,
                abnormalan = isAbnormalan(parametar),
                onDelete = { onDelete(parametar.id) }
            )
        }
    }
}
@Composable
fun KrvniPritisakGraf(lista: List<KrvniPritisak>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Krvni pritisak kroz vrijeme", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(Color(0xFFE53935)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sistolički", fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(Color(0xFF1E88E5)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Dijastolički", fontSize = 12.sp)
                }
            }
            if (lista.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                val prosjekSis = lista.map { it.sistolicki }.average()
                val prosjekDij = lista.map { it.dijastolicki }.average()
                Text(
                    "Prosjek: %.0f/%.0f mmHg".format(prosjekSis, prosjekDij),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val sortirana = lista
                .filter { DateTimeUtils.parseTimestamp(it.datum, it.vrijeme) != null }
                .sortedBy { DateTimeUtils.parseTimestamp(it.datum, it.vrijeme) }
            if (sortirana.size < 2) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Potrebno je najmanje 2 mjerenja za prikaz grafa",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                return@Column
            }
            val xVrijednosti = sortirana.indices.map { it.toLong() }
            val datumiZaLabele = sortirana.map { it.datum to it.vrijeme }
            val rangeProvider = remember {
                CartesianLayerRangeProvider.fixed(minY = KrvniPritisak.GRAF_MIN, maxY = KrvniPritisak.GRAF_MAX)
            }
            val scrollState = rememberVicoScrollState(
                scrollEnabled = true,
                initialScroll = Scroll.Absolute.End
            )
            val zoomState = rememberVicoZoomState(zoomEnabled = false)
            val modelProducer = remember { CartesianChartModelProducer() }
            LaunchedEffect(sortirana) {
                modelProducer.runTransaction {
                    lineSeries {
                        series(xVrijednosti, sortirana.map { it.sistolicki })
                        series(xVrijednosti, sortirana.map { it.dijastolicki })
                    }
                }
                scrollState.scroll(Scroll.Absolute.End)
            }

            val sistolickiRaspon = rememberShapeComponent(
                fill = fill(Color(0xFFE53935).copy(alpha = 0.10f))
            )
            val dijastolickiRaspon = rememberShapeComponent(
                fill = fill(Color(0xFF1E88E5).copy(alpha = 0.10f))
            )
            val decorations = remember {
                listOf(
                    HorizontalBox(y = { 90.0..120.0 }, box = sistolickiRaspon),
                    HorizontalBox(y = { 60.0..80.0 }, box = dijastolickiRaspon)
                )
            }

            val marker = rememberDefaultCartesianMarker(
                label = rememberTextComponent(color = MaterialTheme.colorScheme.onSurface)
            )

            val bottomAxisLabel = rememberTextComponent(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineCount = 2,
                textAlignment = Layout.Alignment.ALIGN_CENTER
            )
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.single(fill(Color(0xFFE53935))),
                                stroke = LineCartesianLayer.LineStroke.continuous(thickness = 2.dp)
                            ),
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.single(fill(Color(0xFF1E88E5))),
                                stroke = LineCartesianLayer.LineStroke.continuous(thickness = 2.dp)
                            )
                        ),
                        rangeProvider = rangeProvider,
                        pointSpacing = 80.dp
                    ),
                    startAxis = VerticalAxis.rememberStart(
                        title = "mmHg"
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        label = bottomAxisLabel,
                        valueFormatter = CartesianValueFormatter { _, value, _ ->
                            formatIndexLabel(value.toInt(), datumiZaLabele)
                        }
                    ),
                    marker = marker,
                    decorations = decorations
                ),
                modelProducer = modelProducer,
                scrollState = scrollState,
                zoomState = zoomState,
                modifier = Modifier.fillMaxWidth().height(220.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Osjenčano polje = normalan raspon (90–120 / 60–80 mmHg)",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ParametarGraf(lista: List<Parametar>, tip: ParametarTip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("${tip.naziv} kroz vrijeme", fontWeight = FontWeight.Bold)
            if (lista.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                val prosjek = lista.map { it.vrijednost }.average()
                Text(
                    "Prosjek: %.1f %s".format(prosjek, tip.jedinica),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            val sortirana = lista
                .filter { DateTimeUtils.parseTimestamp(it.datum, it.vrijeme) != null }
                .sortedBy { DateTimeUtils.parseTimestamp(it.datum, it.vrijeme) }
            if (sortirana.size < 2) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Potrebno je najmanje 2 mjerenja za prikaz grafa",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                return@Column
            }
            val rangeProvider = remember(sortirana, tip) {
                if (tip == ParametarTip.TJELESNA_TEZINA) {
                    val vrijednosti = sortirana.map { it.vrijednost }
                    val stvarniMin = vrijednosti.min()
                    val stvarniMax = vrijednosti.max()
                    val raspon = stvarniMax - stvarniMin
                    val minimalniRaspon = 5.0
                    if (raspon < minimalniRaspon) {
                        val sredina = (stvarniMin + stvarniMax) / 2
                        CartesianLayerRangeProvider.fixed(
                            minY = sredina - minimalniRaspon / 2,
                            maxY = sredina + minimalniRaspon / 2
                        )
                    } else {
                        val margina = raspon * 0.1
                        CartesianLayerRangeProvider.fixed(minY = stvarniMin - margina, maxY = stvarniMax + margina)
                    }
                } else {
                    CartesianLayerRangeProvider.fixed(minY = tip.minGraf, maxY = tip.maxGraf)
                }
            }
            val xVrijednosti = sortirana.indices.map { it.toLong() }
            val datumiZaLabele = sortirana.map { it.datum to it.vrijeme }
            val scrollState = rememberVicoScrollState(
                scrollEnabled = true,
                initialScroll = Scroll.Absolute.End
            )
            val zoomState = rememberVicoZoomState(zoomEnabled = false)
            val modelProducer = remember { CartesianChartModelProducer() }
            LaunchedEffect(sortirana) {
                modelProducer.runTransaction {
                    lineSeries {
                        series(xVrijednosti, sortirana.map { it.vrijednost })
                    }
                }
                scrollState.scroll(Scroll.Absolute.End)
            }

            val rasponFill = rememberShapeComponent(
                fill = fill(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
            )
            val decorations = remember(tip) {
                if (tip == ParametarTip.TJELESNA_TEZINA) {
                    emptyList()
                } else {
                    listOf(
                        HorizontalBox(
                            y = { tip.minNormalno..tip.maxNormalno },
                            box = rasponFill
                        )
                    )
                }
            }

            val marker = rememberDefaultCartesianMarker(
                label = rememberTextComponent(color = MaterialTheme.colorScheme.onSurface)
            )

            val bottomAxisLabel = rememberTextComponent(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineCount = 2,
                textAlignment = Layout.Alignment.ALIGN_CENTER
            )

            val primaryColor = MaterialTheme.colorScheme.primary
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.single(fill(primaryColor)),
                                stroke = LineCartesianLayer.LineStroke.continuous(thickness = 2.dp)
                            )
                        ),
                        rangeProvider = rangeProvider,
                        pointSpacing = 70.dp
                    ),
                    startAxis = VerticalAxis.rememberStart(title = tip.jedinica),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        label = bottomAxisLabel,
                        valueFormatter = CartesianValueFormatter { _, value, _ ->
                            formatIndexLabel(value.toInt(), datumiZaLabele)
                        }
                    ),
                    marker = marker,
                    decorations = decorations
                ),
                modelProducer = modelProducer,
                scrollState = scrollState,
                zoomState = zoomState,
                modifier = Modifier.fillMaxWidth().height(220.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (tip != ParametarTip.TJELESNA_TEZINA) {
                Text(
                    "Osjenčano polje = normalan raspon (${tip.minNormalno}–${tip.maxNormalno} ${tip.jedinica})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun KrvniPritisakCard(
    krvniPritisak: KrvniPritisak,
    abnormalan: Boolean,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (abnormalan)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "${krvniPritisak.sistolicki}/${krvniPritisak.dijastolicki} mmHg",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (abnormalan) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${krvniPritisak.datum}${krvniPritisak.vrijeme.takeIf { it.isNotBlank() }?.let { " u $it" } ?: ""}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (abnormalan) {
                    Text("⚠ Van normalnih vrijednosti", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Obriši", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Brisanje mjerenja") },
            text = { Text("Da li ste sigurni da želite obrisati ovo mjerenje?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Da", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Ne") }
            }
        )
    }
}

@Composable
fun ParametarCard(
    parametar: Parametar,
    tip: ParametarTip,
    abnormalan: Boolean,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (abnormalan)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "${parametar.vrijednost} ${tip.jedinica}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (abnormalan) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${parametar.datum}${parametar.vrijeme.takeIf { it.isNotBlank() }?.let { " u $it" } ?: ""}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (abnormalan) {
                    Text("⚠ Van normalnih vrijednosti", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Obriši", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Brisanje mjerenja") },
            text = { Text("Da li ste sigurni da želite obrisati ovo mjerenje?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Da", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Ne") }
            }
        )
    }
}

private fun formatIndexLabel(index: Int, sortiranaDatumi: List<Pair<String, String>>): String {
    if (index !in sortiranaDatumi.indices) return ""
    val (datum, vrijeme) = sortiranaDatumi[index]
    return if (vrijeme.isNotBlank()) {
        "$datum\n$vrijeme"
    } else {
        datum
    }
}