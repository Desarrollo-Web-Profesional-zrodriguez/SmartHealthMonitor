package mx.utng.smarthealthmonitor.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LecturaFCDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(lectura: LecturaFC)

    // Flow: actualización reactiva cuando hay nuevos datos
    @Query("""
        SELECT * FROM lecturas_fc
        ORDER BY timestamp DESC
        LIMIT 50""")  // últimas 50 lecturas
    fun obtenerUltimas(): Flow<List<LecturaFC>>
}


