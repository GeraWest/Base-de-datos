package mx.edu.utng.rgam.basedatos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// La clase de la base de datos
@Database(entities = [PostEntity::class], version = 1) // Reemplaza `Post` con tu clase Entity
abstract class AppDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "diario_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
