package com.example.meditrack.ui.theme.nalazi

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.meditrack.data.model.Nalaz
import com.example.meditrack.data.ocr.OcrHelper
import com.example.meditrack.data.repository.AuthRepository
import com.example.meditrack.data.repository.NalaziRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import com.example.meditrack.data.model.SaveResult
import com.example.meditrack.data.notification.SnackbarManager
import com.example.meditrack.data.model.NalazSlika
import com.example.meditrack.data.repository.mapFirestoreError

@HiltViewModel
class DodavanjeNalazaViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val nalaziRepository: NalaziRepository,
    private val ocrHelper: OcrHelper,
    private val snackbarManager: SnackbarManager
) : ViewModel() {
    var naziv by mutableStateOf("")
        private set
    var selectedTip by mutableStateOf("laboratorijski")
        private set
    var selectedDatum by mutableStateOf(
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
    )
        private set
    var opis by mutableStateOf("")
        private set

    var slike by mutableStateOf<List<NalazSlika>>(emptyList())
        private set

    var selectedSlikaIndex by mutableStateOf<Int?>(null)
        private set
    var isUploading by mutableStateOf(false)
        private set
    var formErrorMessage by mutableStateOf<String?>(null)
        private set

    var isRecognizingText by mutableStateOf(false)
        private set
    var ocrErrorMessage by mutableStateOf<String?>(null)
        private set

    var prepoznatiTekst by mutableStateOf("")
        private set

    private var originalneSlikeUrls: Set<String> = emptySet()

    fun onPrepoznatiTekstChange(value: String) {
        prepoznatiTekst = value
    }

    fun onNazivChange(value: String) {
        naziv = value; formErrorMessage = null
    }

    fun onTipChange(value: String) {
        selectedTip = value; formErrorMessage = null
    }

    fun onDatumChange(value: String) {
        selectedDatum = value; formErrorMessage = null
    }

    fun onOpisChange(value: String) {
        opis = value
    }

    fun onImagesSelected(uris: List<Uri>) {
        if (uris.isEmpty()) return
        val slobodnaMjesta = Nalaz.MAX_SLIKA - slike.size
        if (slobodnaMjesta <= 0) {
            formErrorMessage = "Dostignut je maksimalan broj slika po nalazu (${Nalaz.MAX_SLIKA})"
            return
        }
        val zaDodati = uris.take(slobodnaMjesta).map { NalazSlika.Nova(it) }
        slike = slike + zaDodati
        formErrorMessage = if (uris.size > slobodnaMjesta) {
            "Dodano je ${zaDodati.size} od ${uris.size} slika — dostignut je maksimum od ${Nalaz.MAX_SLIKA}"
        } else null
    }

    fun removeSlika(index: Int) {
        if (index !in slike.indices) return
        slike = slike.toMutableList().apply { removeAt(index) }
        selectedSlikaIndex = when {
            selectedSlikaIndex == index -> null
            selectedSlikaIndex != null && selectedSlikaIndex!! > index -> selectedSlikaIndex!! - 1
            else -> selectedSlikaIndex
        }
        ocrErrorMessage = null
        formErrorMessage = null
    }

    fun onSlikaClick(index: Int) {
        selectedSlikaIndex = if (selectedSlikaIndex == index) null else index
        ocrErrorMessage = null
    }

    fun onScannerError(message: String) {
        formErrorMessage = "Greška pri skeniranju: $message"
    }

    fun prepoznajTekstSaSlike() {
        val index = selectedSlikaIndex ?: return
        val slika = slike.getOrNull(index) ?: return
        val uri = when (slika) {
            is NalazSlika.Nova -> slika.uri
            is NalazSlika.Postojeca -> Uri.fromFile(File(slika.url))
        }
        viewModelScope.launch {
            isRecognizingText = true
            ocrErrorMessage = null
            try {
                val prepoznatTekstSaSlike = withContext(Dispatchers.Default) {
                    ocrHelper.prepoznajTekst(uri)
                }
                if (prepoznatTekstSaSlike.isBlank()) {
                    ocrErrorMessage =
                        "Nije moguće prepoznati tekst sa slike. Pokušajte sa jasnijom fotografijom."
                } else {
                    prepoznatiTekst = if (prepoznatiTekst.isBlank()) {
                        prepoznatTekstSaSlike
                    } else {
                        "$prepoznatiTekst\n\n--- Prepoznat tekst sa slike ---\n$prepoznatTekstSaSlike"
                    }
                }
            } catch (e: Exception) {
                ocrErrorMessage = "Greška pri prepoznavanju teksta: ${e.message}"
            } finally {
                isRecognizingText = false
            }
        }
    }

    var nalazId by mutableStateOf("")
        private set
    var isEditMode by mutableStateOf(false)
        private set
    var isLoadingNalaz by mutableStateOf(false)
        private set

    fun ucitajNalazZaEdit(id: String) {
        if (id.isBlank() || id == nalazId) return
        nalazId = id
        isEditMode = true
        viewModelScope.launch {
            isLoadingNalaz = true
            formErrorMessage = null
            try {
                val nalaz = nalaziRepository.getNalaz(id)
                if (nalaz != null) {
                    naziv = nalaz.naziv
                    selectedTip = nalaz.tip
                    selectedDatum = nalaz.datum
                    opis = nalaz.opis
                    prepoznatiTekst = nalaz.prepoznatiTekst
                    slike = nalaz.fileUrls.map { NalazSlika.Postojeca(it) }
                    originalneSlikeUrls = nalaz.fileUrls.toSet()
                } else {
                    formErrorMessage = "Nalaz nije pronađen"
                }
            } catch (e: Exception) {
                formErrorMessage = "Greška pri učitavanju nalaza: ${mapFirestoreError(e)}"
            } finally {
                isLoadingNalaz = false
            }
        }
    }

    fun saveNalaz(onSuccess: () -> Unit) {
        if (naziv.isBlank()) {
            formErrorMessage = "Unesite naziv nalaza"
            return
        }
        if (selectedDatum.isBlank()) {
            formErrorMessage = "Izaberite datum"
            return
        }
        val userId = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            isUploading = true
            formErrorMessage = null
            try {
                val finalUrls = nalaziRepository.resolveImageUrls(slike, userId)
                val idZaSnimanje = if (isEditMode) nalazId else nalaziRepository.generateId()
                val nalaz = Nalaz(
                    id = idZaSnimanje,
                    naziv = naziv,
                    tip = selectedTip,
                    datum = selectedDatum,
                    opis = opis,
                    prepoznatiTekst = prepoznatiTekst,
                    fileUrls = finalUrls,
                    korisnikId = userId
                )
                val rezultat = if (isEditMode) {
                    val uklonjeneSlike = (originalneSlikeUrls - finalUrls.toSet()).toList()
                    nalaziRepository.update(nalaz, uklonjeneSlike)
                } else {
                    nalaziRepository.create(nalaz)
                }
                if (rezultat == SaveResult.TIMED_OUT) {
                    snackbarManager.show("Sačuvano lokalno — sinhronizovaće se automatski kad se veza vrati.")
                }
                onSuccess()
            } catch (e: Exception) {
                formErrorMessage = "Greška pri snimanju: ${mapFirestoreError(e)}"
            } finally {
                isUploading = false
            }
        }
    }
}
