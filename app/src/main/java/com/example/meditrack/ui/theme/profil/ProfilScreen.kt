package com.example.meditrack.ui.theme.profil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.meditrack.ui.theme.components.MediTrackBottomNavigationBar
import com.example.meditrack.ui.theme.components.MediTrackTopBar
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.meditrack.ui.theme.components.NetworkStatusBanner
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.os.Build

@Composable
fun ProfilScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToNalazi: () -> Unit,
    onNavigateToParametri: () -> Unit,
    onNavigateToPodsjetnici: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ProfilViewModel = hiltViewModel()
) {
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.osvjeziSistemskuDozvoluNotifikacija()
                viewModel.osvjeziAlarmPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        MediTrackTopBar()
        NetworkStatusBanner()

        if (!viewModel.isLoading && viewModel.user != null && !viewModel.isEmailVerified) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Email adresa nije verifikovana",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Provjerite inbox i potvrdite email adresu.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    if (viewModel.verificationMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.verificationMessage ?: "",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { viewModel.posaljiVerifikacioniEmail() },
                        enabled = !viewModel.isSendingVerification
                    ) {
                        if (viewModel.isSendingVerification) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        } else {
                            Text("Pošaljite ponovo")
                        }
                    }
                }
            }
        }
        if (viewModel.systemNotificationsBlocked) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Notifikacije su blokirane",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Nećete dobijati podsjetnike jer su notifikacije blokirane u podešavanjima uređaja. Otvorite podešavanja da ih omogućite.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Otvorite podešavanja")
                    }
                }
            }
        }
        if (viewModel.preciznaPodsjetnikaDozvolaBlokirana && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Precizni podsjetnici nisu potpuno omogućeni",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Za tačno vrijeme podsjetnika (1h, 15min, 5min prije) potrebna je dodatna dozvola. Bez nje, podsjetnici mogu kasniti.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = {
                        val intent = Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }) {
                        Text("Otvorite podešavanja")
                    }
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator()
            } else if (viewModel.user != null) {
                val user = viewModel.user!!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ProfilInfoItem(label = "IME", value = user.ime)
                    ProfilInfoItem(label = "PREZIME", value = user.prezime)
                    ProfilInfoItem(label = "KORISNIČKO IME", value = user.korisnickoIme)
                    ProfilInfoItem(label = "EMAIL", value = user.email)
                    ProfilInfoItem(label = "TELEFON", value = user.brojTelefona)
                }
                Spacer(modifier = Modifier.height(30.dp))
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notifikacije")
                    Switch(
                        checked = viewModel.notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications(it) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    viewModel.prepareEdit()
                    showEditDialog = true
                }) {
                    Text("Uredite profil")
                }
                Spacer(modifier = Modifier.height(6.dp))
                if (viewModel.errorMessage != null) {
                    Text(
                        text = viewModel.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp
                    )
                }
                Button(onClick = { showLogoutDialog = true }) {
                    Text("Odjavite se")
                }
            } else {
                Text(
                    "Greška pri učitavanju profila",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { showLogoutDialog = true }) {
                    Text("Odjavite se")
                }
            }

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Odjava") },
                    text = { Text("Da li ste sigurni da želite da se odjavite?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showLogoutDialog = false
                            viewModel.onSignOut(onNavigateToLogin)
                        }) { Text("Da") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) { Text("Ne") }
                    }
                )
            }
        }

        MediTrackBottomNavigationBar(
            onNavigateToHome = onNavigateToHome,
            onNavigateToNalazi = onNavigateToNalazi,
            onNavigateToParametri = onNavigateToParametri,
            onNavigateToPodsjetnici = onNavigateToPodsjetnici,
            onNavigateToProfil = {},
            currentRoute = "profil"
        )

        if (showEditDialog) {
            EditProfilDialog(
                korisnickoIme = viewModel.editKorisnickoIme,
                brojTelefona = viewModel.editBrojTelefona,
                errorMessage = viewModel.updateErrorMessage,
                isLoading = viewModel.isUpdating,
                onKorisnickoImeChange = viewModel::onEditKorisnickoImeChange,
                onBrojTelefonaChange = viewModel::onEditBrojTelefonaChange,
                onDismiss = { showEditDialog = false },
                onConfirm = { viewModel.updateUser { showEditDialog = false } }
            )
        }
    }
}

@Composable
fun ProfilInfoItem(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EditProfilDialog(
    korisnickoIme: String,
    brojTelefona: String,
    errorMessage: String?,
    isLoading: Boolean,
    onKorisnickoImeChange: (String) -> Unit,
    onBrojTelefonaChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Uredi profil") },
        text = {
            Column {
                OutlinedTextField(
                    value = korisnickoIme,
                    onValueChange = onKorisnickoImeChange,
                    label = { Text("Korisničko ime") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    isError = errorMessage != null
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = brojTelefona,
                    onValueChange = onBrojTelefonaChange,
                    label = { Text("Broj telefona") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    isError = errorMessage != null
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Sačuvaj")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Otkaži")
            }
        }
    )
}