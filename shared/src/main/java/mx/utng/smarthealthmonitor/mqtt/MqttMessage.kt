package mx.utng.smarthealthmonitor.mqtt

import kotlinx.serialization.Serializable

@Serializable
data class FcMessage(
    val bpm       : Int,
    val estado    : String,
    val timestamp : Long = System.currentTimeMillis()
)

@Serializable
data class TvMessage(
    val bpm    : Int,
    val pasos  : Int = 0,
    val estado : String,
    val hora   : String
)

@Serializable
data class AlertMessage(
    val tipo    : String,
    val bpm     : Int,
    val mensaje : String
)
