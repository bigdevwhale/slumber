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
import androidx.room.Transaction
import androidx.room.Update
import app.slumber.domain.Act
import app.slumber.domain.Draft
import app.slumber.domain.Fig
import app.slumber.domain.Kind
import app.slumber.domain.Look
import app.slumber.domain.Mood
import app.slumber.domain.Scene
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * One dream, as told on waking. [text] is the truth; the panels are a picture of it. [rewritten]: when
 * a new ending was drawn (its panels have alt = 1). [lucid]: the dreamer knew they were dreaming.
 */
@Entity(tableName = "dreams", indices = [Index("at")])
data class Dream(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val at: Long = System.currentTimeMillis(),
    val title: String = "",
    val text: String = "",
    val mood: String = Mood.WONDER.name,
    val lucid: Boolean = false,
    val rewritten: Long = 0,
) {
    val moodOf: Mood get() = Mood.of(mood)

    /** The morning it was brought back. A dream told just after midnight still belongs to the night before's morning. */
    fun day(zone: ZoneId = ZoneId.systemDefault()): LocalDate = Instant.ofEpochMilli(at).atZone(zone).let {
        if (it.hour < 3) it.toLocalDate().minusDays(1) else it.toLocalDate()
    }
}

/** A panel of a dream's page. [alt] 0 is the dream as it was; 1 is the new ending, when there is one. */
@Entity(tableName = "panels", indices = [Index("dreamId")])
data class Panel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dreamId: Long,
    val idx: Int,
    val alt: Int = 0,
    val scene: String = Scene.ROOM.name,
    /** A named place in the atlas, or 0. */
    val spotId: Long = 0,
    val figs: String = "",
    val act: String = Act.STAND.name,
    val mood: String = Mood.WONDER.name,
    val caption: String = "",
    val doodle: String = "",
) {
    val sceneOf: Scene get() = Scene.of(scene)
    val actOf: Act get() = Act.of(act)
    val moodOf: Mood get() = Mood.of(mood)
    val figsOf: List<Fig> get() = Fig.decode(figs)

    companion object {
        fun of(dreamId: Long, idx: Int, d: Draft, alt: Int = 0) = Panel(
            dreamId = dreamId, idx = idx, alt = alt, scene = d.scene.name, spotId = d.spotId,
            figs = Fig.encode(d.figs), act = d.act.name, mood = d.mood.name, caption = d.caption, doodle = d.doodle,
        )
    }
}

/** A place of your own dreamland: "Grandma's house, but bigger". [x], [y]: where it sits on the atlas. */
@Entity(tableName = "spots")
data class Spot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val scene: String = Scene.HOUSE.name,
    val x: Float = 0f,
    val y: Float = 0f,
    val at: Long = System.currentTimeMillis(),
) {
    val sceneOf: Scene get() = Scene.of(scene)
}

/** Someone who keeps coming back. [me] is the dreamer. */
@Entity(tableName = "people")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val look: String = Look().encode(),
    val me: Boolean = false,
    val at: Long = System.currentTimeMillis(),
) {
    val lookOf: Look get() = Look.decode(look)
}

@Dao
interface SlumberDao {
    @Query("SELECT * FROM dreams ORDER BY at DESC")
    fun dreams(): Flow<List<Dream>>

    @Query("SELECT * FROM panels ORDER BY dreamId, alt, idx")
    fun panels(): Flow<List<Panel>>

    @Query("SELECT * FROM spots ORDER BY at")
    fun spots(): Flow<List<Spot>>

    @Query("SELECT * FROM people ORDER BY me DESC, at")
    fun people(): Flow<List<Person>>

    @Query("SELECT * FROM dreams ORDER BY at DESC")
    suspend fun allDreams(): List<Dream>

    @Query("SELECT * FROM panels ORDER BY dreamId, alt, idx")
    suspend fun allPanels(): List<Panel>

    @Query("SELECT * FROM spots ORDER BY at")
    suspend fun allSpots(): List<Spot>

    @Query("SELECT * FROM people ORDER BY me DESC, at")
    suspend fun allPeople(): List<Person>

    @Query("SELECT * FROM people WHERE me = 1 LIMIT 1")
    suspend fun me(): Person?

    @Query("SELECT * FROM dreams WHERE id = :id")
    suspend fun dream(id: Long): Dream?

    @Query("SELECT * FROM panels WHERE dreamId = :id AND alt = :alt ORDER BY idx")
    suspend fun panelsOf(id: Long, alt: Int): List<Panel>

    @Insert suspend fun insert(d: Dream): Long
    @Insert suspend fun insert(p: Panel): Long
    @Insert suspend fun insertPanels(p: List<Panel>)
    @Insert suspend fun insert(s: Spot): Long
    @Insert suspend fun insert(p: Person): Long

    @Update suspend fun update(d: Dream)
    @Update suspend fun update(p: Panel)
    @Update suspend fun update(s: Spot)
    @Update suspend fun update(p: Person)

    @Query("DELETE FROM dreams WHERE id = :id")
    suspend fun deleteDream(id: Long)

    @Query("DELETE FROM panels WHERE dreamId = :id AND alt = :alt")
    suspend fun deletePanels(id: Long, alt: Int)

    @Query("DELETE FROM panels WHERE dreamId = :id")
    suspend fun deleteAllPanels(id: Long)

    @Query("DELETE FROM spots WHERE id = :id")
    suspend fun deleteSpot(id: Long)

    @Query("UPDATE panels SET spotId = 0 WHERE spotId = :id")
    suspend fun unlinkSpot(id: Long)

    @Query("DELETE FROM people WHERE id = :id AND me = 0")
    suspend fun deletePerson(id: Long)

    /** A new dream with its first-guess panels. */
    @Transaction
    suspend fun tell(d: Dream, panels: List<Draft>): Long {
        val id = insert(d)
        insertPanels(panels.mapIndexed { i, p -> Panel.of(id, i, p) })
        return id
    }

    /** The page as edited: the panels of one version replaced whole. */
    @Transaction
    suspend fun replacePanels(dreamId: Long, alt: Int, panels: List<Panel>) {
        deletePanels(dreamId, alt)
        insertPanels(panels.mapIndexed { i, p -> p.copy(id = 0, dreamId = dreamId, idx = i, alt = alt) })
    }

    @Transaction
    suspend fun forget(id: Long) {
        deleteAllPanels(id)
        deleteDream(id)
    }

    /** Someone leaves the cast: in old dreams they become a nameless someone. */
    @Transaction
    suspend fun dismiss(id: Long) {
        for (p in allPanels()) {
            val figs = p.figsOf
            if (figs.none { it == Fig.Cast(id) }) continue
            update(p.copy(figs = Fig.encode(figs.map { if (it == Fig.Cast(id)) Fig.Extra(Kind.SOMEONE) else it })))
        }
        deletePerson(id)
    }

    /** Someone who came back gets kept: every nameless figure with their [label] becomes them. */
    @Transaction
    suspend fun adopt(person: Person, label: String): Long {
        val id = insert(person)
        val key = label.trim().lowercase()
        if (key.isEmpty()) return id
        for (p in allPanels()) {
            val figs = p.figsOf
            if (figs.none { it is Fig.Extra && it.label.trim().lowercase() == key }) continue
            update(p.copy(figs = Fig.encode(figs.map { if (it is Fig.Extra && it.label.trim().lowercase() == key) Fig.Cast(id) else it })))
        }
        return id
    }

    @Transaction
    suspend fun forgetSpot(id: Long) {
        unlinkSpot(id)
        deleteSpot(id)
    }
}

@Database(entities = [Dream::class, Panel::class, Spot::class, Person::class], version = 1, exportSchema = true)
abstract class SlumberDatabase : RoomDatabase() {
    abstract fun dao(): SlumberDao

    companion object {
        fun create(context: Context): SlumberDatabase =
            Room.databaseBuilder(context.applicationContext, SlumberDatabase::class.java, "slumber.db").build()
    }
}
