package mx.edu.utng.rgam.basedatos

import androidx.room.Entity
import androidx.room.PrimaryKey

//Una entidad representa una tabla
@Entity(tableName = "posts")
data class PostEntity (
    @PrimaryKey(autoGenerate = true) val id: Int =0,
    var content: String
)