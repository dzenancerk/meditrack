package com.example.meditrack.ui.theme.parametri

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditrack.data.model.KrvniPritisak
import com.example.meditrack.data.model.Parametar
import com.example.meditrack.data.model.ParametarTip
import com.example.meditrack.data.repository.AuthRepository
import com.example.meditrack.data.repository.KrvniPritisakRepository
import com.example.meditrack.data.repository.ParametriRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.Job
import com.example.meditrack.data.model.SaveResult
import com.example.meditrack.data.notification.SnackbarManager
import com.example.meditrack.data.repository.mapFirestoreError
import com.example.meditrack.utils.DateTimeUtils
@HiltViewModel
class ParametriViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val parametriRepository: ParametriRepository,
    private val krvniPritisakRepository: KrvniPritisakRepository,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    var selectedTab by mutableIntStateOf(0)
        private set

    var parametri by mutableStateOf<List<Parametar>>(emptyList())
        private set

    var krvniPritisak by mutableStateOf<List<KrvniPritisak>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set
    private var loadJob: Job? = null
    val tabovi = listOf("Krvni pritisak") + ParametarTip.entries.map { it.naziv }

    init {
        loadData()
    }

    fun onTabChange(index: Int) {
        selectedTab = index
        loadData()
    }

    fun loadData() {
        val userId = authRepository.currentUser?.uid ?: return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            isLoading = true
            errorMessage = null
            if (selectedTab == 0) {
                krvniPritisakRepository.getKrvniPritisak(userId)
                    .catch { e ->
                        errorMessage = "Greška pri učitavanju: ${mapFirestoreError(e)}"
                        isLoading = false
                    }
                    .collect { lista ->
                        krvniPritisak = lista.sortedBy { DateTimeUtils.parseDateTime(it.datum, it.vrijeme) }
                        isLoading = false
                    }
            } else {
                val tip = ParametarTip.entries[selectedTab - 1]
                parametriRepository.getParametriByTip(userId, tip.kljuc)
                    .catch { e ->
                        errorMessage = "Greška pri učitavanju: ${mapFirestoreError(e)}"
                        isLoading = false
                    }
                    .collect { lista ->
                        parametri = lista.sortedBy { DateTimeUtils.parseDateTime(it.datum, it.vrijeme) }
                        isLoading = false
                    }
            }
        }
    }

    fun deleteParametar(parametarId: String) {
        viewModelScope.launch {
            try {
                val rezultat = parametriRepository.delete(parametarId)
                if (rezultat == SaveResult.TIMED_OUT) {
                    snackbarManager.show("Obrisano lokalno — sinhronizovaće se automatski kad se veza vrati.")
                }
                loadData()
            } catch (e: Exception) {
                errorMessage = "Greška pri brisanju: ${mapFirestoreError(e)}"
            }
        }
    }

    fun deleteKrvniPritisak(id: String) {
        viewModelScope.launch {
            try {
                val rezultat = krvniPritisakRepository.delete(id)
                if (rezultat == SaveResult.TIMED_OUT) {
                    snackbarManager.show("Obrisano lokalno — sinhronizovaće se automatski kad se veza vrati.")
                }
                loadData()
            } catch (e: Exception) {
                errorMessage = "Greška pri brisanju: ${mapFirestoreError(e)}"
            }
        }
    }

    fun isAbnormalan(parametar: Parametar): Boolean {
        val tip = ParametarTip.fromKljuc(parametar.tip) ?: return false
        return parametar.vrijednost < tip.minNormalno || parametar.vrijednost > tip.maxNormalno
    }

    fun isKrvniPritisakAbnormalan(kp: KrvniPritisak): Boolean {
        return kp.sistolicki < 90 || kp.sistolicki > 120 ||
                kp.dijastolicki < 60 || kp.dijastolicki > 80
    }
}