package cat.copernic.aguamap1.domain.model

import com.google.firebase.firestore.Exclude
import java.util.Date

data class Fountain(
    val id: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val operational: Boolean = true,
    val category: Category = Category(),
    val votedByPositive: List<String> = emptyList(),
    val votedByNegative: List<String> = emptyList(),
    val description: String = "",
    val imageUrl: String = "",
    val dateCreated: Date = Date(),
    val ratingAverage: Double = 0.0,
    val totalRatings: Int = 0,
    val status: StateFountain = StateFountain.PENDING,
    val createdBy: String = "",
    val positiveVotes: Int = 0,
    val negativeVotes: Int = 0,
    @get:Exclude
    val comments: List<Comment> = emptyList(),
    @get:Exclude
    val distanceFromUser: Double? = null
)