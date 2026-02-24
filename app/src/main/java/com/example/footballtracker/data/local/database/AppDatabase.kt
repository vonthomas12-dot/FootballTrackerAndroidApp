package com.example.footballtracker.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.footballtracker.data.local.dao.MatchDao
import com.example.footballtracker.data.local.entity.MatchEntity
import com.example.footballtracker.data.local.entity.PlayerEntity

@Database(
    entities = [MatchEntity::class, PlayerEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun matchDao(): MatchDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // To remove the foreign key and index, we need to recreate the table in SQLite
                db.execSQL(
                    "CREATE TABLE players_new (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "matchOwnerId INTEGER NOT NULL, " +
                            "name TEXT NOT NULL, " +
                            "team TEXT NOT NULL, " +
                            "goals INTEGER NOT NULL)"
                )
                db.execSQL(
                    "INSERT INTO players_new (id, matchOwnerId, name, team, goals) " +
                            "SELECT id, matchOwnerId, name, team, goals FROM players"
                )
                db.execSQL("DROP TABLE players")
                db.execSQL("ALTER TABLE players_new RENAME TO players")
            }
        }

        fun build(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "football_tracker_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
