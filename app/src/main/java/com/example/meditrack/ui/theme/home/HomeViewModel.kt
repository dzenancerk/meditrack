package com.example.meditrack.ui.theme.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditrack.data.model.KrvniPritisak
import com.example.meditrack.data.model.Parametar
import com.example.meditrack.data.model.Podsjetnik
import com.example.meditrack.data.repository.AuthRepository
import com.example.meditrack.data.repository.KrvniPritisakRepository
import com.example.meditrack.data.repository.ParametriRepository
import com.example.meditrack.data.repository.PodsjetniciRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.async
import com.example.meditrack.data.model.ParametarTip
import com.example.meditrack.data.repository.mapFirestoreError
import kotlinx.coroutines.flow.first
import com.example.meditrack.utils.DateTimeUtils

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val podsjetniciRepository: PodsjetniciRepository,
    private val parametriRepository: ParametriRepository,
    private val krvniPritisakRepository: KrvniPritisakRepository
) : ViewModel() {

    var nextPodsjetnici by mutableStateOf<List<Podsjetnik>>(emptyList())
        private set
    var lastParametriByTip by mutableStateOf<Map<ParametarTip, Parametar>>(emptyMap())
        private set
    var lastKrvniPritisak by mutableStateOf<KrvniPritisak?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadData()
    }

    fun loadData() {
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val (podsjetnici, parametri, krvniPritisci) = kotlinx.coroutines.coroutineScope {
                    val p = async { podsjetniciRepository.getPodsjetnici(userId).first() }
                    val par = async { parametriRepository.getParametri(userId).first() }
                    val kp = async { krvniPritisakRepository.getKrvniPritisak(userId).first() }
                    Triple(p.await(), par.await(), kp.await())
                }
                val now = Date()
                nextPodsjetnici = podsjetnici
                    .filter { DateTimeUtils.parseDateTime(it.datum, it.vrijeme)?.after(now) == true }
                    .sortedBy { DateTimeUtils.parseDateTime(it.datum, it.vrijeme) }
                    .take(3)

                lastParametriByTip = parametri
                    .groupBy { it.tip }
                    .mapNotNull { (tipKljuc, lista) ->
                        val tip = ParametarTip.fromKljuc(tipKljuc) ?: return@mapNotNull null
                        val najnoviji = lista.maxByOrNull { DateTimeUtils.parseDateTime(it.datum, it.vrijeme) ?: Date(0) }
                            ?: return@mapNotNull null
                        tip to najnoviji
                    }
                    .toMap()

                lastKrvniPritisak = krvniPritisci
                    .sortedByDescending { DateTimeUtils.parseDateTime(it.datum, it.vrijeme) }
                    .firstOrNull()
            } catch (e: Exception) {
                errorMessage = "Greška pri učitavanju: ${mapFirestoreError(e)}"
            } finally {
                isLoading = false
            }
        }
    }
}