package eu.kanade.tachiyomi.ui.home.intelligence

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import logcat.LogPriority
import tachiyomi.core.common.util.system.logcat

class KitsuXIntelDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "kitsux_intel.db"
        private const val DATABASE_VERSION = 1

        // Tables
        private const val TABLE_METADATA = "jikan_metadata"
        private const val TABLE_PROFILE = "user_genre_profile"
        private const val TABLE_CACHE = "trending_cache"

        // Metadata columns
        private const val COL_META_ID = "content_id"
        private const val COL_META_MAL_ID = "mal_id"
        private const val COL_META_TYPE = "media_type"
        private const val COL_META_TITLE = "title"
        private const val COL_META_COVER = "cover_url"
        private const val COL_META_BANNER = "banner_url"
        private const val COL_META_GENRES = "genres"
        private const val COL_META_SCORE = "score"
        private const val COL_META_POPULARITY = "popularity"
        private const val COL_META_SYNOPSIS = "synopsis"
        private const val COL_META_UPDATED = "last_updated"

        // Profile columns
        private const val COL_PROF_GENRE = "genre"
        private const val COL_PROF_SCORE = "score"

        // Cache columns
        private const val COL_CACHE_KEY = "cache_key"
        private const val COL_CACHE_DATA = "json_data"
        private const val COL_CACHE_UPDATED = "last_updated"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createMetadataTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_METADATA (
                $COL_META_ID TEXT PRIMARY KEY,
                $COL_META_MAL_ID INTEGER,
                $COL_META_TYPE TEXT,
                $COL_META_TITLE TEXT,
                $COL_META_COVER TEXT,
                $COL_META_BANNER TEXT,
                $COL_META_GENRES TEXT,
                $COL_META_SCORE REAL,
                $COL_META_POPULARITY INTEGER,
                $COL_META_SYNOPSIS TEXT,
                $COL_META_UPDATED INTEGER
            )
        """.trimIndent()

        val createProfileTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_PROFILE (
                $COL_PROF_GENRE TEXT PRIMARY KEY,
                $COL_PROF_SCORE INTEGER DEFAULT 0
            )
        """.trimIndent()

        val createCacheTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_CACHE (
                $COL_CACHE_KEY TEXT PRIMARY KEY,
                $COL_CACHE_DATA TEXT,
                $COL_CACHE_UPDATED INTEGER
            )
        """.trimIndent()

        db.execSQL(createMetadataTable)
        db.execSQL(createProfileTable)
        db.execSQL(createCacheTable)
        logcat(LogPriority.INFO) { "KitsuX Intelligence Database created successfully." }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Simple drops for first version migrations
        db.execSQL("DROP TABLE IF EXISTS $TABLE_METADATA")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILE")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CACHE")
        onCreate(db)
    }

    // --- Profile CRUD ---

    fun incrementGenreScore(genre: String, points: Int) {
        val normalizedGenre = genre.trim().lowercase()
        if (normalizedGenre.isBlank()) return

        val db = writableDatabase
        db.beginTransaction()
        try {
            val cursor = db.rawQuery(
                "SELECT $COL_PROF_SCORE FROM $TABLE_PROFILE WHERE $COL_PROF_GENRE = ?",
                arrayOf(normalizedGenre)
            )
            var currentScore = 0
            if (cursor.moveToFirst()) {
                currentScore = cursor.getInt(0)
            }
            cursor.close()

            val values = ContentValues().apply {
                put(COL_PROF_GENRE, normalizedGenre)
                put(COL_PROF_SCORE, currentScore + points)
            }

            db.insertWithOnConflict(TABLE_PROFILE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            db.setTransactionSuccessful()
            logcat(LogPriority.DEBUG) { "Incremented genre score for '$normalizedGenre' by $points. New score: ${currentScore + points}" }
        } catch (e: Exception) {
            logcat(LogPriority.ERROR, e) { "Failed to increment genre score" }
        } finally {
            db.endTransaction()
        }
    }

    fun getGenreScores(): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_PROF_GENRE, $COL_PROF_SCORE FROM $TABLE_PROFILE", null)
        while (cursor.moveToNext()) {
            val genre = cursor.getString(0)
            val score = cursor.getInt(1)
            result[genre] = score
        }
        cursor.close()
        return result
    }

    // --- Cache CRUD ---

    fun saveCache(key: String, jsonData: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CACHE_KEY, key)
            put(COL_CACHE_DATA, jsonData)
            put(COL_CACHE_UPDATED, System.currentTimeMillis())
        }
        db.insertWithOnConflict(TABLE_CACHE, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getCache(key: String, maxAgeMillis: Long): String? {
        var data: String? = null
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_CACHE_DATA, $COL_CACHE_UPDATED FROM $TABLE_CACHE WHERE $COL_CACHE_KEY = ?",
            arrayOf(key)
        )
        if (cursor.moveToFirst()) {
            val updated = cursor.getLong(1)
            if (System.currentTimeMillis() - updated < maxAgeMillis) {
                data = cursor.getString(0)
            }
        }
        cursor.close()
        return data
    }

    // --- Metadata Cache CRUD ---

    fun saveMetadata(
        contentId: String,
        malId: Long,
        mediaType: String,
        title: String,
        coverUrl: String?,
        bannerUrl: String?,
        genresJson: String,
        score: Double,
        popularity: Int,
        synopsis: String?
    ) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_META_ID, contentId)
            put(COL_META_MAL_ID, malId)
            put(COL_META_TYPE, mediaType)
            put(COL_META_TITLE, title)
            put(COL_META_COVER, coverUrl)
            put(COL_META_BANNER, bannerUrl)
            put(COL_META_GENRES, genresJson)
            put(COL_META_SCORE, score)
            put(COL_META_POPULARITY, popularity)
            put(COL_META_SYNOPSIS, synopsis)
            put(COL_META_UPDATED, System.currentTimeMillis())
        }
        db.insertWithOnConflict(TABLE_METADATA, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getMetadata(contentId: String, maxAgeMillis: Long): Cursor? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_METADATA WHERE $COL_META_ID = ?",
            arrayOf(contentId)
        )
        if (cursor.moveToFirst()) {
            val updated = cursor.getLong(cursor.getColumnIndexOrThrow(COL_META_UPDATED))
            if (System.currentTimeMillis() - updated < maxAgeMillis) {
                return cursor
            }
        }
        cursor.close()
        return null
    }
}
