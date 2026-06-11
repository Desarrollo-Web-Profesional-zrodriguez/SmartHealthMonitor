package mx.utng.smarthealthmonitor.data.models

import mx.utng.smarthealthmonitor.data.db.LecturaFC

// Datos de prueba para desarrollo (mock data)
object MockData {
    val historialFC = listOf(
        LecturaFC(1, 78, 1717410000000, "11:00", true),
        LecturaFC(2, 82, 1717408200000, "10:30", true),
        LecturaFC(3, 76, 1717406400000, "10:00", true),
        LecturaFC(4, 110, 1717404600000, "09:30", false),
        LecturaFC(5, 71, 1717402800000, "09:00", true),
        LecturaFC(6, 80, 1717401000000, "08:30", true),
        LecturaFC(7, 74, 1717399200000, "08:00", true)
    )
    var fcActual = 78
    var pasosActual = 4250
}
