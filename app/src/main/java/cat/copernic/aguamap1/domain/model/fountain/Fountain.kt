package cat.copernic.aguamap1.domain.model.fountain

import cat.copernic.aguamap1.domain.model.category.Category
import cat.copernic.aguamap1.domain.model.comment.Comment
import cat.copernic.aguamap1.domain.model.fountain.StateFountain
import com.google.firebase.firestore.Exclude
import java.util.Date

/**
 * Representa una fuente de agua en el ecosistema de AguaMap.
 * Contiene información geográfica, metadatos de usuario, estadísticas de valoración
 * y estados de moderación para su visualización en el mapa y gestión administrativa.
 *
 * @property id Identificador único del documento en Firestore.
 * @property name Nombre descriptivo dado a la fuente por el usuario.
 * @property latitude Coordenada de latitud para el posicionamiento en el mapa.
 * @property longitude Coordenada de longitud para el posicionamiento en el mapa.
 * @property geohash Cadena de texto que codifica las coordenadas para búsquedas geográficas eficientes.
 * @property operational Indica si la fuente tiene agua y funciona actualmente.
 * @property category Objeto [Category] que define el tipo de fuente (ej. Natural, Ornamental).
 * @property votedByPositive Lista de UIDs de usuarios que han dado un voto positivo.
 * @property votedByNegative Lista de UIDs de usuarios que han dado un voto negativo.
 * @property description Detalles adicionales sobre la ubicación o estado de la fuente.
 * @property imageUrl URL de la fotografía de la fuente almacenada en la nube (Cloudinary).
 * @property dateCreated Fecha y hora en la que se registró la fuente en el sistema.
 * @property ratingAverage Promedio de las valoraciones (estrellas) recibidas por los usuarios.
 * @property totalRatings Contador total de reseñas realizadas sobre esta fuente.
 * @property status Estado de moderación de la fuente (PENDING, APPROVED, REJECTED).
 * @property createdBy UID del usuario que dio de alta la fuente.
 * @property positiveVotes Contador acumulado de votos positivos para facilitar el filtrado.
 * @property negativeVotes Contador acumulado de votos negativos.
 * @property comments Lista transitoria de comentarios (No persistida directamente en el objeto Fountain).
 * @property distanceFromUser Distancia calculada en tiempo de ejecución respecto a la ubicación del usuario.
 */
data class Fountain(
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val geohash: String = "",
    val operational: Boolean = true,
    val category: Category = Category(),
    val votedByPositive: List<String> = emptyList(),
    val votedByNegative: List<String> = emptyList(),
    val description: String = "",
    //val moreInformation: String = "",
    val imageUrl: String = "",
    val dateCreated: Date = Date(),
    val ratingAverage: Double = 0.0,
    val totalRatings: Int = 0,
    val status: StateFountain = StateFountain.PENDING,
    val createdBy: String = "",
    val positiveVotes: Int = 0,
    val negativeVotes: Int = 0,

    /**
     * @get:Exclude evita que Firestore intente leer/escribir este campo directamente
     * en el documento de la fuente, ya que los comentarios residen en una subcolección.
     */
    @get:Exclude
    val comments: List<Comment> = emptyList(),

    /**
     * Campo volátil utilizado solo en la capa de UI para ordenar fuentes por cercanía.
     */
    @get:Exclude
    val distanceFromUser: Double? = null
)