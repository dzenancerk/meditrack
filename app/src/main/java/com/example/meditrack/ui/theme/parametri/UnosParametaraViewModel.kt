package com.example.meditrack.ui.theme.parametri

import androidx.compose.runtime.getValue
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
import java.util.Date
import javax.inject.Inject
import com.example.meditrack.data.model.SaveResult
import kotlinx.coroutines.flow.first
import com.example.meditrack.data.notification.SnackbarManager
import com.example.meditrack.data.repository.mapFirestoreError
import com.example.meditrack.utils.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.Locale

@HiltViewModel
class UnosParametraViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val parametriRepository: ParametriRepository,
    private val krvniPritisakRepository: KrvniPritisakRepository,
    private val snackbarManager: SnackbarManager
) : ViewModel() {

    var selectedTip by mutableStateOf(0)
        private set

    var vrijednost by mutableStateOf("")
        private set

    var sistolicki by mutableStateOf("")
        private set

    var dijastolicki by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    val tabovi = listOf("Krvni pritisak") + ParametarTip.entries.map { it.naziv }

    fun setInitialTab(tabIndex: Int) {
        selectedTip = tabIndex
    }


    fun onTipChange(index: Int) {
        selectedTip = index
        vrijednost = ""
        sistolicki = ""
        dijastolicki = ""
        errorMessage = null
    }

    fun onVrijednostChange(v: String) { vrijednost = v; errorMessage = null }
    fun onSistolickiChange(v: String) { sistolicki = v; errorMessage = null }
    fun onDijastolickiChange(v: String) { dijastolicki = v; errorMessage = null }

    fun onSave(onSuccess: () -> Unit) {
        val userId = authRepository.currentUser?.uid ?: return

        if (selectedTip == 0) {
            saveKrvniPritisak(userId, onSuccess)
        } else {
            saveParametar(userId, onSuccess)
        }
    }

    private fun saveKrvniPritisak(userId: String, onSuccess: () -> Unit) {
        val sis = sistolicki.toIntOrNull()
        val dij = dijastolicki.toIntOrNull()
        if (sis == null || sis <= 0) {
            errorMessage = "Unesite ispravnu sistolički vrijednost"
            return
        }
        if (dij == null || dij <= 0) {
            errorMessage = "Unesite ispravnu dijastolički vrijednost"
            return
        }
        if (sis < KrvniPritisak.MIN_SISTOLICKI || sis > KrvniPritisak.MAX_SISTOLICKI) {
            errorMessage = "Sistolički pritisak mora biti između ${KrvniPritisak.MIN_SISTOLICKI} i ${KrvniPritisak.MAX_SISTOLICKI} mmHg"
            return
        }
        if (dij < KrvniPritisak.MIN_DIJASTOLICKI || dij > KrvniPritisak.MAX_DIJASTOLICKI) {
            errorMessage = "Dijastolički pritisak mora biti između ${KrvniPritisak.MIN_DIJASTOLICKI} i ${KrvniPritisak.MAX_DIJASTOLICKI} mmHg"
            return
        }
        val sada = Date()
        val datumSnimanja = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(sada)
        val vrijemeSnimanja = SimpleDateFormat("HH:mm", Locale.getDefault()).format(sada)
        val noviTimestamp = DateTimeUtils.parseDateTime(datumSnimanja, vrijemeSnimanja)?.time
        if (noviTimestamp == null) {
            errorMessage = "Neispravan datum ili vrijeme"
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val postojeci = krvniPritisakRepository.getKrvniPritisak(userId).first()
                    .mapNotNull { DateTimeUtils.parseDateTime(it.datum, it.vrijeme)?.time }
                if (!jeRazmakDovoljan(noviTimestamp, postojeci, KrvniPritisak.MIN_RAZMAK_MINUTA)) {
                    errorMessage = "Mora proći najmanje ${KrvniPritisak.MIN_RAZMAK_MINUTA} minuta od posljednjeg mjerenja krvnog pritiska"
                    return@launch
                }
                val id = krvniPritisakRepository.generateId()
                val rezultat = krvniPritisakRepository.create(
                    KrvniPritisak(
                        id = id,
                        sistolicki = sis,
                        dijastolicki = dij,
                        datum = datumSnimanja,
                        vrijeme = vrijemeSnimanja,
                        korisnikId = userId
                    )
                )
                if (rezultat == SaveResult.TIMED_OUT) {
                    snackbarManager.show("Sačuvano lokalno — sinhronizovaće se automatski kad se veza vrati.")
                }
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Greška pri snimanju: ${mapFirestoreError(e)}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun saveParametar(userId: String, onSuccess: () -> Unit) {
        val tip = ParametarTip.entries[selectedTip - 1]
        val v = vrijednost.replace(",", ".").toDoubleOrNull()
        if (v == null || v <= 0) {
            errorMessage = "Unesite ispravnu vrijednost"
            return
        }
        if (v < tip.minMoguce || v > tip.maxMoguce) {
            errorMessage = "Vrijednost za ${tip.naziv} mora biti između ${tip.minMoguce} i ${tip.maxMoguce} ${tip.jedinica}"
            return
        }
        val sada = Date()
        val datumSnimanja = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(sada)
        val vrijemeSnimanja = SimpleDateFormat("HH:mm", Locale.getDefault()).format(sada)
        val noviTimestamp = DateTimeUtils.parseDateTime(datumSnimanja, vrijemeSnimanja)?.time
        if (noviTimestamp == null) {
            errorMessage = "Neispravan datum ili vrijeme"
            return
        }
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val postojeci = parametriRepository.getParametriByTip(userId, tip.kljuc).first()
                    .mapNotNull { DateTimeUtils.parseDateTime(it.datum, it.vrijeme)?.time }
                if (!jeRazmakDovoljan(noviTimestamp, postojeci, tip.minRazmakMinuta)) {
                    errorMessage = "Mora proći najmanje ${tip.minRazmakMinuta} minuta od posljednjeg mjerenja (${tip.naziv})"
                    return@launch
                }
                val id = parametriRepository.generateId()
                val rezultat = parametriRepository.create(
                    Parametar(
                        id = id,
                        tip = tip.kljuc,
                        vrijednost = v,
                        jedinica = tip.jedinica,
                        datum = datumSnimanja,
                        vrijeme = vrijemeSnimanja,
                        korisnikId = userId
                    )
                )
                if (rezultat == SaveResult.TIMED_OUT) {
                    snackbarManager.show("Sačuvano lokalno — sinhronizovaće se automatski kad se veza vrati.")
                }
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Greška pri snimanju: ${mapFirestoreError(e)}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun jeRazmakDovoljan(
        noviTimestamp: Long,
        postojeciTimestampi: List<Long>,
        minRazmakMinuta: Int
    ): Boolean {
        val minRazmakMs = minRazmakMinuta * 60_000L
        return postojeciTimestampi.none { Math.abs(noviTimestamp - it) < minRazmakMs }
    }
}