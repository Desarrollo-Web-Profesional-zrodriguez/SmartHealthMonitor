package mx.utng.smarthealthmonitor.`data`.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SmartHealthDB_Impl : SmartHealthDB() {
  private val _lecturaFCDao: Lazy<LecturaFCDao> = lazy {
    LecturaFCDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "769981bf94b6b2101c19808596c249c2", "8a25280a8a8fdda8ad70f11733c41889") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `lecturas_fc` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `valorBpm` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `hora` TEXT NOT NULL, `esNormal` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '769981bf94b6b2101c19808596c249c2')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `lecturas_fc`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsLecturasFc: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsLecturasFc.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLecturasFc.put("valorBpm", TableInfo.Column("valorBpm", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLecturasFc.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLecturasFc.put("hora", TableInfo.Column("hora", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLecturasFc.put("esNormal", TableInfo.Column("esNormal", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysLecturasFc: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesLecturasFc: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoLecturasFc: TableInfo = TableInfo("lecturas_fc", _columnsLecturasFc, _foreignKeysLecturasFc, _indicesLecturasFc)
        val _existingLecturasFc: TableInfo = read(connection, "lecturas_fc")
        if (!_infoLecturasFc.equals(_existingLecturasFc)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |lecturas_fc(mx.utng.smarthealthmonitor.data.db.LecturaFC).
              | Expected:
              |""".trimMargin() + _infoLecturasFc + """
              |
              | Found:
              |""".trimMargin() + _existingLecturasFc)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "lecturas_fc")
  }

  public override fun clearAllTables() {
    super.performClear(false, "lecturas_fc")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(LecturaFCDao::class, LecturaFCDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun lecturaDao(): LecturaFCDao = _lecturaFCDao.value
}
