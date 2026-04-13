package com.example.luontopeli.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.luontopeli.data.local.dao.NatureSpotDao
import com.example.luontopeli.data.local.dao.WalkSessionDao
import com.example.luontopeli.data.local.entity.NatureSpot
import com.example.luontopeli.data.local.entity.WalkSession

@Database(
    entities = [WalkSession::class, NatureSpot::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun walkSessionDao(): WalkSessionDao
    abstract fun natureSpotDao(): NatureSpotDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "luontopeli_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}