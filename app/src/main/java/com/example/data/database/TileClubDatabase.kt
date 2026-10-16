package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LevelEntity::class, UserProfileEntity::class],
    version = 3,
    exportSchema = false
)
abstract class TileClubDatabase : RoomDatabase() {
    abstract fun dao(): TileClubDao

    companion object {
        @Volatile
        private var INSTANCE: TileClubDatabase? = null

        fun getDatabase(context: Context): TileClubDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TileClubDatabase::class.java,
                    "tile_club_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
