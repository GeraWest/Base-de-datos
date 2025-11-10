package mx.edu.utng.rgam.basedatos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState // Importante para observar el Flow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mx.edu.utng.rgam.basedatos.ui.theme.BaseDatosTheme

class MainActivity : ComponentActivity() {

    private lateinit var postDao: PostDao
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 🚨 Asegúrate de que AppDatabase.getDatabase está inicializado correctamente
        // y que la clase AppDatabase esté definida.
        db = AppDatabase.getDatabase(this)
        postDao = db.postDao()

        enableEdgeToEdge()

        setContent {
            BaseDatosTheme {
                // Pasamos la instancia del DAO al Composable principal
                PantallaPrincipal(postDao = postDao)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PantallaPrincipal(postDao: PostDao) {
        val scope = rememberCoroutineScope()

        // 🟢 CORRECCIÓN CLAVE: Observar el Flow como un State de Compose
        // postDao.getAll() devuelve Flow<List<PostEntity>>.
        // collectAsState() transforma ese Flow en un 'State' observable.
        // La lista se inicializa a 'emptyList()' y se actualiza automáticamente.
        val posts by postDao.getAll().collectAsState(initial = emptyList())

        var text by remember { mutableStateOf("") }
        var editingPost by remember { mutableStateOf<PostEntity?>(null) }

        // ❌ Eliminamos LaunchedEffect(Unit) que intentaba asignar el Flow a List
        // ❌ Eliminamos la función refresh() que ya no es necesaria

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(title = { Text("Mi diario con Persistencia") })
            },
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp) // Añadido padding general
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            label = { Text("¿Qué estás pensando?") },
                            // Ajustado el peso para que se vea bien
                            modifier = Modifier.weight(0.7f)
                        )

                        Button(
                            onClick = {
                                scope.launch {
                                    if (editingPost == null) {
                                        // ✅ Insertar (función suspendida)
                                        postDao.insert(PostEntity(content = text))
                                    } else {
                                        // ✅ Lógica de Edición Corregida
                                        val postToUpdate = editingPost!!.copy(content = text)
                                        postDao.update(postToUpdate)
                                        editingPost = null
                                    }
                                    text = ""
                                    // ❌ Ya no se llama a refresh() ni se asigna posts = postDao.getAll()
                                    // El cambio se propaga automáticamente por el Flow.
                                }
                            },
                            modifier = Modifier.padding(start = 8.dp).weight(0.3f) // Ajustado el peso
                        ) {
                            Text(if (editingPost == null) "Guardar" else "Editar")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (posts.isEmpty()) {
                        Text("No hay entradas en el diario. ¡Añade algo!",
                            modifier = Modifier.padding(16.dp),
                            color = androidx.compose.ui.graphics.Color.Gray)
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(posts) { post ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    // Añadido un poco de elevación y esquinas redondeadas
                                    elevation = androidx.compose.material3.CardDefaults.cardElevation(2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Usar un modificador para asegurar que el texto no empuje los botones
                                        Text(post.content, modifier = Modifier.weight(1f))

                                        Row(horizontalArrangement = Arrangement.End) {
                                            TextButton(onClick = {
                                                editingPost = post
                                                text = post.content
                                            }) { Text("Editar") }

                                            TextButton(onClick = {
                                                scope.launch {
                                                    // ✅ Eliminar (función suspendida)
                                                    postDao.delete(post)
                                                    // El Flow actualizará automáticamente la lista
                                                }
                                            }) { Text("Eliminar") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}


