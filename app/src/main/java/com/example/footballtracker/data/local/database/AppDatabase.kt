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
import com.example.footballtracker.data.local.entity.MatchPlayerEntity

@Database(
    entities = [MatchEntity::class, PlayerEntity::class, MatchPlayerEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun matchDao(): MatchDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create the new junction table
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS match_players (" +
                            "matchId INTEGER NOT NULL, " +
                            "playerId INTEGER NOT NULL, " +
                            "team TEXT NOT NULL, " +
                            "goals INTEGER NOT NULL, " +
                            "PRIMARY KEY(matchId, playerId), " +
                            "FOREIGN KEY(matchId) REFERENCES matches(matchId) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                            "FOREIGN KEY(playerId) REFERENCES players(id) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_match_players_matchId ON match_players (matchId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_match_players_playerId ON match_players (playerId)")

                // Migrate data from players to match_players for those assigned to a match
                db.execSQL(
                    "INSERT INTO match_players (matchId, playerId, team, goals) " +
                            "SELECT matchOwnerId, id, team, goals FROM players WHERE matchOwnerId != 0"
                )

                // Recreate players table without match-related columns
                db.execSQL(
                    "CREATE TABLE players_new (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "name TEXT NOT NULL, " + "goals INTEGER NOT NULL)")
                db.execSQL("INSERT INTO players_new (id, name, goals) SELECT id, name, goals FROM players")
                db.execSQL("DROP TABLE players")
                db.execSQL("ALTER TABLE players_new RENAME TO players")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE matches ADD COLUMN isUploaded INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE players ADD COLUMN assists INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE match_players ADD COLUMN assists INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun build(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "football_tracker_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
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
    }
}
