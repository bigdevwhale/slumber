package app.slumber.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import app.slumber.domain.Clarity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** The morning a moment belongs to: anything before 4 a.m. still counts as the night before. */
fun morningOf(at: Long, zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(at).atZone(zone).let { if (it.hour < 4) it.toLocalDate().minusDays(1) else it.toLocalDate() }

/** A dream as told on waking, and how clear it was ([Clarity] ordinal). */
@Entity(tableName = "dreams", indices = [Index("at")])
data class Dream(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val at: Long = System.currentTimeMillis(),
    val text: String = "",
    val clarity: Int = 0,
) {
    val clarityOf: Clarity get() = Clarity.of(clarity)
    fun day(zone: ZoneId = ZoneId.systemDefault()) = morningOf(at, zone)
}

/** A reality check done: from a nudge, or [self] when you remembered on your own. */
@Entity(tableName = "checks", indices = [Index("at")])
data class Check(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val at: Long = System.currentTimeMillis(),
    val self: Boolean = false,
)

/** The bedtime ritual, done. */
@Entity(tableName = "rituals", indices = [Index("at")])
data class Ritual(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val at: Long = System.currentTimeMillis(),
)

/** A dream sign of your own, in your words: "the blue door". */
@Entity(tableName = "own_signs")
data class OwnSign(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phrase: String,
    val at: Long = System.currentTimeMillis(),
)

@Dao
interface SlumberDao {
    @Query("SELECT * FROM dreams ORDER BY at DESC") fun dreams(): Flow<List<Dream>>
    @Query("SELECT * FROM checks ORDER BY at") fun checks(): Flow<List<Check>>
    @Query("SELECT * FROM rituals ORDER BY at") fun rituals(): Flow<List<Ritual>>
    @Query("SELECT * FROM own_signs ORDER BY at") fun ownSigns(): Flow<List<OwnSign>>

    @Query("SELECT * FROM dreams ORDER BY at DESC") suspend fun allDreams(): List<Dream>
    @Query("SELECT * FROM checks ORDER BY at") suspend fun allChecks(): List<Check>
    @Query("SELECT * FROM rituals ORDER BY at") suspend fun allRituals(): List<Ritual>
    @Query("SELECT * FROM own_signs ORDER BY at") suspend fun allOwnSigns(): List<OwnSign>
    @Query("SELECT * FROM checks ORDER BY at DESC LIMIT 1") suspend fun lastCheck(): Check?

    @Insert suspend fun insert(d: Dream): Long
    @Insert suspend fun insert(c: Check): Long
    @Insert suspend fun insert(r: Ritual): Long
    @Insert suspend fun insert(s: OwnSign): Long
    @Insert suspend fun insertDreams(d: List<Dream>)
    @Insert suspend fun insertChecks(c: List<Check>)
    @Insert suspend fun insertRituals(r: List<Ritual>)
    @Update suspend fun update(d: Dream)

    @Query("DELETE FROM dreams WHERE id = :id") suspend fun deleteDream(id: Long)
    @Query("DELETE FROM own_signs WHERE id = :id") suspend fun deleteOwnSign(id: Long)
}

@Database(entities = [Dream::class, Check::class, Ritual::class, OwnSign::class], version = 1, exportSchema = true)
abstract class SlumberDatabase : RoomDatabase() {
    abstract fun dao(): SlumberDao

    companion object {
        fun create(context: Context): SlumberDatabase =
            Room.databaseBuilder(context.applicationContext, SlumberDatabase::class.java, "spintop.db").build()
    }
}
