package com.example.meditrack.ui.theme.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.meditrack.ui.theme.components.MediTrackTopBar
import com.example.meditrack.ui.theme.components.NetworkStatusBanner
import com.example.meditrack.ui.theme.shared.LoadingIndicator

@Composable
fun SignInScreen(
    onNavigateToSignUp: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SignInViewModel = hiltViewModel()
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data, onNavigateToHome)
    }

    if (viewModel.isLoading) {
        LoadingIndicator()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            MediTrackTopBar()
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
                    value = viewModel.email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = viewModel.errorMessage != null
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = viewModel.password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    label = { Text("Šifra") },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Default.Visibility
                                else
                                    Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = viewModel.errorMessage != null
                )
                if (viewModel.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { viewModel.showResetDialog() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Zaboravili ste šifru?")
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.onSignIn(onNavigateToHome) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    enabled = !viewModel.isLoading
                ) {
                    Text("Prijavite se")
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Nemate račun?")
                TextButton(onClick = onNavigateToSignUp) {
                    Text("Registrujte se")
                }
                Button(
                    onClick = {
                        val intent = viewModel.getGoogleSignInIntent()
                        googleSignInLauncher.launch(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                ) {
                    Text("Nastavite preko Google profila")
                }
            }
            if (viewModel.resetDialogVisible) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissResetDialog() },
                    title = { Text("Reset lozinke") },
                    text = {
                        Column {
                            Text(
                                "Unesite email adresu na koju ćemo poslati link za reset lozinke.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = viewModel.resetEmail,
                                onValueChange = viewModel::onResetEmailChange,
                                label = { Text("Email") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !viewModel.isSendingReset
                            )
                            if (viewModel.resetMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = viewModel.resetMessage ?: "",
                                    color = if (viewModel.resetMessage?.contains("poslan") == true)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { viewModel.sendPasswordReset() },
                            enabled = !viewModel.isSendingReset
                        ) {
                            if (viewModel.isSendingReset) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            } else {
                                Text("Pošaljite")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { viewModel.dismissResetDialog() }) {
                            Text("Zatvorite")
                        }
                    }
                )
            }
        }
    }
}