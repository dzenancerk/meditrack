package com.example.meditrack.ui.theme.nalazi

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditrack.data.model.Nalaz
import com.example.meditrack.data.repository.AuthRepository
import com.example.meditrack.data.repository.NalaziRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import com.example.meditrack.data.model.SaveResult
import com.example.meditrack.data.notification.SnackbarManager
import com.example.meditrack.data.repository.mapFirestoreError
import com.example.meditrack.utils.DateTimeUtils
@HiltViewModel
class NalaziViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val nalaziRepository: NalaziRepository,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    var nalazi by mutableStateOf<List<Nalaz>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var tipFilter by mutableStateOf("sve")
        private set
    private var sviNalazi: List<Nalaz> = emptyList()
    init {
        loadNalazi()
    }

    fun loadNalazi() {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            nalaziRepository.getNalazi(userId)
                .catch { e ->
                    errorMessage = "Greška pri učitavanju nalaza: ${mapFirestoreError(e)}"
                    isLoading = false
                }
                .collect { lista ->
                    sviNalazi = lista
                    primijeniFilter()
                    isLoading = false
                }
        }
    }


    fun onTipFilterChange(vrijednost: String) {
        tipFilter = vrijednost
        primijeniFilter()
    }

    private fun primijeniFilter() {
        val filtrirano = if (tipFilter == "sve") {
            sviNalazi
        } else {
            sviNalazi.filter { it.tip == tipFilter }
        }
        nalazi = filtrirano.sortedByDescending { DateTimeUtils.parseDateTime(it.datum) }
    }
    fun deleteNalaz(nalaz: Nalaz) {
        viewModelScope.launch {
            try {
                val rezultat = nalaziRepository.delete(nalaz)
                if (rezultat == SaveResult.TIMED_OUT) {
                    snackbarManager.show("Obrisano lokalno — sinhronizovaće se automatski kad se veza vrati.")
                }
                loadNalazi()
            } catch (e: Exception) {
                errorMessage = "Greška pri brisanju: ${e.message}"
            }
        }
    }
}