package mx.utng.smarthealthmonitor.`data`.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class LecturaFCDao_Impl(
  __db: RoomDatabase,
) : LecturaFCDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfLecturaFC: EntityInsertAdapter<LecturaFC>
  init {
    this.__db = __db
    this.__insertAdapterOfLecturaFC = object : EntityInsertAdapter<LecturaFC>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `lecturas_fc` (`id`,`valorBpm`,`timestamp`,`hora`,`esNormal`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: LecturaFC) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.valorBpm.toLong())
        statement.bindLong(3, entity.timestamp)
        statement.bindText(4, entity.hora)
        val _tmp: Int = if (entity.esNormal) 1 else 0
        statement.bindLong(5, _tmp.toLong())
      }
    }
  }

  public override suspend fun insertar(lectura: LecturaFC): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfLecturaFC.insert(_connection, lectura)
  }

  public override fun obtenerUltimas(): Flow<List<LecturaFC>> {
    val _sql: String = """
        |
        |        SELECT * FROM lecturas_fc
        |        ORDER BY timestamp DESC
        |        LIMIT 50
        """.trimMargin()
    return createFlow(__db, false, arrayOf("lecturas_fc")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfValorBpm: Int = getColumnIndexOrThrow(_stmt, "valorBpm")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfHora: Int = getColumnIndexOrThrow(_stmt, "hora")
        val _columnIndexOfEsNormal: Int = getColumnIndexOrThrow(_stmt, "esNormal")
        val _result: MutableList<LecturaFC> = mutableListOf()
        while (_stmt.step()) {
          val _item: LecturaFC
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpValorBpm: Int
          _tmpValorBpm = _stmt.getLong(_columnIndexOfValorBpm).toInt()
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpHora: String
          _tmpHora = _stmt.getText(_columnIndexOfHora)
          val _tmpEsNormal: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfEsNormal).toInt()
          _tmpEsNormal = _tmp != 0
          _item = LecturaFC(_tmpId,_tmpValorBpm,_tmpTimestamp,_tmpHora,_tmpEsNormal)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun limpiarViejos(umbral: Long) {
    val _sql: String = "DELETE FROM lecturas_fc WHERE timestamp < ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, umbral)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
