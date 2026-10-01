package com.example.meditrack.data.model

enum class ParametarTip(
    val kljuc: String,
    val naziv: String,
    val jedinica: String,
    val minNormalno: Double,
    val maxNormalno: Double,
    val minRazmakMinuta: Int,
    val minMoguce: Double,
    val maxMoguce: Double,
    val minGraf: Double,
    val maxGraf: Double
) {
    PULS("puls", "Puls", "bpm", 60.0, 100.0, 15, 15.0, 220.0, 30.0, 120.0),
    SECER_U_KRVI("secer", "Šećer u krvi", "mmol/L", 3.9, 6.1, 120, 1.0, 33.0, 2.0, 20.0),
    TJELESNA_TEZINA("tezina", "Tjelesna težina", "kg", 40.0, 150.0, 720, 20.0, 300.0, 0.0, 0.0),
    TEMPERATURA("temperatura", "Temperatura", "°C", 36.1, 37.2, 15, 25.0, 43.0, 34.0, 40.0);

    companion object {
        fun fromKljuc(kljuc: String) = entries.find { it.kljuc == kljuc }
    }
}