package mx.edu.utng.rgam.basedatos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow // Importamos Flow para consultas reactivas

// Función del Dao es definir las operaciones
// Insercion, Modificacion, Eliminacion, Consulta
@Dao
interface PostDao {
    // 1. CONSULTA PRINCIPAL (REACTIVA)
    // Usamos Flow para que la UI se actualice automáticamente cuando cambie la BD
    @Query("SELECT * FROM posts ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    // 2. INSERCIÓN (ASÍNCRONA)
    // Marcamos como 'suspend' para que se ejecute fuera del hilo principal.
    @Insert
    suspend fun insert(post: PostEntity)

    // 3. ELIMINACIÓN (ASÍNCRONA)
    // Marcamos como 'suspend'
    @Delete
    suspend fun delete(post: PostEntity)

    // 4. ACTUALIZACIÓN (ASÍNCRONA)
    // Marcamos como 'suspend'
    @Update
    suspend fun update(post: PostEntity): Int

    // 5. CONSULTA POR ID (ASÍNCRONA)
    // Marcamos como 'suspend'
    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getById(id: Int): PostEntity
}