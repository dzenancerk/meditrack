package com.example.meditrack.ui.theme.nalazi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditrack.data.model.Nalaz
import com.example.meditrack.data.repository.NalaziRepository
import com.example.meditrack.data.repository.mapFirestoreError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NalazDetaljiViewModel @Inject constructor(
    private val nalaziRepository: NalaziRepository
) : ViewModel() {
    var nalaz by mutableStateOf<Nalaz?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadNalaz(nalazId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                nalaz = nalaziRepository.getNalaz(nalazId)
                if (nalaz == null) {
                    errorMessage = "Nalaz nije pronađen"
                }
            } catch (e: Exception) {
                errorMessage = "Greška pri učitavanju nalaza: ${mapFirestoreError(e)}"
            } finally {
                isLoading = false
            }
        }
    }
}