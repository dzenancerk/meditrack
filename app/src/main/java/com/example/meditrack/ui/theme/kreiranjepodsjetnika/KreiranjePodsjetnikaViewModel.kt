package com.example.meditrack.ui.theme.kreiranjepodsjetnika

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditrack.data.model.Podsjetnik
import com.example.meditrack.data.repository.AuthRepository
import com.example.meditrack.data.repository.PodsjetniciRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Date
import com.example.meditrack.data.model.SaveResult
import com.example.meditrack.data.notification.PodsjetnikReminderScheduler
import com.example.meditrack.data.notification.SnackbarManager
import com.example.meditrack.data.repository.mapFirestoreError
import com.example.meditrack.utils.DateTimeUtils

@HiltViewModel
class KreiranjePodsjetnikaViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val podsjetniciRepository: PodsjetniciRepository,
    private val snackbarManager: SnackbarManager,
    private val reminderScheduler: PodsjetnikReminderScheduler,
) : ViewModel() {

    var podsjetnikId by mutableStateOf("")
        private set
    var naziv by mutableStateOf("")
        private set
    var selectedTip by mutableStateOf("lijek")
        private set
    var selectedDatum by mutableStateOf("")
        private set
    var selectedVrijeme by mutableStateOf("")
        private set
    var opis by mutableStateOf("")
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isEditMode by mutableStateOf(false)
        private set

    var isLoadingPodsjetnik by mutableStateOf(false)
        private set

    fun ucitajPodsjetnikZaEdit(id: String) {
        if (id.isBlank() || id == podsjetnikId) return
        isEditMode = true
        viewModelScope.launch {
            isLoadingPodsjetnik = true
            errorMessage = null
            try {
                val podsjetnik = podsjetniciRepository.getPodsjetnik(id)
                if (podsjetnik != null) {
                    podsjetnikId = podsjetnik.id
                    naziv = podsjetnik.naziv
                    selectedTip = podsjetnik.tip
                    selectedDatum = podsjetnik.datum
                    selectedVrijeme = podsjetnik.vrijeme
                    opis = podsjetnik.opis
                } else {
                    errorMessage = "Podsjetnik nije pronađen"
                }
            } catch (e: Exception) {
                errorMessage = "Greška pri učitavanju podsjetnika: ${mapFirestoreError(e)}"
            } finally {
                isLoadingPodsjetnik = false
            }
        }
    }

    fun onNazivChange(value: String) { naziv = value; errorMessage = null }
    fun onTipChange(value: String) { selectedTip = value; errorMessage = null }
    fun onDatumChange(value: String) { selectedDatum = value; errorMessage = null }
    fun onVrijemeChange(value: String) { selectedVrijeme = value; errorMessage = null }
    fun onOpisChange(value: String) { opis = value }

    fun onSave(onSuccess: () -> Unit) {
        if (naziv.isBlank()) {
            errorMessage = "Unesite naziv"
            return
        }
        if (selectedDatum.isBlank()) {
            errorMessage = "Izaberite datum"
            return
        }
        if (selectedVrijeme.isBlank()) {
            errorMessage = "Izaberite vrijeme"
            return
        }
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val dateTime = DateTimeUtils.parseDateTime(selectedDatum, selectedVrijeme)
                if (dateTime == null || dateTime.before(Date())) {
                    errorMessage = "Ne možete kreirati podsjetnik za prošlo vrijeme."
                    return@launch
                }
                val idZaSnimanje = if (isEditMode) podsjetnikId else podsjetniciRepository.generateId()
                val podsjetnik = Podsjetnik(
                    id = idZaSnimanje,
                    naziv = naziv,
                    tip = selectedTip,
                    datum = selectedDatum,
                    vrijeme = selectedVrijeme,
                    opis = opis,
                    korisnikId = userId
                )
                val rezultat = if (isEditMode) {
                    reminderScheduler.cancel(podsjetnikId)
                    podsjetniciRepository.update(podsjetnik)
                } else {
                    podsjetniciRepository.create(podsjetnik)
                }
                if (rezultat == SaveResult.TIMED_OUT) {
                    snackbarManager.show("Sačuvano lokalno — sinhronizovaće se automatski kad se veza vrati.")
                }
                reminderScheduler.schedule(podsjetnik)
                onSuccess()
            } catch (e: Exception) {
                errorMessage = "Greška pri snimanju podsjetnika: ${mapFirestoreError(e)}"
            } finally {
                isLoading = false
            }
        }
    }
}