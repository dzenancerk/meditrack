package com.example.meditrack.ui.theme.podsjetnici

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
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.catch
import com.example.meditrack.data.model.SaveResult
import com.example.meditrack.data.notification.SnackbarManager
import com.example.meditrack.data.notification.PodsjetnikReminderScheduler
import com.example.meditrack.data.repository.mapFirestoreError
import com.example.meditrack.utils.DateTimeUtils
@HiltViewModel
class PodsjetniciViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val podsjetniciRepository: PodsjetniciRepository,
    private val snackbarManager: SnackbarManager,
    private val reminderScheduler: PodsjetnikReminderScheduler,
) : ViewModel() {

    var podsjetnici by mutableStateOf<List<Podsjetnik>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var statusFilter by mutableStateOf("sve")
        private set
    var tipFilter by mutableStateOf("sve")
        private set

    companion object {
        private val ISTEKAO_NAKON_MS = TimeUnit.HOURS.toMillis(12)
    }
    private var sviPodsjetnici: List<Podsjetnik> = emptyList()

    init {
        loadPodsjetnici()
    }

    fun loadPodsjetnici() {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading = true
            podsjetniciRepository.getPodsjetnici(userId)
                .catch { e ->
                    errorMessage = "Greška pri učitavanju podsjetnika: ${mapFirestoreError(e)}"
                    isLoading = false
                }
                .collect { lista ->
                    val (aktivni, istekli) = razdvojIstekle(lista)
                    sviPodsjetnici = aktivni
                    primijeniFilter()
                    errorMessage = null
                    isLoading = false
                    obrisiIstekle(istekli)
                }
        }
    }
    fun onStatusFilterChange(vrijednost: String) {
        statusFilter = vrijednost
        primijeniFilter()
    }
    fun onTipFilterChange(vrijednost: String) {
        tipFilter = vrijednost
        primijeniFilter()
    }
    private fun primijeniFilter() {
        val now = Date()
        var rezultat = sviPodsjetnici
        rezultat = when (statusFilter) {
            "aktivno" -> rezultat.filter { DateTimeUtils.parseDateTime(it.datum, it.vrijeme)?.after(now) == true }
            "proslo" -> rezultat.filter { DateTimeUtils.parseDateTime(it.datum, it.vrijeme)?.after(now) != true }
            else -> rezultat
        }
        rezultat = when (tipFilter) {
            "lijek" -> rezultat.filter { it.tip == "lijek" }
            "pregled" -> rezultat.filter { it.tip == "pregled" }
            else -> rezultat
        }
        podsjetnici = rezultat.sortedBy { DateTimeUtils.parseDateTime(it.datum, it.vrijeme) }
    }

    fun deletePodsjetnik(podsjetnikId: String) {
        viewModelScope.launch {
            try {
                reminderScheduler.cancel(podsjetnikId)
                val rezultat = podsjetniciRepository.delete(podsjetnikId)
                if (rezultat == SaveResult.TIMED_OUT) {
                    snackbarManager.show("Obrisano lokalno — sinhronizovaće se automatski kad se veza vrati.")
                }
                errorMessage = null
                loadPodsjetnici()
            } catch (e: Exception) {
                errorMessage = "Greška pri brisanju podsjetnika: ${mapFirestoreError(e)}"
            }
        }
    }

    private fun razdvojIstekle(lista: List<Podsjetnik>): Pair<List<Podsjetnik>, List<Podsjetnik>> {
        val now = Date()
        return lista.partition { p ->
            val vrijeme = DateTimeUtils.parseDateTime(p.datum, p.vrijeme)
            vrijeme == null || now.time - vrijeme.time < ISTEKAO_NAKON_MS
        }
    }

    private fun obrisiIstekle(istekli: List<Podsjetnik>) {
        if (istekli.isEmpty()) return
        viewModelScope.launch {
            istekli.forEach { p ->
                try {
                    podsjetniciRepository.delete(p.id)
                } catch (e: Exception) {
                }
            }
        }
    }
}