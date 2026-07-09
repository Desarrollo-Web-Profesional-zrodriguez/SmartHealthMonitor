package mx.utng.smarthealthmonitor.tv.domain.model

import mx.utng.smarthealthmonitor.data.db.LecturaFC

data class TvUiState(
    val lecturas    : List<LecturaFC> = emptyList(),
    val fcActual    : Int             = 0,
    val isLoading   : Boolean         = true,
    val error       : String?         = null,
)
