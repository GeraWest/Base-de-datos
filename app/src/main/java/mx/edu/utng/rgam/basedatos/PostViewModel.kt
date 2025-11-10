package mx.edu.utng.rgam.basedatos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ⚠️ Usaremos el patrón UiState para gestionar el estado de forma inmutable y observable
data class PostUiState(
    val posts: List<PostEntity> = emptyList()
)

class PostViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Inicialización de Room (Dejar la inicialización aquí es válido para AndroidViewModel)
    private val dao = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "diario_db"
    ).build().postDao()

    // 2. 🟢 CORRECCIÓN CLAVE: Transformar el Flow de Room a StateFlow
    // Este StateFlow será la única fuente de verdad que la UI observará.
    val uiState: StateFlow<PostUiState> = dao.getAll()
        // Mapeamos el resultado de Flow<List<PostEntity>> a nuestro PostUiState
        .map { postList ->
            PostUiState(posts = postList)
        }
        // Lo convertimos en un StateFlow que se inicia inmediatamente y se mantiene vivo
        .stateIn(
            scope = viewModelScope,
            // Mantiene la coroutine activa mientras la UI esté visible (recomendado)
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PostUiState(posts = emptyList())
        )

    // ❌ La función loadPosts() ya no es necesaria, el Flow lo hace por ti al inicializarse.

    // Función que agrega nueva publicacion
    fun addPost(content: String){
        viewModelScope.launch {
            // ✅ Insertar (función suspendida). El Flow se encarga de la recarga automática.
            dao.insert(PostEntity(content=content))
        }
    }

    // Función que actualiza un Post
    // Necesitas una función de actualización ya que la editaste en MainActivity.kt
    fun updatePost(post: PostEntity){
        viewModelScope.launch {
            // ✅ Actualizar (función suspendida). El Flow se encarga de la recarga automática.
            dao.update(post)
        }
    }

    // Funcion que elimina un Post
    fun deletePost(post: PostEntity){
        viewModelScope.launch {
            // ✅ Eliminar (función suspendida). El Flow se encarga de la recarga automática.
            dao.delete(post)
        }
    }
}
