package cat.copernic.aguamap1.domain.model

import com.google.firebase.firestore.Exclude
import java.util.Date

data class Fountain(
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isOperational: Boolean = true,
    val category: String = "",
    val description: String = "",
    val dateCreated: Date = Date(),
    val visits: Long = 0,
    val ratingAverage: Double = 0.0,
    val totalRatings: Int = 0,
    val status: String = "PENDING",
    val createdBy: String = "",
    val positiveVotes: Int = 0,
    val negativeVotes: Int = 0,
    @get:Exclude
    val distanceFromUser: Double? = null
)